import ActivityKit
import SwiftUI
import WidgetKit

@main
struct WorkClockLiveActivityBundle: WidgetBundle {
    var body: some Widget {
        if #available(iOSApplicationExtension 16.1, *) {
            WorkClockLiveActivityWidget()
        }
    }
}

@available(iOSApplicationExtension 16.1, *)
struct WorkClockLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: WorkClockLiveActivityAttributes.self) { context in
            WorkClockLiveActivityView(
                title: context.attributes.title,
                state: context.state
            )
            .activityBackgroundTint(Color(red: 0.08, green: 0.11, blue: 0.10))
            .activitySystemActionForegroundColor(Color(red: 0.55, green: 0.86, blue: 0.68))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(context.attributes.title)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Text(context.state.phaseLabel)
                            .font(.headline)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.startedAt, style: .timer)
                        .font(.title3.monospacedDigit().bold())
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text(context.state.subtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            } compactLeading: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
            } compactTrailing: {
                Text(context.state.startedAt, style: .timer)
                    .monospacedDigit()
                    .frame(maxWidth: 54)
            } minimal: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
            }
        }
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct WorkClockLiveActivityView: View {
    let title: String
    let state: WorkClockLiveActivityAttributes.ContentState

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color(red: 0.20, green: 0.49, blue: 0.36).opacity(0.22))
                    .frame(width: 42, height: 42)
                Image(systemName: state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(Color(red: 0.55, green: 0.86, blue: 0.68))
                    .font(.system(size: 18, weight: .bold))
            }

            VStack(alignment: .leading, spacing: 5) {
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(state.phaseLabel)
                    .font(.headline)
                Text(state.subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer(minLength: 8)

            Text(state.startedAt, style: .timer)
                .font(.system(size: 30, weight: .bold, design: .rounded).monospacedDigit())
                .minimumScaleFactor(0.75)
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 14)
    }
}
