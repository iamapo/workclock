import SwiftUI

struct WatchContentView: View {
    @ObservedObject var session: WatchSessionModel

    var body: some View {
        ZStack {
            DayflowWatchPalette.background
                .ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 9) {
                    brandHeader
                    hero
                    progress
                    actions
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 7)
            }
        }
    }

    private var brandHeader: some View {
        HStack(spacing: 7) {
            Image(systemName: "clock.fill")
                .font(.caption2.weight(.black))
                .foregroundStyle(.white)
                .frame(width: 22, height: 22)
                .background(DayflowWatchPalette.coral, in: Circle())

            Text("WORKCLOCK")
                .font(.caption2.weight(.black))
                .tracking(0.5)
                .foregroundStyle(DayflowWatchPalette.navy)

            Spacer(minLength: 2)

            HStack(spacing: 4) {
                Circle()
                    .fill(tone.pillContent)
                    .frame(width: 5, height: 5)
                Text(session.state.uppercased())
                    .font(.system(.caption2, design: .default).weight(.black))
                    .lineLimit(1)
            }
            .foregroundStyle(tone.pillContent)
            .padding(.horizontal, 7)
            .padding(.vertical, 4)
            .background(tone.pillBackground, in: Capsule())
        }
    }

    private var hero: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(heroKicker)
                .font(.caption2.weight(.black))
                .tracking(0.7)
                .foregroundStyle(tone.content.opacity(0.82))

            heroValue

            Text(session.caption)
                .font(.caption2.weight(.semibold))
                .foregroundStyle(tone.content.opacity(0.8))
                .lineLimit(1)
                .minimumScaleFactor(0.65)
        }
        .frame(maxWidth: .infinity, minHeight: 82, alignment: .leading)
        .padding(.horizontal, 13)
        .padding(.vertical, 10)
        .background(tone.background, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }

    @ViewBuilder
    private var heroValue: some View {
        if isPaused {
            TimelineView(.periodic(from: .now, by: 1)) { context in
                Text(session.breakDuration(at: context.date))
                    .font(.title.weight(.black))
                    .monospacedDigit()
                    .foregroundStyle(tone.content)
            }
        } else {
            Text(session.remaining)
                .font(.title.weight(.black))
                .monospacedDigit()
                .foregroundStyle(tone.content)
                .lineLimit(1)
                .minimumScaleFactor(0.65)
        }
    }

    private var progress: some View {
        VStack(spacing: 4) {
            HStack {
                Text("TAGESFORTSCHRITT")
                    .font(.caption2.weight(.black))
                    .tracking(0.4)
                Spacer()
                Text("\(progressPercentage) %")
                    .font(.caption2.weight(.black))
                    .monospacedDigit()
            }
            .foregroundStyle(DayflowWatchPalette.navy)

            ProgressView(value: session.progress.clamped(to: 0...1))
                .progressViewStyle(.linear)
                .tint(DayflowWatchPalette.green)
                .background(DayflowWatchPalette.softMuted, in: Capsule())
        }
    }

    private var actions: some View {
        HStack(spacing: 7) {
            Button {
                session.sendPrimaryAction()
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: primarySymbol)
                    Text(session.primaryAction)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                }
                .font(.caption2.weight(.black))
                .frame(maxWidth: .infinity, minHeight: 37)
            }
            .buttonStyle(.plain)
            .background(DayflowWatchPalette.navy, in: Capsule())
            .foregroundStyle(.white)
            .accessibilityLabel(session.primaryAction)

            if !session.secondaryAction.isEmpty {
                Button {
                    session.sendEndDay()
                } label: {
                    Image(systemName: "stop.fill")
                        .font(.caption.weight(.black))
                        .frame(width: 37, height: 37)
                }
                .buttonStyle(.plain)
                .background(DayflowWatchPalette.panel, in: Circle())
                .overlay {
                    Circle().stroke(DayflowWatchPalette.coral, lineWidth: 1.5)
                }
                .foregroundStyle(DayflowWatchPalette.coral)
                .accessibilityLabel(session.secondaryAction)
            }
        }
    }

    private var primarySymbol: String {
        session.state.localizedCaseInsensitiveContains("Aktiv") ? "pause.fill" : "play.fill"
    }

    private var isPaused: Bool {
        session.state.localizedCaseInsensitiveContains("Pause")
    }

    private var heroKicker: String {
        if isPaused { return "PAUSENZEIT" }
        if session.state.localizedCaseInsensitiveContains("Fertig") { return "GEARBEITET" }
        if session.state.localizedCaseInsensitiveContains("Aktiv") { return "NOCH BIS FEIERABEND" }
        return "BEREIT FÜR HEUTE"
    }

    private var progressPercentage: Int {
        Int((session.progress.clamped(to: 0...1) * 100).rounded())
    }

    private var tone: DayflowWatchTone {
        if isPaused { return .paused }
        if session.state.localizedCaseInsensitiveContains("Fertig") { return .finished }
        return .active
    }
}

private struct DayflowWatchTone {
    let background: Color
    let content: Color
    let pillBackground: Color
    let pillContent: Color

    static let active = DayflowWatchTone(
        background: DayflowWatchPalette.coral,
        content: .white,
        pillBackground: DayflowWatchPalette.successSoft,
        pillContent: DayflowWatchPalette.success
    )
    static let paused = DayflowWatchTone(
        background: DayflowWatchPalette.sand,
        content: DayflowWatchPalette.navy,
        pillBackground: DayflowWatchPalette.background.opacity(0.82),
        pillContent: DayflowWatchPalette.navy
    )
    static let finished = DayflowWatchTone(
        background: DayflowWatchPalette.green,
        content: DayflowWatchPalette.navy,
        pillBackground: DayflowWatchPalette.background.opacity(0.82),
        pillContent: DayflowWatchPalette.success
    )
}

private enum DayflowWatchPalette {
    static let background = Color(red: 1.000, green: 0.980, blue: 0.949)
    static let panel = Color.white
    static let softMuted = Color(red: 0.925, green: 0.882, blue: 0.827)
    static let navy = Color(red: 0.063, green: 0.145, blue: 0.267)
    static let coral = Color(red: 1.000, green: 0.365, blue: 0.314)
    static let success = Color(red: 0.247, green: 0.482, blue: 0.345)
    static let successSoft = Color(red: 0.875, green: 0.965, blue: 0.914)
    static let sand = Color(red: 0.957, green: 0.843, blue: 0.643)
    static let green = Color(red: 0.400, green: 0.847, blue: 0.718)
}

private extension Comparable {
    func clamped(to limits: ClosedRange<Self>) -> Self {
        min(max(self, limits.lowerBound), limits.upperBound)
    }
}

#Preview {
    WatchContentView(session: WatchSessionModel())
}
