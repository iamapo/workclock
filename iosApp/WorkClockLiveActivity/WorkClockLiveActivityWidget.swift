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
            .activityBackgroundTint(WorkClockPalette.background)
            .activitySystemActionForegroundColor(WorkClockPalette.ink)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(context.attributes.title)
                            .font(.caption)
                            .foregroundStyle(WorkClockPalette.mint)
                        Text(context.state.phaseLabel)
                            .font(.headline)
                            .foregroundStyle(WorkClockPalette.paper)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.startedAt, style: .timer)
                        .font(.title3.monospacedDigit().bold())
                        .foregroundStyle(WorkClockPalette.paper)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(context.state.subtitle)
                            .font(.caption)
                            .foregroundStyle(WorkClockPalette.paper.opacity(0.72))
                        WorkBreakProgressView(state: context.state)
                            .frame(height: 4)
                    }
                }
            } compactLeading: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(context.state.phase == "paused" ? WorkClockPalette.lemon : WorkClockPalette.mint)
            } compactTrailing: {
                Text(context.state.startedAt, style: .timer)
                    .monospacedDigit()
                    .foregroundStyle(WorkClockPalette.paper)
                    .frame(maxWidth: 54)
            } minimal: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(context.state.phase == "paused" ? WorkClockPalette.lemon : WorkClockPalette.mint)
            }
            .keylineTint(WorkClockPalette.mint)
        }
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct WorkClockLiveActivityView: View {
    let title: String
    let state: WorkClockLiveActivityAttributes.ContentState

    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 14) {
                PhaseIcon(phase: state.phase)

                VStack(alignment: .leading, spacing: 4) {
                    PhaseBadge(phase: state.phase)

                    Text(state.phaseLabel)
                        .font(.headline)
                        .foregroundStyle(WorkClockPalette.ink)
                        .lineLimit(1)

                    Text(state.subtitle)
                        .font(.subheadline)
                        .foregroundStyle(WorkClockPalette.muted)
                        .lineLimit(1)
                }

                Spacer(minLength: 8)

                Text(state.startedAt, style: .timer)
                    .font(.system(size: 27, weight: .bold, design: .rounded).monospacedDigit())
                    .foregroundStyle(WorkClockPalette.ink)
                    .minimumScaleFactor(0.72)
                    .lineLimit(1)
                    .frame(minWidth: 104, alignment: .trailing)
            }

            WorkBreakProgressView(state: state)
                .frame(height: 5)
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 14)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(title), \(state.phaseLabel), \(state.subtitle)")
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct PhaseIcon: View {
    let phase: String

    private var accent: Color {
        phase == "paused" ? WorkClockPalette.lemon : WorkClockPalette.mint
    }

    var body: some View {
        ZStack {
            Circle()
                .fill(accent.opacity(0.16))
            Circle()
                .stroke(accent, lineWidth: 5)
            Image(systemName: phase == "paused" ? "pause.fill" : "clock")
                .font(.system(size: 21, weight: .bold))
                .foregroundStyle(WorkClockPalette.ink)
        }
        .frame(width: 48, height: 48)
        .accessibilityHidden(true)
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct PhaseBadge: View {
    let phase: String

    private var accent: Color {
        phase == "paused" ? WorkClockPalette.lemon : WorkClockPalette.mint
    }

    private var foreground: Color {
        phase == "paused" ? WorkClockPalette.lemonInk : WorkClockPalette.mintInk
    }

    var body: some View {
        Text(phase == "paused" ? "PAUSE" : "ARBEITSZEIT")
            .font(.system(size: 11, weight: .bold, design: .rounded))
            .tracking(0.7)
            .foregroundStyle(foreground)
            .padding(.horizontal, 9)
            .padding(.vertical, 4)
            .background(accent.opacity(0.12), in: Capsule())
            .overlay {
                Capsule()
                    .stroke(accent.opacity(0.48), lineWidth: 1)
            }
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct WorkBreakProgressView: View {
    let state: WorkClockLiveActivityAttributes.ContentState

    private var workedShare: CGFloat {
        let worked = max(state.workedMinutes, 0)
        let paused = max(state.breakMinutes, 0)
        let total = worked + paused
        return total > 0 ? CGFloat(worked) / CGFloat(total) : 1
    }

    var body: some View {
        GeometryReader { proxy in
            let spacing: CGFloat = 3
            let availableWidth = max(proxy.size.width - spacing, 0)

            HStack(spacing: spacing) {
                Capsule()
                    .fill(WorkClockPalette.mint)
                    .frame(width: availableWidth * workedShare)
                Capsule()
                    .fill(WorkClockPalette.lemon)
                    .frame(width: availableWidth * (1 - workedShare))
            }
        }
        .accessibilityHidden(true)
    }
}

private enum WorkClockPalette {
    static let background = Color(red: 1.00, green: 0.98, blue: 0.95)
    static let paper = Color.white
    static let ink = Color(red: 0.09, green: 0.08, blue: 0.12)
    static let muted = Color(red: 0.32, green: 0.30, blue: 0.37)
    static let mint = Color(red: 0.40, green: 0.87, blue: 0.71)
    static let mintInk = Color(red: 0.04, green: 0.39, blue: 0.26)
    static let lemon = Color(red: 1.00, green: 0.85, blue: 0.30)
    static let lemonInk = Color(red: 0.42, green: 0.29, blue: 0.00)
}
