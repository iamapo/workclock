import Foundation
import WorkClockCore

@MainActor
final class WorkClockModel: ObservableObject {
    @Published private(set) var day: WorkDay
    @Published private(set) var now = Date()
    @Published private(set) var syncError: String?
    @Published private(set) var isSyncing = false
    @Published private(set) var lastSyncDate: Date?

    let replica: CloudKitDayReplica
    private let workClock: MacWorkClock
    private var timer: Timer?
    private var lastAutomaticSync = Date.distantPast

    init() {
        let replica = CloudKitDayReplica()
        self.replica = replica
        workClock = MacWorkClock(replica: replica)
        day = workClock.today()
        replica.onError = { [weak self] message in
            Task { @MainActor in self?.syncError = message }
        }
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                self?.now = Date()
                self?.refresh()
                self?.requestAutomaticSyncIfNeeded()
            }
        }
        Task { await sync() }
    }

    var isRunning: Bool {
        day.status == WorkStatus.working || day.status == WorkStatus.paused
    }

    var isFinished: Bool {
        day.status == WorkStatus.finished
    }

    var statusText: String {
        switch day.status {
        case WorkStatus.working: "Arbeitszeit läuft"
        case WorkStatus.paused: "Pause"
        case WorkStatus.finished: "Arbeitstag beendet"
        default: "Noch nicht gestartet"
        }
    }

    var primaryTitle: String {
        switch day.status {
        case WorkStatus.working: "Pause"
        case WorkStatus.paused: "Fortsetzen"
        case WorkStatus.finished: "Neuen Tag starten"
        default: "Starten"
        }
    }

    var elapsedText: String {
        var minutes = Int(day.workedMinutes)
        if day.status == WorkStatus.working,
           let start = day.activeSessionStartMinute?.intValue {
            minutes += max(0, Self.minuteOfDay(from: now) - start)
        }
        return String(format: "%02d:%02d", minutes / 60, minutes % 60)
    }

    var syncText: String {
        if isSyncing {
            return "Sync: läuft …"
        }
        guard let lastSyncDate else {
            return "Sync: noch nicht erfolgt"
        }
        return "Sync: \(lastSyncDate.formatted(date: .omitted, time: .shortened))"
    }

    func primary() {
        _ = workClock.execute(command: .primary)
        changed()
    }

    func stop() {
        _ = workClock.execute(command: .endday)
        changed()
    }

    func reopen() {
        workClock.reopenDay()
        changed()
    }

    func eventDate(at index: Int) -> Date {
        guard let event = day.events[index] as? WorkEvent else {
            return now
        }
        return Calendar.current.startOfDay(for: now)
            .addingTimeInterval(TimeInterval(Int(event.minuteOfDay) * 60))
    }

    func changeEvent(at index: Int, to date: Date) {
        workClock.changeEventTime(
            eventIndex: Int32(index),
            minuteOfDay: Int32(Self.minuteOfDay(from: date))
        )
        changed()
    }

    func sync() async {
        guard !isSyncing else { return }
        isSyncing = true
        lastAutomaticSync = Date()
        defer { isSyncing = false }
        syncError = nil
        await withCheckedContinuation { continuation in
            workClock.sync { [weak self] success, _ in
                Task { @MainActor in
                    self?.refresh()
                    if success?.boolValue == true {
                        self?.lastSyncDate = Date()
                    } else if self?.syncError == nil {
                        self?.syncError = "Sync fehlgeschlagen."
                    }
                    continuation.resume()
                }
            }
        }
    }

    func refresh() {
        day = workClock.today()
    }

    private func changed() {
        refresh()
        Task { await sync() }
    }

    private func requestAutomaticSyncIfNeeded() {
        guard !isSyncing, Date().timeIntervalSince(lastAutomaticSync) >= 10 else { return }
        Task { await sync() }
    }

    private static func minuteOfDay(from date: Date) -> Int {
        let components = Calendar.current.dateComponents([.hour, .minute], from: date)
        return (components.hour ?? 0) * 60 + (components.minute ?? 0)
    }
}
