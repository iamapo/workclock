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
import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.backup.rememberBackupStateHolder
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusCoordinator
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.presentation.CalendarViewModel
import com.iamapo.timetracker.presentation.SettingsViewModel
import com.iamapo.timetracker.presentation.TimeTextFormatter
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.reminders.ReminderScheduleCoordinator
import com.iamapo.timetracker.reminders.ReminderScheduler
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

object TimeTrackerRoute {
    @Composable
    operator fun invoke(
        onViewModelReady: (TimeTrackerViewModel) -> Unit = {},
        onStateChanged: (TimeTrackerUiState) -> Unit = {},
        timeProvider: TimeProvider = koinInject(),
        repository: WorkHistoryRepository = koinInject(),
        workDayStore: WorkDayStore = koinInject(),
        backupFileController: BackupFileController = koinInject(),
        lockScreenStatusController: LockScreenStatusController = koinInject(),
        reminderScheduler: ReminderScheduler = koinInject(),
        timeTrackerViewModel: TimeTrackerViewModel = koinViewModel(),
        calendarViewModel: CalendarViewModel = koinViewModel(),
        settingsViewModel: SettingsViewModel = koinViewModel()
    ) {
        val state by timeTrackerViewModel.uiState.collectAsState()
        val calendarState by calendarViewModel.uiState.collectAsState()
        val settingsState by settingsViewModel.uiState.collectAsState()
        var activeTab by remember { mutableStateOf(MainTab.Today) }
        val requestedTabEvent by DeepLinkRouter.requestedTabEvent.collectAsState()
        var selectedCalendarDate by remember { mutableStateOf(calendarState.days.firstOrNull { it.isToday }?.date) }
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val undoLabel = stringResource(Res.string.undo)
        val backupStateHolder = rememberBackupStateHolder(
            workDayStore = workDayStore,
            repository = repository,
            timeProvider = timeProvider,
            backupFileController = backupFileController
        )

        androidx.compose.runtime.LaunchedEffect(requestedTabEvent) {
            if (requestedTabEvent > 0) {
                activeTab = MainTab.Today
            }
        }
        androidx.compose.runtime.LaunchedEffect(state) {
            onStateChanged(state)
        }
        androidx.compose.runtime.LaunchedEffect(timeTrackerViewModel) {
            onViewModelReady(timeTrackerViewModel)
        }
        androidx.compose.runtime.LaunchedEffect(repository, lockScreenStatusController) {
            LockScreenStatusCoordinator(repository, timeProvider, lockScreenStatusController).run()
        }
        androidx.compose.runtime.LaunchedEffect(repository, reminderScheduler) {
            ReminderScheduleCoordinator(repository, timeProvider, reminderScheduler).run()
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
                            onPrimaryAction = timeTrackerViewModel::onPrimaryAction,
                            onSecondaryAction = {
                                val finishedMinute = timeTrackerViewModel.onSecondaryAction()
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
                                            timeTrackerViewModel.onReopenDay()
                                        }
                                    }
                                }
                            },
                            onEventTimeChanged = timeTrackerViewModel::onTimelineEventTimeChanged,
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
                            onPreviousMonth = calendarViewModel::showPreviousMonth,
                            onNextMonth = calendarViewModel::showNextMonth,
                            onIncreaseDay = calendarViewModel::increaseDay,
                            onDecreaseDay = calendarViewModel::decreaseDay,
                            onVacation = calendarViewModel::setVacation,
                            onSick = calendarViewModel::setSick,
                            onForgottenWorkDay = calendarViewModel::setForgottenWorkDay,
                            onClear = calendarViewModel::clearDay,
                            onSetWorkTimes = calendarViewModel::setWorkTimes,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                    MainTab.Settings -> {
                        SettingsScreen(
                            state = settingsState,
                            onDecreaseRequiredBreak = settingsViewModel::decreaseRequiredBreak,
                            onIncreaseRequiredBreak = settingsViewModel::increaseRequiredBreak,
                            onLockScreenStatusChanged = settingsViewModel::setLockScreenStatusEnabled,
                            onRemindersChanged = { enabled ->
                                if (enabled) {
                                    reminderScheduler.requestAuthorization { authorized ->
                                        settingsViewModel.setRemindersEnabled(authorized)
                                    }
                                } else {
                                    settingsViewModel.setRemindersEnabled(false)
                                }
                            },
                            onDecreaseWeekdayTarget = settingsViewModel::decreaseWeekdayTarget,
                            onIncreaseWeekdayTarget = settingsViewModel::increaseWeekdayTarget,
                            onAutomaticHolidaysChanged = settingsViewModel::setAutomaticHolidaysEnabled,
                            onHolidayFederalStateChanged = settingsViewModel::setHolidayFederalState,
                            backupStatus = backupStateHolder.status,
                            pendingBackupImport = backupStateHolder.pendingImport,
                            canUndoImport = backupStateHolder.canUndoImport,
                            onExportBackup = backupStateHolder::exportBackup,
                            onImportBackup = backupStateHolder::importBackup,
                            onCancelImport = backupStateHolder::cancelImport,
                            onConfirmImport = backupStateHolder::confirmImport,
                            onUndoImport = backupStateHolder::undoImport,
                            onDeleteAllEntries = settingsViewModel::deleteAllEntries,
                            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
