package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.ObserveWorkHistoryUseCase
import com.iamapo.timetracker.domain.usecase.TrackWorkDayUseCase
import com.iamapo.timetracker.domain.usecase.HandleTimeTrackingCommandUseCase
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
    private val handleTimeTrackingCommand: HandleTimeTrackingCommandUseCase =
        HandleTimeTrackingCommandUseCase(repository, timeProvider, trackWorkDay)
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
    }

    fun onPrimaryAction() {
        handleTimeTrackingCommand(TimeTrackingCommand.Primary)
    }

    fun onSecondaryAction() {
        onAction(TimeTrackerAction.EndDay)
    }

    fun onWatchCommand(command: TimeTrackingCommand) {
        handleTimeTrackingCommand(command)
    }

    fun onWatchEvent(
        command: TimeTrackingCommand,
        date: LocalDate,
        minuteOfDay: Int
    ): Boolean {
        return handleTimeTrackingCommand(command, date, minuteOfDay)
    }

    fun onAction(action: TimeTrackerAction) {
        trackWorkDay(action)
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

}
