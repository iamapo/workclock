import SwiftUI

struct WatchContentView: View {
    @ObservedObject var session: WatchSessionModel

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 6) {
                Circle()
                    .fill(session.isReachable ? Color.green : Color.orange)
                    .frame(width: 7, height: 7)
                Text("WorkClock")
                    .font(.caption2)
                    .fontWeight(.black)
                    .foregroundStyle(.secondary)
            }

            Text(session.state)
                .font(.caption)
                .fontWeight(.bold)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.green.opacity(0.18), in: Capsule())
                .foregroundStyle(.green)

            Text(session.remaining)
                .font(.system(size: 38, weight: .black, design: .rounded))
                .minimumScaleFactor(0.6)
                .lineLimit(1)

            Text(session.caption)
                .font(.caption2)
                .fontWeight(.semibold)
                .foregroundStyle(.secondary)
                .lineLimit(2)
                .multilineTextAlignment(.center)

            VStack(spacing: 6) {
                Button(session.primaryAction) {
                    session.sendPrimaryAction()
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.small)

                if !session.secondaryAction.isEmpty {
                    Button(session.secondaryAction) {
                        session.sendEndDay()
                    }
                    .buttonStyle(.bordered)
                    .controlSize(.small)
                }
            }
        }
        .padding(.horizontal, 8)
    }
}

#Preview {
    WatchContentView(session: WatchSessionModel())
}
