package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.LockScreenStatusMapper
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.domain.usecase.DeleteWorkEntriesUseCase
import com.iamapo.timetracker.domain.usecase.EditCalendarDayUseCase
import com.iamapo.timetracker.domain.usecase.ObserveWorkHistoryUseCase
import com.iamapo.timetracker.domain.usecase.TrackWorkDayUseCase
import com.iamapo.timetracker.domain.usecase.UpdateWorkSettingsUseCase
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class TimeTrackerViewModel(
    private val timeProvider: TimeProvider,
    private val repository: WorkHistoryRepository,
    initialSnapshot: TimeSnapshot = timeProvider.now(),
    observeWorkHistory: ObserveWorkHistoryUseCase = ObserveWorkHistoryUseCase(repository),
    private val trackWorkDay: TrackWorkDayUseCase = TrackWorkDayUseCase(repository, timeProvider),
    private val editCalendarDay: EditCalendarDayUseCase = EditCalendarDayUseCase(repository),
    private val updateWorkSettings: UpdateWorkSettingsUseCase = UpdateWorkSettingsUseCase(repository, timeProvider),
    private val deleteWorkEntries: DeleteWorkEntriesUseCase = DeleteWorkEntriesUseCase(repository),
    private val lockScreenStatusController: LockScreenStatusController = NoOpLockScreenStatusController
) : ViewModel() {
    private val history = observeWorkHistory()
    private val ticker = MutableStateFlow(0)

    val uiState: StateFlow<TimeTrackerUiState> = combine(history, ticker) { savedHistory, _ ->
        mapUiState(savedHistory, timeProvider.now())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = mapUiState(repository.history.value, initialSnapshot)
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                ticker.update { it + 1 }
            }
        }
        viewModelScope.launch {
            combine(history, ticker) { savedHistory, _ ->
                val snapshot = timeProvider.now()
                LockScreenStatusMapper.map(
                    day = savedHistory.dayWithWeeklySummary(snapshot.date),
                    snapshot = snapshot,
                    enabled = savedHistory.lockScreenStatusEnabled
                )
            }.collect(lockScreenStatusController::publish)
        }
    }

    fun onPrimaryAction() {
        when (currentStatus()) {
            WorkStatus.NotStarted -> onAction(TimeTrackerAction.StartDay)
            WorkStatus.Working -> onAction(TimeTrackerAction.StartBreak)
            WorkStatus.Paused -> onAction(TimeTrackerAction.ResumeWork)
            WorkStatus.Finished -> onAction(TimeTrackerAction.StartNewDay)
        }
    }

    fun onSecondaryAction() {
        onAction(TimeTrackerAction.EndDay)
    }

    fun onWatchCommand(command: String) {
        when (command) {
            WatchCommandPrimary -> onPrimaryAction()
            WatchCommandStartDay -> onAction(TimeTrackerAction.StartDay)
            WatchCommandStartBreak -> onAction(TimeTrackerAction.StartBreak)
            WatchCommandResumeWork -> onAction(TimeTrackerAction.ResumeWork)
            WatchCommandStartNewDay -> onAction(TimeTrackerAction.StartNewDay)
            WatchCommandEndDay -> onAction(TimeTrackerAction.EndDay)
        }
    }

    fun onWatchEvent(
        command: String,
        date: LocalDate,
        minuteOfDay: Int
    ): Boolean {
        val status = repository.history.value.dayWithWeeklySummary(date).status
        val action = actionForWatchCommand(command, status) ?: return false
        if (!canApplyWatchAction(action, status)) return false

        trackWorkDay(
            action = action,
            date = date,
            minuteOfDay = minuteOfDay
        )
        return true
    }

    fun onAction(action: TimeTrackerAction) {
        trackWorkDay(action)
    }

    fun increaseRequiredBreak() {
        updateWorkSettings.increaseRequiredBreak()
    }

    fun decreaseRequiredBreak() {
        updateWorkSettings.decreaseRequiredBreak()
    }

    fun setLockScreenStatusEnabled(enabled: Boolean) {
        updateWorkSettings.setLockScreenStatusEnabled(enabled)
    }

    fun increaseCalendarDay(date: LocalDate) {
        editCalendarDay.increaseDay(date)
    }

    fun decreaseCalendarDay(date: LocalDate) {
        editCalendarDay.decreaseDay(date)
    }

    fun setCalendarDayVacation(date: LocalDate) {
        editCalendarDay.setVacation(date)
    }

    fun setCalendarDaySick(date: LocalDate) {
        editCalendarDay.setSick(date)
    }

    fun clearCalendarDay(date: LocalDate) {
        editCalendarDay.clearDay(date)
    }

    fun deleteAllEntries() {
        deleteWorkEntries()
    }

    private fun currentStatus(): WorkStatus {
        val snapshot = timeProvider.now()
        return repository.history.value.dayWithWeeklySummary(snapshot.date).status
    }

    private fun actionForWatchCommand(
        command: String,
        status: WorkStatus
    ): TimeTrackerAction? = when (command) {
        WatchCommandPrimary -> when (status) {
            WorkStatus.NotStarted -> TimeTrackerAction.StartDay
            WorkStatus.Working -> TimeTrackerAction.StartBreak
            WorkStatus.Paused -> TimeTrackerAction.ResumeWork
            WorkStatus.Finished -> TimeTrackerAction.StartNewDay
        }
        WatchCommandStartDay -> TimeTrackerAction.StartDay
        WatchCommandStartBreak -> TimeTrackerAction.StartBreak
        WatchCommandResumeWork -> TimeTrackerAction.ResumeWork
        WatchCommandStartNewDay -> TimeTrackerAction.StartNewDay
        WatchCommandEndDay -> TimeTrackerAction.EndDay
        else -> null
    }

    private fun canApplyWatchAction(
        action: TimeTrackerAction,
        status: WorkStatus
    ): Boolean = when (action) {
        TimeTrackerAction.StartDay -> status == WorkStatus.NotStarted
        TimeTrackerAction.StartBreak -> status == WorkStatus.Working
        TimeTrackerAction.ResumeWork -> status == WorkStatus.Paused
        TimeTrackerAction.EndDay -> status == WorkStatus.Working || status == WorkStatus.Paused
        TimeTrackerAction.StartNewDay -> status == WorkStatus.Finished
    }

    private fun mapUiState(
        history: WorkHistory,
        snapshot: TimeSnapshot
    ): TimeTrackerUiState = TimeTrackerUiStateMapper.map(
        day = history.dayWithWeeklySummary(snapshot.date),
        snapshot = snapshot,
        history = history.days,
        lockScreenStatusEnabled = history.lockScreenStatusEnabled
    )

    private companion object {
        const val WatchCommandPrimary = "primary"
        const val WatchCommandStartDay = "startDay"
        const val WatchCommandStartBreak = "startBreak"
        const val WatchCommandResumeWork = "resumeWork"
        const val WatchCommandStartNewDay = "startNewDay"
        const val WatchCommandEndDay = "endDay"
    }
}
