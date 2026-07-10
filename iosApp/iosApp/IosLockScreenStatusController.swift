import ActivityKit
import ComposeApp
import Foundation

final class IosLockScreenStatusController: NSObject, LockScreenStatusController {
    func publish(status: LockScreenStatus) {
        guard #available(iOS 16.1, *) else { return }

        Task { @MainActor in
            if status.visible {
                await Self.upsertLiveActivity(status: status)
            } else {
                await Self.endLiveActivity()
            }
        }
    }

    @available(iOS 16.1, *)
    @MainActor
    private static func upsertLiveActivity(status: LockScreenStatus) async {
        let state = WorkClockLiveActivityAttributes.ContentState(
            phase: status.phase,
            phaseLabel: status.phaseLabel,
            subtitle: status.subtitle,
            startedAt: startedAtDate(status: status),
            workedMinutes: Int(status.workedMinutes),
            breakMinutes: Int(status.breakMinutes)
        )

        if let activity = Activity<WorkClockLiveActivityAttributes>.activities.first {
            await activity.update(using: state)
            return
        }

        do {
            _ = try Activity<WorkClockLiveActivityAttributes>.request(
                attributes: WorkClockLiveActivityAttributes(title: status.title),
                contentState: state,
                pushType: nil
            )
        } catch {
            #if DEBUG
            print("Could not start WorkClock Live Activity: \(error)")
            #endif
        }
    }

    @available(iOS 16.1, *)
    @MainActor
    private static func endLiveActivity() async {
        for activity in Activity<WorkClockLiveActivityAttributes>.activities {
            await activity.end(dismissalPolicy: .immediate)
        }
    }

    private static func startedAtDate(status: LockScreenStatus) -> Date {
        if status.startedAtEpochMillis > 0 {
            return Date(timeIntervalSince1970: TimeInterval(status.startedAtEpochMillis) / 1000)
        }

        let elapsedSeconds = max(TimeInterval(status.elapsedMinutes) * 60, 0)
        return Date(timeIntervalSinceNow: -elapsedSeconds)
    }
}
