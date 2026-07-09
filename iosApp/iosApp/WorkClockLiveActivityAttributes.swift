import ActivityKit
import Foundation

@available(iOS 16.1, *)
struct WorkClockLiveActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var phase: String
        var phaseLabel: String
        var subtitle: String
        var startedAt: Date
    }

    var title: String
}
