import ComposeApp
import Foundation
import UserNotifications

final class IosReminderScheduler: NSObject, ReminderScheduler {
    private let center = UNUserNotificationCenter.current()
    private var latestSchedule: ReminderSchedule?
    private var authorizationResultHandler: ((KotlinBoolean) -> Void)?

    func apply(schedule: ReminderSchedule) {
        cancelPendingReminders()
        guard schedule.hasReminders else {
            latestSchedule = nil
            return
        }

        latestSchedule = schedule
        center.getNotificationSettings(completionHandler: handleSchedulingSettings)
    }

    func requestAuthorization(onResult: @escaping (KotlinBoolean) -> Void) {
        authorizationResultHandler = onResult
        center.getNotificationSettings(completionHandler: handleAuthorizationSettings)
    }

    private func handleSchedulingSettings(_ settings: UNNotificationSettings) {
        DispatchQueue.main.async { [weak self] in
            self?.scheduleLatestIfAuthorized(settings)
        }
    }

    private func scheduleLatestIfAuthorized(_ settings: UNNotificationSettings) {
        guard settings.authorizationStatus.allowsNotifications,
              let schedule = latestSchedule else { return }

        scheduleOne(id: Identifier.breakReminder, reminder: schedule.breakReminder)
        scheduleOne(id: Identifier.dailyTarget, reminder: schedule.dailyTargetReminder)
        scheduleOne(id: Identifier.weeklyTarget, reminder: schedule.weeklyTargetReminder)
    }

    private func scheduleOne(id: String, reminder: Reminder?) {
        guard let reminder else { return }

        let content = UNMutableNotificationContent()
        content.title = reminder.title
        content.body = reminder.body
        content.sound = .default

        let interval = max(
            TimeInterval(reminder.triggerAtEpochMillis) / 1000 - Date().timeIntervalSince1970,
            1
        )
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: interval, repeats: false)
        let request = UNNotificationRequest(identifier: id, content: content, trigger: trigger)
        center.add(request)
    }

    private func cancelPendingReminders() {
        center.removePendingNotificationRequests(withIdentifiers: [
            Identifier.breakReminder,
            Identifier.dailyTarget,
            Identifier.weeklyTarget
        ])
    }

    private func handleAuthorizationSettings(_ settings: UNNotificationSettings) {
        switch settings.authorizationStatus {
        case .notDetermined:
            center.requestAuthorization(
                options: [.alert, .sound],
                completionHandler: handleAuthorizationRequest
            )
        case .authorized, .provisional, .ephemeral:
            completeAuthorization(granted: true)
        case .denied:
            completeAuthorization(granted: false)
        @unknown default:
            completeAuthorization(granted: false)
        }
    }

    private func handleAuthorizationRequest(granted: Bool, error: Error?) {
        completeAuthorization(granted: granted && error == nil)
    }

    private func completeAuthorization(granted: Bool) {
        guard let authorizationResultHandler else { return }
        self.authorizationResultHandler = nil
        DispatchQueue.main.async {
            authorizationResultHandler(KotlinBoolean(bool: granted))
        }
    }

    private enum Identifier {
        static let breakReminder = "workclock.reminder.break"
        static let dailyTarget = "workclock.reminder.dailyTarget"
        static let weeklyTarget = "workclock.reminder.weeklyTarget"
    }
}

private extension UNAuthorizationStatus {
    var allowsNotifications: Bool {
        switch self {
        case .authorized, .provisional, .ephemeral:
            return true
        case .notDetermined, .denied:
            return false
        @unknown default:
            return false
        }
    }
}

private extension ReminderSchedule {
    var hasReminders: Bool {
        breakReminder != nil || dailyTargetReminder != nil || weeklyTargetReminder != nil
    }
}
