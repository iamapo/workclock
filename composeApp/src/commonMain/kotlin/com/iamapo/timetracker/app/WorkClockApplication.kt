package com.iamapo.timetracker.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.TimeTrackerRoute
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun WorkClockApplication(
    platformDependencies: WorkClockPlatformDependencies,
    onViewModelReady: (TimeTrackerViewModel) -> Unit = {},
    onStateChanged: (TimeTrackerUiState) -> Unit = {}
) {
    val appModules = remember(platformDependencies) {
        workClockModules(platformDependencies)
    }

    KoinApplication(
        configuration = koinConfiguration {
            modules(appModules)
        }
    ) {
        TimeTrackerRoute(
            onViewModelReady = onViewModelReady,
            onStateChanged = onStateChanged
        )
    }
}
