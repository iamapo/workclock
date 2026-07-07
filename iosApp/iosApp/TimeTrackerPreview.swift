import ComposeApp
import SwiftUI
import UIKit

struct TimeTrackerPreviewView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.PreviewViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview("iOS Arbeitszeit Dashboard") {
    TimeTrackerPreviewView()
        .ignoresSafeArea()
}
