package com.iamapo.timetracker

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.data.IosWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.ui.TimeTrackerRoute
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.watch.IosWatchSessionController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = MainViewController(NoOpLockScreenStatusController)

fun MainViewController(lockScreenStatusController: LockScreenStatusController): UIViewController = ComposeUIViewController {
    val workDayStore = remember { IosWorkDayStore() }
    val timeProvider = remember { SystemTimeProvider() }
    val repository = remember {
        PersistedWorkHistoryRepository(
            store = workDayStore,
            today = timeProvider.now().date
        )
    }
    val viewModel = remember {
        TimeTrackerViewModel(
            timeProvider = timeProvider,
            repository = repository,
            lockScreenStatusController = lockScreenStatusController
        )
    }
    val watchSession = remember {
        IosWatchSessionController(onCommand = viewModel::onWatchCommand).also { it.activate() }
    }

    TimeTrackerRoute(
        workDayStore = workDayStore,
        viewModel = viewModel,
        onStateChanged = watchSession::publish
    )
}

fun PreviewViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState,
            onPrimaryAction = {},
            onSecondaryAction = {},
            onDecreaseRequiredBreak = {},
            onIncreaseRequiredBreak = {},
            onOpenCalendar = {}
        )
    }
}
