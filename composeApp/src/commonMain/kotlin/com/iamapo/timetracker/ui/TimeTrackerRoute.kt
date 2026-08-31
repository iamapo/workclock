package com.iamapo.timetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iamapo.timetracker.app.WorkClockContainer
import com.iamapo.timetracker.backup.rememberBackupStateHolder
import com.iamapo.timetracker.lockscreen.LockScreenStatusCoordinator
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.presentation.CalendarViewModel
import com.iamapo.timetracker.presentation.SettingsViewModel
import com.iamapo.timetracker.presentation.AppCalendarStateMapper
import com.iamapo.timetracker.presentation.TimeTextFormatter
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.reminders.ReminderScheduleCoordinator
import com.iamapo.timetracker.report.rememberReportStateHolder
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.undo
import com.iamapo.timetracker.resources.workday_finished_at_message
import com.iamapo.timetracker.ui.components.BottomNavigationBar
import com.iamapo.timetracker.ui.components.MainTab
import com.iamapo.timetracker.ui.screens.CalendarEditorScreen
import com.iamapo.timetracker.ui.screens.SettingsScreen
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

object TimeTrackerRoute {
    @Composable
    operator fun invoke(
        container: WorkClockContainer,
        onViewModelReady: (TimeTrackerViewModel) -> Unit = {},
        onStateChanged: (TimeTrackerUiState) -> Unit = {}
    ) {
        val timeProvider = container.timeProvider
        val repository = container.repository
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
        val requestedTabEvent by DeepLinkRouter.requestedTabEvent.collectAsState()
        var selectedCalendarDate by remember { mutableStateOf(calendarState.days.firstOrNull { it.isToday }?.date) }
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val undoLabel = stringResource(Res.string.undo)
        val backupStateHolder = rememberBackupStateHolder(
            workDayStore = container.workDayStore,
            repository = repository,
            timeProvider = timeProvider,
            backupFileController = container.backupFileController
        )
        val reportStateHolder = rememberReportStateHolder(
            repository = repository,
            timeProvider = timeProvider,
            fileController = container.reportFileController
        )

        androidx.compose.runtime.LaunchedEffect(requestedTabEvent) {
            if (requestedTabEvent > 0) {
                activeTab = MainTab.Today
            }
        }
        androidx.compose.runtime.LaunchedEffect(state) {
            onStateChanged(state)
        }
        androidx.compose.runtime.LaunchedEffect(resolvedViewModel) {
            onViewModelReady(resolvedViewModel)
        }
        androidx.compose.runtime.LaunchedEffect(repository, container.lockScreenStatusController) {
            LockScreenStatusCoordinator(repository, timeProvider, container.lockScreenStatusController).run()
        }
        androidx.compose.runtime.LaunchedEffect(repository, container.reminderScheduler) {
            ReminderScheduleCoordinator(repository, timeProvider, container.reminderScheduler).run()
        }

        TimeTrackerTheme {
            Scaffold(
                containerColor = com.iamapo.timetracker.ui.theme.AppColors.Background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                ),
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
                            onSecondaryAction = {
                                val finishedMinute = resolvedViewModel.onSecondaryAction()
                                if (finishedMinute != null) {
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = getString(
                                                Res.string.workday_finished_at_message,
                                                TimeTextFormatter.clock(finishedMinute)
                                            ),
                                            actionLabel = undoLabel,
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            resolvedViewModel.onReopenDay()
                                        }
                                    }
                                }
                            },
                            onEventTimeChanged = resolvedViewModel::onTimelineEventTimeChanged,
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
                            onPreviousMonth = resolvedCalendarViewModel::showPreviousMonth,
                            onNextMonth = resolvedCalendarViewModel::showNextMonth,
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
                            onRemindersChanged = { enabled ->
                                if (enabled) {
                                    container.reminderScheduler.requestAuthorization { authorized ->
                                        resolvedSettingsViewModel.setRemindersEnabled(authorized)
                                    }
                                } else {
                                    resolvedSettingsViewModel.setRemindersEnabled(false)
                                }
                            },
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
                            reportPeriod = reportStateHolder.period,
                            reportStatus = reportStateHolder.status,
                            canNavigateToNextReportPeriod = reportStateHolder.canNavigateNext,
                            onReportPeriodTypeChanged = reportStateHolder::selectPeriodType,
                            onPreviousReportPeriod = reportStateHolder::showPreviousPeriod,
                            onNextReportPeriod = reportStateHolder::showNextPeriod,
                            onExportReport = reportStateHolder::exportPdf,
                            onDeleteAllEntries = resolvedSettingsViewModel::deleteAllEntries,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
