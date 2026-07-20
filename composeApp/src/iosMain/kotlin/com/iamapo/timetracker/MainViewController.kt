package com.iamapo.timetracker

import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.backup.IosBackupFileController
import com.iamapo.timetracker.data.IosWorkDayStore
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.TimeTrackerRoute
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.watch.IosWatchSessionController
import com.iamapo.timetracker.app.createWorkClockDependencies
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = MainViewController(NoOpLockScreenStatusController)

fun MainViewController(lockScreenStatusController: LockScreenStatusController): UIViewController {
    var rootController: UIViewController? = null
    val backupFileController = IosBackupFileController { rootController }
    val dependencies = createWorkClockDependencies(
        workDayStore = IosWorkDayStore(),
        backupFileController = backupFileController,
        lockScreenStatusController = lockScreenStatusController
    )

    rootController = ComposeUIViewController {
        var watchSession by remember { mutableStateOf<IosWatchSessionController?>(null) }

        TimeTrackerRoute(
            dependencies = dependencies,
            onViewModelReady = { viewModel ->
                if (watchSession == null) {
                    watchSession = IosWatchSessionController(
                        onCommand = viewModel::onWatchCommand,
                        onEvent = viewModel::onWatchEvent
                    ).also { it.activate() }
                }
            },
            onStateChanged = { state -> watchSession?.publish(state) }
        )
    }
    return rootController
}

fun PreviewViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState,
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
