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
            .widgetURL(URL(string: "workclock://timetracker"))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(context.attributes.title.uppercased())
                            .font(.system(.caption, design: .monospaced))
                            .tracking(0.6)
                            .foregroundStyle(WorkClockPalette.paper.opacity(0.7))
                        Text(context.state.phaseLabel)
                            .font(.system(.headline, design: .serif))
                            .foregroundStyle(WorkClockPalette.paper)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.startedAt, style: .timer)
                        .font(.system(.title3, design: .serif).monospacedDigit().bold())
                        .foregroundStyle(WorkClockPalette.paper)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(context.state.subtitle)
                            .font(.system(.caption, design: .monospaced))
                            .foregroundStyle(WorkClockPalette.paper.opacity(0.72))
                        WorkBreakProgressView(state: context.state)
                            .frame(height: 4)
                    }
                }
            } compactLeading: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(context.state.phase == "paused" ? WorkClockPalette.ochre : WorkClockPalette.plum)
            } compactTrailing: {
                Text(context.state.startedAt, style: .timer)
                    .monospacedDigit()
                    .foregroundStyle(WorkClockPalette.paper)
                    .frame(maxWidth: 54)
            } minimal: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(context.state.phase == "paused" ? WorkClockPalette.ochre : WorkClockPalette.plum)
            }
            .keylineTint(context.state.phase == "paused" ? WorkClockPalette.ochre : WorkClockPalette.plum)
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
                    PhaseStamp(phase: state.phase)

                    Text(state.phaseLabel)
                        .font(.system(.headline, design: .serif))
                        .foregroundStyle(WorkClockPalette.ink)
                        .lineLimit(1)

                    Text(state.subtitle)
                        .font(.system(.subheadline, design: .serif))
                        .foregroundStyle(WorkClockPalette.muted)
                        .lineLimit(1)
                }

                Spacer(minLength: 8)

                Text(state.startedAt, style: .timer)
                    .font(.system(size: 27, weight: .bold, design: .serif).monospacedDigit())
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
        phase == "paused" ? WorkClockPalette.ochre : WorkClockPalette.plum
    }

    var body: some View {
        ZStack {
            Circle()
                .fill(WorkClockPalette.panel)
            Circle()
                .stroke(accent, lineWidth: 2)
            Image(systemName: phase == "paused" ? "pause.fill" : "clock")
                .font(.system(size: 19, weight: .bold))
                .foregroundStyle(accent)
        }
        .frame(width: 46, height: 46)
        .accessibilityHidden(true)
    }
}

/// A rotated, bordered tag — the ledger's rubber stamp — instead of a filled pill.
@available(iOSApplicationExtension 16.1, *)
private struct PhaseStamp: View {
    let phase: String

    private var accent: Color {
        phase == "paused" ? WorkClockPalette.ochre : WorkClockPalette.plum
    }

    var body: some View {
        Text(phase == "paused" ? "PAUSE" : "ARBEITSZEIT")
            .font(.system(size: 11, weight: .bold, design: .monospaced))
            .tracking(0.6)
            .foregroundStyle(accent)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .overlay {
                RoundedRectangle(cornerRadius: 3)
                    .stroke(accent, lineWidth: 1.5)
            }
            .rotationEffect(.degrees(-3))
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
                    .fill(WorkClockPalette.green)
                    .frame(width: availableWidth * workedShare)
                Capsule()
                    .fill(WorkClockPalette.ochre)
                    .frame(width: availableWidth * (1 - workedShare))
            }
        }
        .accessibilityHidden(true)
    }
}

/// Ledger palette: green columnar paper, ink-navy text, a red margin accent — mirrors AppColors.kt.
private enum WorkClockPalette {
    static let background = Color(red: 0.918, green: 0.941, blue: 0.898)
    static let panel = Color(red: 0.957, green: 0.973, blue: 0.941)
    static let paper = Color(red: 0.918, green: 0.941, blue: 0.898)
    static let ink = Color(red: 0.118, green: 0.165, blue: 0.239)
    static let muted = Color(red: 0.357, green: 0.420, blue: 0.373)
    static let green = Color(red: 0.290, green: 0.420, blue: 0.243)
    static let ochre = Color(red: 0.659, green: 0.471, blue: 0.122)
    static let plum = Color(red: 0.478, green: 0.310, blue: 0.561)
    static let margin = Color(red: 0.698, green: 0.227, blue: 0.180)
}
