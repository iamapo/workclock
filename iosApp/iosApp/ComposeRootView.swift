import ComposeApp
import SwiftUI
import UIKit

struct ComposeRootView: UIViewControllerRepresentable {
    let cloudReplica: CloudKitDayReplica

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            lockScreenStatusController: IosLockScreenStatusController(),
            reminderScheduler: IosReminderScheduler(),
            cloudReplica: cloudReplica
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
