package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
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
    initialDay: WorkDay = WorkDay()
) : ViewModel() {
    private val day = MutableStateFlow(initialDay)
    private val ticker = MutableStateFlow(0)

    val uiState: StateFlow<TimeTrackerUiState> = combine(day, ticker) { workDay, _ ->
        TimeTrackerUiStateMapper.map(workDay, timeProvider.now())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimeTrackerUiStateMapper.map(initialDay, timeProvider.now())
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
        when (day.value.status) {
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
        val now = timeProvider.now().minuteOfDay
        day.update { current ->
            when (action) {
                TimeTrackerAction.StartDay -> current.start(now)
                TimeTrackerAction.StartBreak -> current.startBreak(now)
                TimeTrackerAction.ResumeWork -> current.resume(now)
                TimeTrackerAction.EndDay -> current.finish(now)
                TimeTrackerAction.StartNewDay -> WorkDay(config = current.config).start(now)
            }
        }
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
