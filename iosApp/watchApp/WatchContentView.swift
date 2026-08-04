import SwiftUI

struct WatchContentView: View {
    @ObservedObject var session: WatchSessionModel

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.118, green: 0.165, blue: 0.239),
                    Color(red: 0.055, green: 0.086, blue: 0.129)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 12) {
                HStack(spacing: 6) {
                    Circle()
                        .fill(accentColor)
                        .frame(width: 7, height: 7)
                    Text("WORKCLOCK")
                        .font(.system(.caption2, design: .monospaced))
                        .fontWeight(.black)
                        .tracking(0.6)
                        .foregroundStyle(paper.opacity(0.75))
                }

                ZStack {
                    Circle()
                        .stroke(paper.opacity(0.12), lineWidth: 11)
                    Circle()
                        .trim(from: 0, to: session.progress.clamped(to: 0...1))
                        .stroke(accentColor, style: StrokeStyle(lineWidth: 11, lineCap: .round))
                        .rotationEffect(.degrees(-90))

                    VStack(spacing: 2) {
                        if isPaused {
                            Text("PAUSE")
                                .font(.system(.caption2, design: .monospaced))
                                .fontWeight(.bold)
                                .foregroundStyle(paper.opacity(0.65))
                            TimelineView(.periodic(from: .now, by: 1)) { context in
                                Text(session.breakDuration(at: context.date))
                                    .font(.system(size: 27, weight: .black, design: .serif))
                                    .foregroundStyle(paper)
                                    .monospacedDigit()
                            }
                        } else {
                            Text(session.remaining)
                                .font(.system(size: 15, weight: .black, design: .serif))
                                .foregroundStyle(paper)
                                .minimumScaleFactor(0.6)
                                .lineLimit(2)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .frame(width: 138, height: 138)

                HStack(spacing: 12) {
                    Button {
                        session.sendPrimaryAction()
                    } label: {
                        Image(systemName: primarySymbol)
                            .font(.system(size: 14, weight: .black))
                            .frame(width: 42, height: 42)
                    }
                    .buttonStyle(.plain)
                    .background(accentColor, in: Circle())
                    .foregroundStyle(ink)
                    .accessibilityLabel(session.primaryAction)

                    if !session.secondaryAction.isEmpty {
                        Button {
                            session.sendEndDay()
                        } label: {
                            Image(systemName: "stop.fill")
                                .font(.system(size: 12, weight: .black))
                                .frame(width: 42, height: 42)
                        }
                        .buttonStyle(.plain)
                        .background(paper.opacity(0.14), in: Circle())
                        .foregroundStyle(paper)
                        .accessibilityLabel(session.secondaryAction)
                    }
                }
            }
            .padding(.horizontal, 14)
        }
    }

    private var primarySymbol: String {
        if session.state.localizedCaseInsensitiveContains("Aktiv") {
            return "pause.fill"
        }
        return "play.fill"
    }

    private var isPaused: Bool {
        session.state.localizedCaseInsensitiveContains("Pause")
    }

    private var accentColor: Color {
        switch session.state {
        case "Aktiv":
            return Self.plum
        case "Pause":
            return Self.ochre
        case "Fertig":
            return Self.blue
        default:
            return Self.green
        }
    }

    // Ledger palette — mirrors core/design AppColors.kt so the watch reads as the same book.
    private var paper: Color { Self.paper }
    private var ink: Color { Self.ink }
    private static let paper = Color(red: 0.918, green: 0.941, blue: 0.898)
    private static let ink = Color(red: 0.118, green: 0.165, blue: 0.239)
    private static let green = Color(red: 0.290, green: 0.420, blue: 0.243)
    private static let ochre = Color(red: 0.659, green: 0.471, blue: 0.122)
    private static let plum = Color(red: 0.478, green: 0.310, blue: 0.561)
    private static let blue = Color(red: 0.184, green: 0.353, blue: 0.659)
}

private extension Comparable {
    func clamped(to limits: ClosedRange<Self>) -> Self {
        min(max(self, limits.lowerBound), limits.upperBound)
    }
}

#Preview {
    WatchContentView(session: WatchSessionModel())
}
