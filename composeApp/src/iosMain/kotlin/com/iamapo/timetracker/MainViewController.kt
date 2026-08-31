package com.iamapo.timetracker

import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.app.IosWorkClockContainer
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.DeepLinkRouter
import com.iamapo.timetracker.ui.TimeTrackerRoute
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.watch.IosWatchSessionController
import platform.UIKit.UIViewController

private val watchSessionController = IosWatchSessionController()
private val container = IosWorkClockContainer()

fun activateWatchSession() {
    watchSessionController.activate()
}

fun MainViewController(): UIViewController {
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

fun PreviewViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState(),
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}