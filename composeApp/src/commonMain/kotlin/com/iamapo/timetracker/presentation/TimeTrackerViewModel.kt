package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.ui.state.TimeTrackerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimeTrackerViewModel(
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val workDayStore: WorkDayStore = NoOpWorkDayStore,
    initialSnapshot: TimeSnapshot = timeProvider.now(),
    initialHistory: WorkHistory = workDayStore.loadHistory(initialSnapshot.date)
) : ViewModel() {
    private val history = MutableStateFlow(initialHistory)
    private val ticker = MutableStateFlow(0)

    val uiState: StateFlow<TimeTrackerUiState> = combine(history, ticker) { savedHistory, _ ->
        val snapshot = timeProvider.now()
        TimeTrackerUiStateMapper.map(
            day = savedHistory.dayWithWeeklySummary(snapshot.date),
            snapshot = snapshot,
            history = savedHistory.days
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimeTrackerUiStateMapper.map(
            day = initialHistory.dayWithWeeklySummary(initialSnapshot.date),
            snapshot = initialSnapshot,
            history = initialHistory.days
        )
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
        when (currentDay().status) {
            WorkStatus.NotStarted -> onAction(TimeTrackerAction.StartDay)
            WorkStatus.Working -> onAction(TimeTrackerAction.StartBreak)
            WorkStatus.Paused -> onAction(TimeTrackerAction.ResumeWork)
            WorkStatus.Finished -> onAction(TimeTrackerAction.StartNewDay)
        }
    }

    fun onSecondaryAction() {
        onAction(TimeTrackerAction.EndDay)
    }

    fun onAction(action: TimeTrackerAction) {
        val snapshot = timeProvider.now()
        val current = history.value.dayWithWeeklySummary(snapshot.date)
        val updated = when (action) {
            TimeTrackerAction.StartDay -> current.start(snapshot.minuteOfDay)
            TimeTrackerAction.StartBreak -> current.startBreak(snapshot.minuteOfDay)
            TimeTrackerAction.ResumeWork -> current.resume(snapshot.minuteOfDay)
            TimeTrackerAction.EndDay -> current.finish(snapshot.minuteOfDay)
            TimeTrackerAction.StartNewDay -> WorkDay(config = current.config).start(snapshot.minuteOfDay)
        }
        val updatedHistory = history.value.withDay(snapshot.date, updated)

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    private fun currentDay(): WorkDay {
        val snapshot = timeProvider.now()
        return history.value.dayWithWeeklySummary(snapshot.date)
    }

    private fun WorkDay.start(now: Int): WorkDay = copy(
        status = WorkStatus.Working,
        startMinute = now,
        activeSessionStartMinute = now,
        pauseStartedMinute = null,
        workedMinutes = 0,
        breakMinutes = 0,
        lastBreakMinutes = null,
        events = listOf(WorkEvent(now, "Arbeitsbeginn", WorkEventKind.Work))
    )

    private fun WorkDay.startBreak(now: Int): WorkDay {
        if (status != WorkStatus.Working || activeSessionStartMinute == null) return this
        val workedInSession = elapsedMinutes(activeSessionStartMinute, now)
        return copy(
            status = WorkStatus.Paused,
            activeSessionStartMinute = null,
            pauseStartedMinute = now,
            workedMinutes = workedMinutes + workedInSession,
            events = events + WorkEvent(now, "Pause gestartet", WorkEventKind.Break)
        )
    }

    private fun WorkDay.resume(now: Int): WorkDay {
        if (status != WorkStatus.Paused || pauseStartedMinute == null) return this
        val breakInSession = elapsedMinutes(pauseStartedMinute, now)
        return copy(
            status = WorkStatus.Working,
            activeSessionStartMinute = now,
            pauseStartedMinute = null,
            breakMinutes = breakMinutes + breakInSession,
            lastBreakMinutes = breakInSession,
            events = events + WorkEvent(now, "Weitergearbeitet", WorkEventKind.Work)
        )
    }

    private fun WorkDay.finish(now: Int): WorkDay = when (status) {
        WorkStatus.Working -> {
            val workedInSession = activeSessionStartMinute?.let { elapsedMinutes(it, now) } ?: 0
            copy(
                status = WorkStatus.Finished,
                activeSessionStartMinute = null,
                workedMinutes = workedMinutes + workedInSession,
                events = events + WorkEvent(now, "Arbeitstag beendet", WorkEventKind.Target)
            )
        }
        WorkStatus.Paused -> {
            val breakInSession = pauseStartedMinute?.let { elapsedMinutes(it, now) } ?: 0
            copy(
                status = WorkStatus.Finished,
                pauseStartedMinute = null,
                breakMinutes = breakMinutes + breakInSession,
                lastBreakMinutes = breakInSession.takeIf { it > 0 } ?: lastBreakMinutes,
                events = events + WorkEvent(now, "Arbeitstag beendet", WorkEventKind.Target)
            )
        }
        WorkStatus.NotStarted,
        WorkStatus.Finished -> this
    }

    private fun elapsedMinutes(startMinute: Int, endMinute: Int): Int =
        if (endMinute >= startMinute) endMinute - startMinute else 24 * 60 - startMinute + endMinute
}
