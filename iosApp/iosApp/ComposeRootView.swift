import ComposeApp
import SwiftUI
import UIKit

struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            lockScreenStatusController: IosLockScreenStatusController()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
