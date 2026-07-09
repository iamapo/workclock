import SwiftUI

struct WatchContentView: View {
    @ObservedObject var session: WatchSessionModel

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.11, green: 0.09, blue: 0.08),
                    Color(red: 0.02, green: 0.02, blue: 0.04),
                    Color(red: 0.03, green: 0.08, blue: 0.11)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(spacing: 9) {
                HStack(spacing: 6) {
                    Circle()
                        .fill(session.isReachable ? Self.mint : Self.lemon)
                        .frame(width: 7, height: 7)
                    Text("WorkClock")
                        .font(.caption2)
                        .fontWeight(.black)
                        .foregroundStyle(.white.opacity(0.78))
                }

                ZStack {
                    Circle()
                        .stroke(.white.opacity(0.13), lineWidth: 12)
                    Circle()
                        .trim(from: 0, to: ringProgress)
                        .stroke(Self.mint, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                        .rotationEffect(.degrees(-90))

                    VStack(spacing: 3) {
                        Text(session.remaining)
                            .font(.system(size: 36, weight: .black, design: .rounded))
                            .foregroundStyle(.white)
                            .minimumScaleFactor(0.54)
                            .lineLimit(1)
                        Text(session.caption)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundStyle(.white.opacity(0.72))
                            .lineLimit(2)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 18)
                }
                .frame(width: 136, height: 136)

                HStack(spacing: 12) {
                    Button {
                        session.sendPrimaryAction()
                    } label: {
                        Image(systemName: primarySymbol)
                            .font(.system(size: 14, weight: .black))
                            .frame(width: 42, height: 42)
                    }
                    .buttonStyle(.plain)
                    .background(Self.lemon, in: Circle())
                    .foregroundStyle(Color(red: 0.09, green: 0.08, blue: 0.12))
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
                        .background(.white.opacity(0.13), in: Circle())
                        .foregroundStyle(.white)
                        .accessibilityLabel(session.secondaryAction)
                    }
                }

                HStack {
                    Text(session.state)
                    Spacer(minLength: 8)
                    Text(statusPercent)
                }
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundStyle(.white.opacity(0.72))
            }
            .padding(.horizontal, 14)
        }
    }

    private var primarySymbol: String {
        if session.primaryAction.localizedCaseInsensitiveContains("Pause") {
            return "pause.fill"
        }
        return "play.fill"
    }

    private var ringProgress: Double {
        if session.state.localizedCaseInsensitiveContains("Fertig") {
            return 1
        }
        if session.state.localizedCaseInsensitiveContains("Bereit") {
            return 0.08
        }
        if session.state.localizedCaseInsensitiveContains("Pause") {
            return 0.48
        }
        return 0.65
    }

    private var statusPercent: String {
        "\(Int((ringProgress * 100).rounded()))%"
    }

    private static let mint = Color(red: 0.40, green: 0.87, blue: 0.71)
    private static let lemon = Color(red: 1.0, green: 0.85, blue: 0.30)
}

#Preview {
    WatchContentView(session: WatchSessionModel())
}
