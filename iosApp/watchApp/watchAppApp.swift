import SwiftUI

@main
struct watchAppApp: App {
    @StateObject private var session = WatchSessionModel()

    var body: some Scene {
        WindowGroup {
            WatchContentView(session: session)
        }
    }
}
