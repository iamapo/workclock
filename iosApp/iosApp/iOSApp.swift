import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.activateWatchSession()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea()
                .onOpenURL { url in
                    if url.scheme == "workclock", url.host == "timetracker" {
                        MainViewControllerKt.requestTimeTrackerTab()
                    }
                }
        }
    }
}
