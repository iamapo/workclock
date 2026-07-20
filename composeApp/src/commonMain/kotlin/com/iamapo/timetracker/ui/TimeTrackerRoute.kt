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
import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.backup.BackupFileResult
import com.iamapo.timetracker.backup.BackupReadResult
import com.iamapo.timetracker.backup.BackupUiStatus
import com.iamapo.timetracker.backup.NoOpBackupFileController
import com.iamapo.timetracker.backup.PendingBackupImport
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkClockBackupSerializer
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
        backupFileController: BackupFileController = NoOpBackupFileController,
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
        var backupStatus by remember { mutableStateOf(BackupUiStatus.None) }
        var pendingBackupImport by remember { mutableStateOf<PendingBackupImport?>(null) }
        var canUndoImport by remember(workDayStore) {
            mutableStateOf(workDayStore.loadPreImportHistory() != null)
        }

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
                            backupStatus = backupStatus,
                            pendingBackupImport = pendingBackupImport,
                            canUndoImport = canUndoImport,
                            onExportBackup = {
                                backupStatus = BackupUiStatus.None
                                val snapshot = timeProvider.now()
                                val content = WorkClockBackupSerializer.encode(
                                    history = repository.history.value,
                                    createdAtEpochMillis = snapshot.epochMillis
                                )
                                backupFileController.saveBackup(
                                    suggestedFileName = "workclock-backup-${snapshot.date}.${WorkClockBackupSerializer.FileExtension}",
                                    content = content
                                ) { result ->
                                    backupStatus = when (result) {
                                        BackupFileResult.Success -> BackupUiStatus.Exported
                                        BackupFileResult.Failure -> BackupUiStatus.Failure
                                        BackupFileResult.Cancelled -> BackupUiStatus.None
                                    }
                                }
                            },
                            onImportBackup = {
                                backupStatus = BackupUiStatus.None
                                backupFileController.openBackup { result ->
                                    when (result) {
                                        is BackupReadResult.Success -> {
                                            val backup = WorkClockBackupSerializer.decode(result.content)
                                            if (backup == null) {
                                                backupStatus = BackupUiStatus.InvalidFile
                                            } else {
                                                val dates = backup.history.days.keys
                                                pendingBackupImport = PendingBackupImport(
                                                    backup = backup,
                                                    firstDate = dates.minOrNull()?.toString(),
                                                    lastDate = dates.maxOrNull()?.toString()
                                                )
                                            }
                                        }
                                        BackupReadResult.Failure -> backupStatus = BackupUiStatus.Failure
                                        BackupReadResult.Cancelled -> backupStatus = BackupUiStatus.None
                                    }
                                }
                            },
                            onCancelImport = { pendingBackupImport = null },
                            onConfirmImport = {
                                val candidate = pendingBackupImport
                                if (candidate != null) {
                                    pendingBackupImport = null
                                    runCatching {
                                        workDayStore.savePreImportHistory(repository.history.value)
                                        repository.update { candidate.backup.history }
                                    }.onSuccess {
                                        canUndoImport = true
                                        backupStatus = BackupUiStatus.Imported
                                    }.onFailure {
                                        backupStatus = BackupUiStatus.Failure
                                    }
                                }
                            },
                            onUndoImport = {
                                val previousHistory = workDayStore.loadPreImportHistory()
                                if (previousHistory == null) {
                                    canUndoImport = false
                                    backupStatus = BackupUiStatus.Failure
                                } else {
                                    runCatching {
                                        repository.update { previousHistory }
                                        workDayStore.clearPreImportHistory()
                                    }.onSuccess {
                                        canUndoImport = false
                                        backupStatus = BackupUiStatus.ImportUndone
                                    }.onFailure {
                                        backupStatus = BackupUiStatus.Failure
                                    }
                                }
                            },
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
