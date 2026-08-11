import AppKit
import SwiftUI
import WorkClockCore

@MainActor
final class AppDelegate: NSObject, NSApplicationDelegate {
    static weak var model: WorkClockModel?

    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApplication.shared.registerForRemoteNotifications()
    }

    func application(
        _ application: NSApplication,
        didReceiveRemoteNotification userInfo: [String: Any]
    ) {
        Task { await Self.model?.sync() }
    }
}

@main
struct WorkClockMacApp: App {
    @NSApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var model: WorkClockModel

    init() {
        let model = WorkClockModel()
        _model = StateObject(wrappedValue: model)
        AppDelegate.model = model
    }

    var body: some Scene {
        MenuBarExtra(model.elapsedText, systemImage: menuIcon) {
            MenuContent(model: model)
        }
        .menuBarExtraStyle(.window)

        Window("WorkClock", id: "main") {
            MainWindowContent(model: model)
        }
    }

    private var menuIcon: String {
        model.day.status == WorkStatus.working ? "timer" : "clock"
    }
}

private struct DayControls: View {
    @ObservedObject var model: WorkClockModel

    var body: some View {
        HStack {
            Button(model.primaryTitle) { model.primary() }
            if model.isRunning {
                Button("Tag beenden") { model.stop() }
            }
            if model.isFinished {
                Button("Tag fortsetzen") { model.reopen() }
            }
        }
    }
}

private struct SyncStatus: View {
    @ObservedObject var model: WorkClockModel

    var body: some View {
        Text(model.syncText)
            .font(.caption)
            .foregroundStyle(.secondary)
        if let error = model.syncError {
            Text(error)
                .font(.caption2)
                .foregroundStyle(.red)
                .textSelection(.enabled)
        }
    }
}

private struct MenuContent: View {
    @ObservedObject var model: WorkClockModel
    @Environment(\.openWindow) private var openWindow

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(model.statusText).font(.headline)
            Text(model.elapsedText).font(.system(.title, design: .monospaced))
            DayControls(model: model)
            Divider()
            SyncStatus(model: model)
            Button("Jetzt synchronisieren") { Task { await model.sync() } }
                .disabled(model.isSyncing)
            Button("Tag bearbeiten") {
                openWindow(id: "main")
                NSApp.activate(ignoringOtherApps: true)
            }
            Button("Beenden") { NSApp.terminate(nil) }
        }
        .padding()
        .frame(width: 280)
    }
}

private struct MainWindowContent: View {
    @ObservedObject var model: WorkClockModel

    var body: some View {
        VStack(spacing: 18) {
            Text(model.statusText).font(.title2)
            Text(model.elapsedText)
                .font(.system(size: 52, weight: .semibold, design: .monospaced))
            DayControls(model: model)
            if !model.day.events.isEmpty {
                Form {
                    ForEach(model.day.events.indices, id: \.self) { index in
                        EventRow(model: model, index: index)
                    }
                }
                .formStyle(.grouped)
            }
            SyncStatus(model: model)
        }
        .padding(32)
        .frame(minWidth: 420, minHeight: 280)
    }
}

private struct EventRow: View {
    @ObservedObject var model: WorkClockModel
    let index: Int

    var body: some View {
        DatePicker(
            (model.day.events[index] as? WorkEvent)?.title ?? "",
            selection: Binding(
                get: { model.eventDate(at: index) },
                set: { date in model.changeEvent(at: index, to: date) }
            ),
            displayedComponents: .hourAndMinute
        )
    }
}
