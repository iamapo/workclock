import Foundation
import WatchConnectivity

final class WatchSessionModel: NSObject, ObservableObject {
    @Published private(set) var state = "Bereit"
    @Published private(set) var remaining = "0:00"
    @Published private(set) var caption = "iPhone verbinden"
    @Published private(set) var primaryAction = "Tag starten"
    @Published private(set) var secondaryAction = ""
    @Published private(set) var command = "primary"
    @Published private(set) var isReachable = false

    private let session: WCSession?

    override init() {
        if WCSession.isSupported() {
            session = WCSession.default
        } else {
            session = nil
        }
        super.init()
        session?.delegate = self
        session?.activate()
    }

    func sendPrimaryAction() {
        send(command)
    }

    func sendEndDay() {
        send("endDay")
    }

    private func send(_ command: String) {
        guard let session else { return }
        let message = ["command": command]

        if session.isReachable {
            session.sendMessage(message, replyHandler: nil, errorHandler: nil)
        } else {
            session.transferUserInfo(message)
        }
    }

    private func apply(_ payload: [String: Any]) {
        DispatchQueue.main.async {
            self.state = payload["state"] as? String ?? self.state
            self.remaining = payload["remaining"] as? String ?? self.remaining
            self.caption = payload["caption"] as? String ?? self.caption
            self.primaryAction = payload["primaryAction"] as? String ?? self.primaryAction
            self.secondaryAction = payload["secondaryAction"] as? String ?? self.secondaryAction
            self.command = payload["command"] as? String ?? self.command
            self.isReachable = self.session?.isReachable ?? false
        }
    }
}

extension WatchSessionModel: WCSessionDelegate {
    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        DispatchQueue.main.async {
            self.isReachable = session.isReachable
        }
    }

    func sessionReachabilityDidChange(_ session: WCSession) {
        DispatchQueue.main.async {
            self.isReachable = session.isReachable
        }
    }

    func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        apply(message)
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        apply(applicationContext)
    }

    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any] = [:]) {
        apply(userInfo)
    }
}
