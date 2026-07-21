import Foundation
import WatchConnectivity

final class WatchSessionModel: NSObject, ObservableObject {
    @Published private(set) var state = "Bereit"
    @Published private(set) var progress = 0.08
    @Published private(set) var remaining = "0:00"
    @Published private(set) var caption = "iPhone verbinden"
    @Published private(set) var primaryAction = "Tag starten"
    @Published private(set) var secondaryAction = ""
    @Published private(set) var command = "primary"
    @Published private(set) var isReachable = false
    @Published private(set) var breakStartedMinute: Int?

    private let session: WCSession?
    private let defaults: UserDefaults
    private var pendingEvents: [PendingWatchEvent]
    private var transferredEventIds: Set<String>
    private var localStatus: WatchStatus
    private var deferredStatePayload: [String: Any]?

    override init() {
        let defaults = UserDefaults.standard
        self.defaults = defaults
        self.pendingEvents = Self.loadPendingEvents(from: defaults)
        self.transferredEventIds = Set(defaults.stringArray(forKey: DefaultsKey.transferredEventIds) ?? [])
        self.localStatus = WatchStatus(
            rawValue: defaults.string(forKey: DefaultsKey.localStatus) ?? ""
        ) ?? .notStarted
        let savedBreakStartedMinute = defaults.integer(forKey: DefaultsKey.breakStartedMinute)
        self.breakStartedMinute = defaults.object(forKey: DefaultsKey.breakStartedMinute) == nil
            ? nil
            : savedBreakStartedMinute

        if WCSession.isSupported() {
            session = WCSession.default
        } else {
            session = nil
        }

        super.init()
        restoreLocalState()
        session?.delegate = self
        session?.activate()
    }

    func sendPrimaryAction() {
        enqueue(command)
    }

    func sendEndDay() {
        enqueue(WatchCommand.endDay)
    }

    private func enqueue(_ rawCommand: String) {
        let resolvedCommand = resolve(rawCommand)
        let event = PendingWatchEvent(command: resolvedCommand)
        pendingEvents.append(event)
        savePendingEvents()
        applyLocal(command: resolvedCommand, event: event)
        syncPendingEvents()
    }

    private func syncPendingEvents() {
        guard let session else { return }

        pendingEvents.forEach { event in
            let payload = event.payload
            if session.isReachable {
                session.sendMessage(
                    payload,
                    replyHandler: { [weak self] reply in
                        self?.apply(reply)
                    },
                    errorHandler: { [weak self] _ in
                        self?.transfer(event, using: session)
                    }
                )
            } else {
                transfer(event, using: session)
            }
        }
    }

    private func apply(_ payload: [String: Any]) {
        if let acknowledgedEventId = payload[PayloadKey.acknowledgedEventId] as? String {
            acknowledge(acknowledgedEventId)
            return
        }

        DispatchQueue.main.async {
            self.isReachable = self.session?.isReachable ?? false
            if !self.pendingEvents.isEmpty {
                self.deferredStatePayload = payload
                return
            }

            self.applyState(payload)
        }
    }

    private func applyState(_ payload: [String: Any]) {
        state = payload["state"] as? String ?? state
        if let receivedProgress = payload[PayloadKey.progress] as? Double {
            progress = receivedProgress
        } else if let receivedProgress = payload[PayloadKey.progress] as? Float {
            progress = Double(receivedProgress)
        } else if let receivedProgress = payload[PayloadKey.progress] as? NSNumber {
            progress = receivedProgress.doubleValue
        }
        remaining = payload["remaining"] as? String ?? remaining
        caption = payload["caption"] as? String ?? caption
        primaryAction = payload["primaryAction"] as? String ?? primaryAction
        secondaryAction = payload["secondaryAction"] as? String ?? secondaryAction
        command = payload["command"] as? String ?? command
        localStatus = WatchStatus(state: state)
        if let receivedBreakStartedMinute = payload[PayloadKey.breakStartedMinute] as? Int {
            breakStartedMinute = receivedBreakStartedMinute >= 0 ? receivedBreakStartedMinute : nil
        } else if localStatus != .paused {
            breakStartedMinute = nil
        }
        saveLocalStatus()
        saveBreakStartedMinute()
    }

