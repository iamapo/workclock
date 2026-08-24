package com.iamapo.timetracker

import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.backup.IosBackupFileController
import com.iamapo.timetracker.data.IosWorkDayStore
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import com.iamapo.timetracker.ui.DeepLinkRouter
import com.iamapo.timetracker.ui.TimeTrackerRoute
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.watch.IosWatchSessionController
import com.iamapo.timetracker.app.createWorkClockDependencies
import platform.UIKit.UIViewController

private val watchSessionController = IosWatchSessionController()

fun activateWatchSession() {
    watchSessionController.activate()
}

fun MainViewController(): UIViewController =
    MainViewController(NoOpLockScreenStatusController, NoOpReminderScheduler)

fun requestTimeTrackerTab() {
    DeepLinkRouter.requestTimeTrackerTab()
}

fun MainViewController(
    lockScreenStatusController: LockScreenStatusController,
    reminderScheduler: ReminderScheduler
): UIViewController {
    var rootController: UIViewController? = null
    val backupFileController = IosBackupFileController { rootController }
    val dependencies = createWorkClockDependencies(
        workDayStore = IosWorkDayStore(),
        backupFileController = backupFileController,
        lockScreenStatusController = lockScreenStatusController,
        reminderScheduler = reminderScheduler
    )

    rootController = ComposeUIViewController {
        TimeTrackerRoute(
            dependencies = dependencies,
            onViewModelReady = { viewModel ->
                watchSessionController.attachHandlers(
                    onCommand = viewModel::onWatchCommand,
                    onEvent = viewModel::onWatchEvent
                )
            },
            onStateChanged = watchSessionController::publish
        )
    }
    return rootController
}

fun PreviewViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState(),
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
