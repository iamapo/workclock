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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.presentation.CalendarViewModel
import com.iamapo.timetracker.presentation.AppCalendarStateMapper
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
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
        lockScreenStatusController: LockScreenStatusController = NoOpLockScreenStatusController,
        onViewModelReady: (TimeTrackerViewModel) -> Unit = {},
        onStateChanged: (TimeTrackerUiState) -> Unit = {}
    ) {
        val timeProvider = remember { SystemTimeProvider() }
        val repository = remember(workDayStore) {
            PersistedWorkHistoryRepository(store = workDayStore, today = timeProvider.now().date)
        }
        val resolvedViewModel = viewModel {
            TimeTrackerViewModel(
                timeProvider = timeProvider,
                repository = repository,
                lockScreenStatusController = lockScreenStatusController
            )
        }
        val resolvedCalendarViewModel = viewModel {
            CalendarViewModel(repository, timeProvider, AppCalendarStateMapper)
        }
        val state by resolvedViewModel.uiState.collectAsState()
        val calendarState by resolvedCalendarViewModel.uiState.collectAsState()
        var activeTab by remember { mutableStateOf(MainTab.Today) }
        var selectedCalendarDate by remember { mutableStateOf(calendarState.days.firstOrNull { it.isToday }?.date) }

        androidx.compose.runtime.LaunchedEffect(state) {
            onStateChanged(state)
        }
        androidx.compose.runtime.LaunchedEffect(resolvedViewModel) {
            onViewModelReady(resolvedViewModel)
        }

        TimeTrackerTheme {
            Scaffold(
                containerColor = com.iamapo.timetracker.ui.theme.AppColors.Background,
                bottomBar = {
                    BottomNavigationBar(
                        selectedTab = activeTab,
                        onSelectTab = { tab ->
                            if (tab == MainTab.Calendar && selectedCalendarDate == null) {
                                selectedCalendarDate = calendarState.days.firstOrNull { it.isToday }?.date
                                    ?: calendarState.days.firstOrNull()?.date
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
                            onPrimaryAction = resolvedViewModel::onPrimaryAction,
                            onSecondaryAction = resolvedViewModel::onSecondaryAction,
                            onDecreaseRequiredBreak = resolvedViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = resolvedViewModel::increaseRequiredBreak,
                            onOpenCalendar = {
                                selectedCalendarDate = calendarState.days.firstOrNull { it.isToday }?.date
                                    ?: calendarState.days.firstOrNull()?.date
                                activeTab = MainTab.Calendar
                            },
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Calendar -> {
                        CalendarEditorScreen(
                            state = calendarState,
                            selectedDate = selectedCalendarDate
                                ?: calendarState.days.firstOrNull { it.isToday }?.date
                                ?: calendarState.days.first().date,
                            onSelectDate = { selectedCalendarDate = it },
                            onIncreaseDay = resolvedCalendarViewModel::increaseDay,
                            onDecreaseDay = resolvedCalendarViewModel::decreaseDay,
                            onVacation = resolvedCalendarViewModel::setVacation,
                            onSick = resolvedCalendarViewModel::setSick,
                            onForgottenWorkDay = resolvedCalendarViewModel::setForgottenWorkDay,
                            onClear = resolvedCalendarViewModel::clearDay,
                            onSetWorkTimes = resolvedCalendarViewModel::setWorkTimes,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Settings -> {
                        SettingsScreen(
                            state = state,
                            onDecreaseDailyTarget = resolvedViewModel::decreaseDailyTarget,
                            onIncreaseDailyTarget = resolvedViewModel::increaseDailyTarget,
                            onDecreaseRequiredBreak = resolvedViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = resolvedViewModel::increaseRequiredBreak,
                            onDecreaseWeeklyTarget = resolvedViewModel::decreaseWeeklyTarget,
                            onIncreaseWeeklyTarget = resolvedViewModel::increaseWeeklyTarget,
                            onLockScreenStatusChanged = resolvedViewModel::setLockScreenStatusEnabled,
                            onDeleteAllEntries = resolvedViewModel::deleteAllEntries,
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
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerRoutePreview() {
    TimeTrackerRoute()
}
