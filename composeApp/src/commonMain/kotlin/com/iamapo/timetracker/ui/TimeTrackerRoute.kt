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
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
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
        viewModel: TimeTrackerViewModel? = null,
        lockScreenStatusController: LockScreenStatusController = NoOpLockScreenStatusController,
        onStateChanged: (TimeTrackerUiState) -> Unit = {}
    ) {
        val resolvedViewModel = viewModel ?: remember(workDayStore, lockScreenStatusController) {
            val timeProvider = SystemTimeProvider()
            TimeTrackerViewModel(
                timeProvider = timeProvider,
                repository = PersistedWorkHistoryRepository(
                    store = workDayStore,
                    today = timeProvider.now().date
                ),
                lockScreenStatusController = lockScreenStatusController
            )
        }
        val state by resolvedViewModel.uiState.collectAsState()
        var activeTab by remember { mutableStateOf(MainTab.Today) }
        var selectedCalendarDate by remember { mutableStateOf(state.calendarDays.firstOrNull { it.isToday }?.date) }

        androidx.compose.runtime.LaunchedEffect(state) {
            onStateChanged(state)
        }

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
                            onPrimaryAction = resolvedViewModel::onPrimaryAction,
                            onSecondaryAction = resolvedViewModel::onSecondaryAction,
                            onDecreaseRequiredBreak = resolvedViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = resolvedViewModel::increaseRequiredBreak,
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
                            onIncreaseDay = resolvedViewModel::increaseCalendarDay,
                            onDecreaseDay = resolvedViewModel::decreaseCalendarDay,
                            onVacation = resolvedViewModel::setCalendarDayVacation,
                            onSick = resolvedViewModel::setCalendarDaySick,
                            onClear = resolvedViewModel::clearCalendarDay,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Settings -> {
                        SettingsScreen(
                            state = state,
                            onDecreaseRequiredBreak = resolvedViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = resolvedViewModel::increaseRequiredBreak,
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
    backgroundColor = 0xFF07080D,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerRoutePreview() {
    TimeTrackerRoute()
}
