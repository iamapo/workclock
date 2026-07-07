package com.iamapo.timetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TimeTrackerRoute {
    @Composable
    operator fun invoke(
        workDayStore: WorkDayStore = NoOpWorkDayStore,
        viewModel: TimeTrackerViewModel = remember {
            TimeTrackerViewModel(workDayStore = workDayStore)
        }
    ) {
        val state by viewModel.uiState.collectAsState()

        TimeTrackerTheme {
            TimeTrackerScreen(
                state = state,
                onPrimaryAction = viewModel::onPrimaryAction,
                onSecondaryAction = viewModel::onSecondaryAction
            )
        }
    }
}