    private func applyLocal(command: String, event: PendingWatchEvent) {
        switch command {
        case WatchCommand.startDay, WatchCommand.startNewDay:
            localStatus = .working
            breakStartedMinute = nil
        case WatchCommand.startBreak:
            localStatus = .paused
            breakStartedMinute = event.occurredAtMinuteOfDay
        case WatchCommand.resumeWork:
            localStatus = .working
            breakStartedMinute = nil
        case WatchCommand.endDay:
            localStatus = .finished
            breakStartedMinute = nil
        default:
            break
        }

        saveLocalStatus()
        saveBreakStartedMinute()
        updateControls(for: localStatus)
        remaining = localStatus == .finished ? "0:00" : "Offline"
        caption = offlineCaption(for: event)
    }

    private func restoreLocalState() {
        updateControls(for: localStatus)
        if let latestEvent = pendingEvents.last {
            remaining = localStatus == .finished ? "0:00" : "Offline"
            caption = offlineCaption(for: latestEvent)
        }
    }

    private func updateControls(for status: WatchStatus) {
        switch status {
        case .notStarted:
            state = "Bereit"
            primaryAction = "Tag starten"
            secondaryAction = ""
            command = WatchCommand.startDay
        case .working:
            state = "Aktiv"
            primaryAction = "Pause starten"
            secondaryAction = "Tag beenden"
            command = WatchCommand.startBreak
        case .paused:
            state = "Pause"
            primaryAction = "Weiterarbeiten"
            secondaryAction = "Tag beenden"
            command = WatchCommand.resumeWork
        case .finished:
            state = "Fertig"
            primaryAction = "Neuen Tag starten"
            secondaryAction = ""
            command = WatchCommand.startNewDay
        }
    }

    private func resolve(_ rawCommand: String) -> String {
        guard rawCommand == WatchCommand.primary else { return rawCommand }

        switch localStatus {
        case .notStarted:
            return WatchCommand.startDay
        case .working:
            return WatchCommand.startBreak
        case .paused:
            return WatchCommand.resumeWork
        case .finished:
            return WatchCommand.startNewDay
        }
    }

    private func transfer(_ event: PendingWatchEvent, using session: WCSession) {
        guard !transferredEventIds.contains(event.id) else { return }

        session.transferUserInfo(event.payload)
        transferredEventIds.insert(event.id)
        saveTransferredEventIds()
    }

    private func acknowledge(_ eventId: String) {
        DispatchQueue.main.async {
            self.pendingEvents.removeAll { $0.id == eventId }
            self.transferredEventIds.remove(eventId)
            self.savePendingEvents()
            self.saveTransferredEventIds()

            if let latestEvent = self.pendingEvents.last {
                self.remaining = self.localStatus == .finished ? "0:00" : "Offline"
                self.caption = self.offlineCaption(for: latestEvent)
                return
            }

            if let deferredStatePayload = self.deferredStatePayload {
                self.deferredStatePayload = nil
                self.applyState(deferredStatePayload)
                return
            }

            let latestApplicationContext = self.session?.receivedApplicationContext ?? [:]
            if !latestApplicationContext.isEmpty {
                self.applyState(latestApplicationContext)
            } else {
                self.remaining = self.localStatus == .finished ? "0:00" : "–"
                self.caption = "Synchronisiert · Status wird geladen"
            }
        }
    }

    private func savePendingEvents() {
        guard let encoded = try? JSONEncoder().encode(pendingEvents) else { return }
        defaults.set(encoded, forKey: DefaultsKey.pendingEvents)
    }

    private func saveTransferredEventIds() {
        defaults.set(Array(transferredEventIds), forKey: DefaultsKey.transferredEventIds)
    }

    private func saveLocalStatus() {
        defaults.set(localStatus.rawValue, forKey: DefaultsKey.localStatus)
    }

    private func saveBreakStartedMinute() {
        if let breakStartedMinute {
            defaults.set(breakStartedMinute, forKey: DefaultsKey.breakStartedMinute)
        } else {
            defaults.removeObject(forKey: DefaultsKey.breakStartedMinute)
        }
    }

