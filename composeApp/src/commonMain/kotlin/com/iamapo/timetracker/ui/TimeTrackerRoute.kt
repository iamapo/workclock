package com.iamapo.timetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamapo.timetracker.app.WorkClockDependencies
import com.iamapo.timetracker.backup.rememberBackupStateHolder
import com.iamapo.timetracker.lockscreen.LockScreenStatusCoordinator
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.presentation.CalendarViewModel
import com.iamapo.timetracker.presentation.SettingsViewModel
import com.iamapo.timetracker.presentation.AppCalendarStateMapper
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.components.BottomNavigationBar
import com.iamapo.timetracker.ui.components.CalendarPanel
import com.iamapo.timetracker.ui.components.MainTab
import com.iamapo.timetracker.ui.screens.CalendarEditorScreen
import com.iamapo.timetracker.ui.screens.SettingsScreen
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TimeTrackerRoute {
    @Composable
    operator fun invoke(
        dependencies: WorkClockDependencies,
        onViewModelReady: (TimeTrackerViewModel) -> Unit = {},
        onStateChanged: (TimeTrackerUiState) -> Unit = {}
    ) {
        val timeProvider = dependencies.timeProvider
        val repository = dependencies.repository
        val resolvedViewModel = viewModel {
            TimeTrackerViewModel(
                timeProvider = timeProvider,
                repository = repository
            )
        }
        val resolvedCalendarViewModel = viewModel {
            CalendarViewModel(repository, timeProvider, AppCalendarStateMapper)
        }
        val resolvedSettingsViewModel = viewModel {
            SettingsViewModel(repository, timeProvider)
        }
        val state by resolvedViewModel.uiState.collectAsState()
        val calendarState by resolvedCalendarViewModel.uiState.collectAsState()
        val settingsState by resolvedSettingsViewModel.uiState.collectAsState()
        var activeTab by remember { mutableStateOf(MainTab.Today) }
        var selectedCalendarDate by remember { mutableStateOf(calendarState.days.firstOrNull { it.isToday }?.date) }
        val backupStateHolder = rememberBackupStateHolder(
            workDayStore = dependencies.workDayStore,
            repository = repository,
            timeProvider = timeProvider,
            backupFileController = dependencies.backupFileController
        )

        androidx.compose.runtime.LaunchedEffect(state) {
            onStateChanged(state)
        }
        androidx.compose.runtime.LaunchedEffect(resolvedViewModel) {
            onViewModelReady(resolvedViewModel)
        }
        androidx.compose.runtime.LaunchedEffect(repository, dependencies.lockScreenStatusController) {
            LockScreenStatusCoordinator(repository, timeProvider, dependencies.lockScreenStatusController).run()
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
                            calendarContent = {
                                CalendarPanel(
                                    monthTitle = calendarState.monthTitle,
                                    days = calendarState.previewDays,
                                    plannedWeek = calendarState.plannedWeek,
                                    reachedWeek = calendarState.reachedWeek,
                                    onOpenCalendar = {
                                        selectedCalendarDate = calendarState.days.firstOrNull { it.isToday }?.date
                                            ?: calendarState.days.firstOrNull()?.date
                                        activeTab = MainTab.Calendar
                                    }
                                )
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
                            state = settingsState,
                            onDecreaseRequiredBreak = resolvedSettingsViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = resolvedSettingsViewModel::increaseRequiredBreak,
                            onLockScreenStatusChanged = resolvedSettingsViewModel::setLockScreenStatusEnabled,
                            onDecreaseWeekdayTarget = resolvedSettingsViewModel::decreaseWeekdayTarget,
                            onIncreaseWeekdayTarget = resolvedSettingsViewModel::increaseWeekdayTarget,
                            onAutomaticHolidaysChanged = resolvedSettingsViewModel::setAutomaticHolidaysEnabled,
                            onHolidayFederalStateChanged = resolvedSettingsViewModel::setHolidayFederalState,
                            backupStatus = backupStateHolder.status,
                            pendingBackupImport = backupStateHolder.pendingImport,
                            canUndoImport = backupStateHolder.canUndoImport,
                            onExportBackup = backupStateHolder::exportBackup,
                            onImportBackup = backupStateHolder::importBackup,
                            onCancelImport = backupStateHolder::cancelImport,
                            onConfirmImport = backupStateHolder::confirmImport,
                            onUndoImport = backupStateHolder::undoImport,
                            onDeleteAllEntries = resolvedSettingsViewModel::deleteAllEntries,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
