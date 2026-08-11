package com.iamapo.timetracker

import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.app.IosWorkClockContainer
import com.iamapo.timetracker.data.sync.WorkDaySync
import com.iamapo.timetracker.domain.sync.CloudReplica
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
import platform.UIKit.UIViewController
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private var activeWorkDaySync: WorkDaySync? = null
private var workDaySyncJob: Job? = null
private val syncScope = MainScope()

private val watchSessionController = IosWatchSessionController()

fun activateWatchSession() {
    watchSessionController.activate()
}

fun MainViewController(): UIViewController =
    MainViewController(NoOpLockScreenStatusController, NoOpReminderScheduler)

fun MainViewController(
    lockScreenStatusController: LockScreenStatusController,
    reminderScheduler: ReminderScheduler
): UIViewController = MainViewController(
    lockScreenStatusController,
    reminderScheduler,
    cloudReplica = null
)

fun MainViewController(
    lockScreenStatusController: LockScreenStatusController,
    reminderScheduler: ReminderScheduler,
    cloudReplica: CloudReplica?
): UIViewController {
    val container = IosWorkClockContainer(
        lockScreenStatusController = lockScreenStatusController,
        reminderScheduler = reminderScheduler,
        cloudReplica = cloudReplica
    )
    val workDaySync = container.workDaySync
    activeWorkDaySync = workDaySync
    workDaySyncJob?.cancel()
    workDaySyncJob = workDaySync?.let { sync ->
        syncScope.launch {
            sync.run()
        }
    }
    var rootController: UIViewController? = null
    container.setPresenter { rootController }

    rootController = ComposeUIViewController {
        TimeTrackerRoute(
            container = container,
            onViewModelReady = { viewModel ->
                watchSessionController.attachHandlers(
                    onCommand = viewModel::onWatchCommand,
                    onEvent = viewModel::onWatchEvent
                )
            },
            onStateChanged = watchSessionController::publish
        )
    }
    return rootController!!
}

fun requestTimeTrackerTab() {
    DeepLinkRouter.requestTimeTrackerTab()
}

fun syncWorkClock(completion: (Boolean) -> Unit) {
    val workDaySync = activeWorkDaySync
    if (workDaySync == null) {
        completion(false)
        return
    }
    syncScope.launch {
        completion(workDaySync.sync())
    }
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