    func breakDuration(at date: Date, calendar: Calendar = .current) -> String {
        guard let breakStartedMinute else { return "0:00" }
        let components = calendar.dateComponents([.hour, .minute], from: date)
        let currentMinute = (components.hour ?? 0) * 60 + (components.minute ?? 0)
        let elapsedMinutes = currentMinute >= breakStartedMinute
            ? currentMinute - breakStartedMinute
            : 24 * 60 - breakStartedMinute + currentMinute
        return "\(elapsedMinutes / 60):" + String(format: "%02d", elapsedMinutes % 60)
    }

    private func offlineCaption(for event: PendingWatchEvent) -> String {
        let count = pendingEvents.count
        let suffix = count == 1 ? "1 Aktion wartet" : "\(count) Aktionen warten"
        return "\(event.clockText) offline | \(suffix)"
    }

    private static func loadPendingEvents(from defaults: UserDefaults) -> [PendingWatchEvent] {
        guard let data = defaults.data(forKey: DefaultsKey.pendingEvents) else { return [] }
        return (try? JSONDecoder().decode([PendingWatchEvent].self, from: data)) ?? []
    }
}

extension WatchSessionModel: WCSessionDelegate {
    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        DispatchQueue.main.async {
            self.isReachable = session.isReachable
            let latestApplicationContext = session.receivedApplicationContext
            if !latestApplicationContext.isEmpty {
                self.apply(latestApplicationContext)
            }
            self.syncPendingEvents()
        }
    }

    func sessionReachabilityDidChange(_ session: WCSession) {
        DispatchQueue.main.async {
            self.isReachable = session.isReachable
            self.syncPendingEvents()
        }
    }

    func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        apply(message)
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        apply(applicationContext)
    }

    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any] = [:]) {
        apply(userInfo)
    }
}

private struct PendingWatchEvent: Codable, Identifiable {
    let id: String
    let command: String
    let occurredAtDate: String
    let occurredAtMinuteOfDay: Int
    let occurredAtEpochMillis: Int64

    init(command: String, date: Date = Date(), calendar: Calendar = .current) {
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: date)

        self.id = UUID().uuidString
        self.command = command
        self.occurredAtDate = String(
            format: "%04d-%02d-%02d",
            components.year ?? 0,
            components.month ?? 0,
            components.day ?? 0
        )
        self.occurredAtMinuteOfDay = (components.hour ?? 0) * 60 + (components.minute ?? 0)
        self.occurredAtEpochMillis = Int64(date.timeIntervalSince1970 * 1000)
    }

    var payload: [String: Any] {
        [
            PayloadKey.messageType: PayloadValue.watchEvent,
            PayloadKey.eventId: id,
            PayloadKey.command: command,
            PayloadKey.occurredAtDate: occurredAtDate,
            PayloadKey.occurredAtMinuteOfDay: occurredAtMinuteOfDay,
            PayloadKey.occurredAtEpochMillis: occurredAtEpochMillis
        ]
    }

    var clockText: String {
        let hours = occurredAtMinuteOfDay / 60
        let minutes = occurredAtMinuteOfDay % 60
        return String(format: "%02d:%02d", hours, minutes)
    }
}

private enum WatchStatus: String {
    case notStarted
    case working
    case paused
    case finished

    init(state: String) {
        switch state {
        case "Aktiv":
            self = .working
        case "Pause":
            self = .paused
        case "Fertig":
            self = .finished
        default:
            self = .notStarted
        }
    }
}

private enum WatchCommand {
    static let primary = "primary"
    static let startDay = "startDay"
    static let startBreak = "startBreak"
    static let resumeWork = "resumeWork"
    static let startNewDay = "startNewDay"
    static let endDay = "endDay"
}

private enum PayloadKey {
    static let messageType = "messageType"
    static let eventId = "eventId"
    static let command = "command"
    static let progress = "progress"
    static let occurredAtDate = "occurredAtDate"
    static let occurredAtMinuteOfDay = "occurredAtMinuteOfDay"
    static let occurredAtEpochMillis = "occurredAtEpochMillis"
    static let acknowledgedEventId = "acknowledgedEventId"
    static let breakStartedMinute = "breakStartedMinute"
}

private enum PayloadValue {
    static let watchEvent = "watchEvent"
}

private enum DefaultsKey {
    static let pendingEvents = "pending_watch_events"
    static let transferredEventIds = "transferred_watch_event_ids"
    static let localStatus = "local_watch_status"
    static let breakStartedMinute = "break_started_minute"
}
