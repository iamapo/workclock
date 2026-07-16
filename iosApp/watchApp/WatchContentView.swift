import SwiftUI

struct WatchContentView: View {
    @ObservedObject var session: WatchSessionModel

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.10, green: 0.09, blue: 0.08),
                    Color(red: 0.02, green: 0.02, blue: 0.04)
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
                    Text("WorkClock")
                        .font(.caption2)
                        .fontWeight(.black)
                        .foregroundStyle(.white.opacity(0.8))
                }

                ZStack {
                    Circle()
                        .stroke(.white.opacity(0.10), lineWidth: 11)
                    Circle()
                        .trim(from: 0, to: ringProgress)
                        .stroke(accentColor, style: StrokeStyle(lineWidth: 11, lineCap: .round))
                        .rotationEffect(.degrees(-90))

                    VStack(spacing: 2) {
                        Text(centerText)
                            .font(.system(size: 15, weight: .black, design: .rounded))
                            .foregroundStyle(.white)
                            .minimumScaleFactor(0.6)
                            .lineLimit(2)
                            .multilineTextAlignment(.center)
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

    private var ringProgress: Double {
        if session.state.localizedCaseInsensitiveContains("Fertig") {
            return 1
        }
        if session.state.localizedCaseInsensitiveContains("Bereit") {
            return 0.08
        }
        if session.state.localizedCaseInsensitiveContains("Pause") {
            return 0.55
        }
        return 0.65
    }

    private var centerText: String {
        if session.state.localizedCaseInsensitiveContains("Pause") {
            return session.caption
        }
        return session.remaining
    }

    private var accentColor: Color {
        session.state.localizedCaseInsensitiveContains("Pause") ? Self.lemon : Self.mint
    }

    private static let mint = Color(red: 0.40, green: 0.87, blue: 0.71)
    private static let lemon = Color(red: 1.0, green: 0.85, blue: 0.30)
}

#Preview {
    WatchContentView(session: WatchSessionModel())
}
