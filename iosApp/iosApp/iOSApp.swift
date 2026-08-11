import ComposeApp
import SwiftUI
import UIKit

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        application.registerForRemoteNotifications()
        return true
    }

    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        MainViewControllerKt.syncWorkClock { changed in
            completionHandler(changed.boolValue ? .newData : .noData)
        }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    private let cloudReplica = CloudKitDayReplica()

    init() {
        MainViewControllerKt.activateWatchSession()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(cloudReplica: cloudReplica)
                .ignoresSafeArea()
                .task {
                    MainViewControllerKt.syncWorkClock { success in
                    }
                }
                .onChange(of: scenePhase) { phase in
                    if phase == .active {
                        MainViewControllerKt.syncWorkClock { success in
                        }
                    }
                }
                .onOpenURL { url in
                    if url.scheme == "workclock", url.host == "timetracker" {
                        MainViewControllerKt.requestTimeTrackerTab()
                    }
                }
        }
    }
}
