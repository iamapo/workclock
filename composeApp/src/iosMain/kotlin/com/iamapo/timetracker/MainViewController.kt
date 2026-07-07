package com.iamapo.timetracker

import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.TimeTrackerRoute
import com.iamapo.timetracker.ui.components.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerRoute()
}

fun PreviewViewController(): UIViewController = ComposeUIViewController {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState,
            onPrimaryAction = {},
            onSecondaryAction = {}
        )
    }
}
