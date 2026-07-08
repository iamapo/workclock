package com.iamapo.timetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.ui.components.BottomNavigationBar
import com.iamapo.timetracker.ui.components.MainTab
import com.iamapo.timetracker.ui.screens.CalendarEditorScreen
import com.iamapo.timetracker.ui.screens.SettingsScreen
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
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
        var activeTab by remember { mutableStateOf(MainTab.Today) }
        var selectedCalendarDate by remember { mutableStateOf(state.calendarDays.firstOrNull { it.isToday }?.date) }

        TimeTrackerTheme {
            Scaffold(
                containerColor = com.iamapo.timetracker.ui.theme.AppColors.Background,
                bottomBar = {
                    BottomNavigationBar(
                        selectedTab = activeTab,
                        onSelectTab = { tab ->
                            if (tab == MainTab.Calendar && selectedCalendarDate == null) {
                                selectedCalendarDate = state.calendarDays.firstOrNull { it.isToday }?.date
                                    ?: state.calendarDays.firstOrNull()?.date
                            }
                            activeTab = tab
                        }
                    )
                }
            ) { paddingValues ->
                when (activeTab) {
                    MainTab.Today -> {
                        TimeTrackerScreen(
                            state = state,
                            onPrimaryAction = viewModel::onPrimaryAction,
                            onSecondaryAction = viewModel::onSecondaryAction,
                            onDecreaseRequiredBreak = viewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = viewModel::increaseRequiredBreak,
                            onOpenCalendar = {
                                selectedCalendarDate = state.calendarDays.firstOrNull { it.isToday }?.date
                                    ?: state.calendarDays.firstOrNull()?.date
                                activeTab = MainTab.Calendar
                            },
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Calendar -> {
                        CalendarEditorScreen(
                            state = state,
                            selectedDate = selectedCalendarDate
                                ?: state.calendarDays.firstOrNull { it.isToday }?.date
                                ?: state.calendarDays.first().date,
                            onSelectDate = { selectedCalendarDate = it },
                            onIncreaseDay = viewModel::increaseCalendarDay,
                            onDecreaseDay = viewModel::decreaseCalendarDay,
                            onVacation = viewModel::setCalendarDayVacation,
                            onSick = viewModel::setCalendarDaySick,
                            onClear = viewModel::clearCalendarDay,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Settings -> {
                        SettingsScreen(
                            state = state,
                            onDecreaseRequiredBreak = viewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = viewModel::increaseRequiredBreak,
                            onDeleteAllEntries = viewModel::deleteAllEntries,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Route - Bottom Navigation",
    showBackground = true,
    backgroundColor = 0xFF07080D,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerRoutePreview() {
    TimeTrackerRoute()
}
