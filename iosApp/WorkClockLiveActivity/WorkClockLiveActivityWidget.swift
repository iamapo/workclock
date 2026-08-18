import ActivityKit
import Foundation
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
            .activityBackgroundTint(DayflowLivePalette.background)
            .activitySystemActionForegroundColor(DayflowLivePalette.navy)
            .widgetURL(URL(string: "workclock://timetracker"))
        } dynamicIsland: { context in
            let tone = DayflowLiveTone.forPhase(context.state.phase)

            return DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 4) {
                        LiveStatusPill(phase: context.state.phase, tone: tone)
                        Text(context.state.phaseLabel)
                            .font(.headline.weight(.black))
                            .foregroundStyle(.white)
                            .lineLimit(1)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text(context.state.startedAt, style: .timer)
                        .font(.title2.weight(.black))
                        .monospacedDigit()
                        .foregroundStyle(.white)
                        .frame(maxWidth: 104, alignment: .trailing)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(spacing: 7) {
                        WorkBreakProgressView(state: context.state)
                            .frame(height: 7)

                        HStack {
                            LiveDurationLabel(
                                label: "Arbeit",
                                minutes: context.state.workedMinutes,
                                color: DayflowLivePalette.green,
                                contentColor: .white.opacity(0.78)
                            )
                            Spacer()
                            Text(context.state.subtitle)
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(.white.opacity(0.72))
                                .lineLimit(1)
                            Spacer()
                            LiveDurationLabel(
                                label: "Pause",
                                minutes: context.state.breakMinutes,
                                color: DayflowLivePalette.sand,
                                contentColor: .white.opacity(0.78)
                            )
                        }
                    }
                }
            } compactLeading: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(tone.background)
            } compactTrailing: {
                Text(context.state.startedAt, style: .timer)
                    .fontWeight(.bold)
                    .monospacedDigit()
                    .foregroundStyle(.white)
                    .frame(maxWidth: 54)
            } minimal: {
                Image(systemName: context.state.phase == "paused" ? "pause.fill" : "clock.fill")
                    .foregroundStyle(tone.background)
            }
            .keylineTint(tone.background)
            .widgetURL(URL(string: "workclock://timetracker"))
        }
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct WorkClockLiveActivityView: View {
    let title: String
    let state: WorkClockLiveActivityAttributes.ContentState

    private var tone: DayflowLiveTone {
        DayflowLiveTone.forPhase(state.phase)
    }

    var body: some View {
        VStack(spacing: 10) {
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 8) {
                    Image(systemName: state.phase == "paused" ? "pause.fill" : "clock.fill")
                        .font(.caption.weight(.black))
                        .foregroundStyle(tone.content)

                    Text(title.uppercased())
                        .font(.caption2.weight(.black))
                        .tracking(0.7)
                        .foregroundStyle(tone.content.opacity(0.78))

                    Spacer()

                    LiveStatusPill(phase: state.phase, tone: tone)
                }

                HStack(alignment: .lastTextBaseline, spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(state.phaseLabel)
                            .font(.headline.weight(.black))
                            .foregroundStyle(tone.content)
                            .lineLimit(1)

                        Text(state.subtitle)
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(tone.content.opacity(0.76))
                            .lineLimit(1)
                    }

                    Spacer(minLength: 8)

                    Text(state.startedAt, style: .timer)
                        .font(.system(size: 30, weight: .black, design: .default))
                        .monospacedDigit()
                        .foregroundStyle(tone.content)
                        .minimumScaleFactor(0.72)
                        .lineLimit(1)
                        .frame(minWidth: 108, alignment: .trailing)
                }
            }
            .padding(.horizontal, 15)
            .padding(.vertical, 12)
            .background(tone.background, in: RoundedRectangle(cornerRadius: 18, style: .continuous))

            VStack(spacing: 6) {
                WorkBreakProgressView(state: state)
                    .frame(height: 8)

                HStack {
                    LiveDurationLabel(
                        label: "Arbeit",
                        minutes: state.workedMinutes,
                        color: DayflowLivePalette.green,
                        contentColor: DayflowLivePalette.navy.opacity(0.78)
                    )
                    Spacer()
                    LiveDurationLabel(
                        label: "Pause",
                        minutes: state.breakMinutes,
                        color: DayflowLivePalette.sand,
                        contentColor: DayflowLivePalette.navy.opacity(0.78)
                    )
                }
            }
            .padding(.horizontal, 2)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(title), \(state.phaseLabel), \(state.subtitle)")
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct LiveStatusPill: View {
    let phase: String
    let tone: DayflowLiveTone

    var body: some View {
        HStack(spacing: 5) {
            Circle()
                .fill(tone.pillContent)
                .frame(width: 6, height: 6)
            Text(phase == "paused" ? "PAUSE" : "AKTIV")
                .font(.caption2.weight(.black))
                .tracking(0.5)
        }
        .foregroundStyle(tone.pillContent)
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(tone.pillBackground, in: Capsule())
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct LiveDurationLabel: View {
    let label: String
    let minutes: Int
    let color: Color
    let contentColor: Color

    var body: some View {
        HStack(spacing: 5) {
            Circle()
                .fill(color)
                .frame(width: 7, height: 7)
            Text("\(label) \(DayflowDuration.text(minutes))")
                .font(.caption2.weight(.semibold))
                .monospacedDigit()
                .foregroundStyle(contentColor)
        }
    }
}

@available(iOSApplicationExtension 16.1, *)
private struct WorkBreakProgressView: View {
    let state: WorkClockLiveActivityAttributes.ContentState

    private var workedShare: CGFloat {
        guard state.targetMinutes > 0 else { return 0 }
        return min(max(CGFloat(state.workedMinutes) / CGFloat(state.targetMinutes), 0), 1)
    }

    private var breakShare: CGFloat {
        guard state.targetMinutes > 0 else { return 0 }
        let availableShare = max(1 - workedShare, 0)
        return min(max(CGFloat(state.breakMinutes) / CGFloat(state.targetMinutes), 0), availableShare)
    }

    var body: some View {
        GeometryReader { proxy in
            let progressWidth = proxy.size.width * min(workedShare + breakShare, 1)

            ZStack(alignment: .leading) {
                Capsule()
                    .fill(DayflowLivePalette.softMuted)

                HStack(spacing: 0) {
                    Rectangle()
                        .fill(DayflowLivePalette.green)
                        .frame(width: proxy.size.width * workedShare)
                    Rectangle()
                        .fill(DayflowLivePalette.sand)
                        .frame(width: proxy.size.width * breakShare)
                }
                .clipShape(Capsule())

                Rectangle()
                    .fill(DayflowLivePalette.navy)
                    .frame(width: 2)
                    .offset(x: max(progressWidth - 1, 0))
            }
        }
        .accessibilityHidden(true)
    }
}

private struct DayflowLiveTone {
    let background: Color
    let content: Color
    let pillBackground: Color
    let pillContent: Color

    static func forPhase(_ phase: String) -> DayflowLiveTone {
        if phase == "paused" {
            return DayflowLiveTone(
                background: DayflowLivePalette.sand,
                content: DayflowLivePalette.navy,
                pillBackground: DayflowLivePalette.background.opacity(0.84),
                pillContent: DayflowLivePalette.navy
            )
        }

        return DayflowLiveTone(
            background: DayflowLivePalette.coral,
            content: .white,
            pillBackground: DayflowLivePalette.successSoft,
            pillContent: DayflowLivePalette.success
        )
    }
}

private enum DayflowDuration {
    static func text(_ minutes: Int) -> String {
        let value = max(minutes, 0)
        return "\(value / 60):" + String(format: "%02d", value % 60)
    }
}

private enum DayflowLivePalette {
    static let background = Color(red: 1.000, green: 0.980, blue: 0.949)
    static let navy = Color(red: 0.063, green: 0.145, blue: 0.267)
    static let coral = Color(red: 1.000, green: 0.365, blue: 0.314)
    static let success = Color(red: 0.247, green: 0.482, blue: 0.345)
    static let successSoft = Color(red: 0.875, green: 0.965, blue: 0.914)
    static let sand = Color(red: 0.957, green: 0.843, blue: 0.643)
    static let green = Color(red: 0.400, green: 0.847, blue: 0.718)
    static let softMuted = Color(red: 0.925, green: 0.882, blue: 0.827)
}
