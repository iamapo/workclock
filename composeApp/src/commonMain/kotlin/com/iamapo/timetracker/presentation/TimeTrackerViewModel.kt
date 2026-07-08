package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.ui.state.TimeTrackerUiState
import kotlinx.datetime.LocalDate
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

    fun onWatchCommand(command: String) {
        when (command) {
            WatchCommandPrimary -> onPrimaryAction()
            WatchCommandStartDay -> onAction(TimeTrackerAction.StartDay)
            WatchCommandStartBreak -> onAction(TimeTrackerAction.StartBreak)
            WatchCommandResumeWork -> onAction(TimeTrackerAction.ResumeWork)
            WatchCommandEndDay -> onAction(TimeTrackerAction.EndDay)
        }
    }

    fun onAction(action: TimeTrackerAction) {
        val snapshot = timeProvider.now()
        val current = history.value.dayWithWeeklySummary(snapshot.date)
        val updated = when (action) {
            TimeTrackerAction.StartDay -> current.start(snapshot.minuteOfDay)
            TimeTrackerAction.StartBreak -> current.startBreak(snapshot.minuteOfDay)
            TimeTrackerAction.ResumeWork -> current.resume(snapshot.minuteOfDay)
            TimeTrackerAction.EndDay -> current.finish(snapshot.minuteOfDay)
            TimeTrackerAction.StartNewDay -> WorkDay(config = history.value.defaultConfig).start(snapshot.minuteOfDay)
        }
        val updatedHistory = history.value.withDay(snapshot.date, updated)

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    fun increaseRequiredBreak() {
        updateRequiredBreak { current -> current + RequiredBreakStepMinutes }
    }

    fun decreaseRequiredBreak() {
        updateRequiredBreak { current -> current - RequiredBreakStepMinutes }
    }

    fun increaseCalendarDay(date: LocalDate) {
        adjustCalendarDay(date, ManualEditStepMinutes)
    }

    fun decreaseCalendarDay(date: LocalDate) {
        adjustCalendarDay(date, -ManualEditStepMinutes)
    }

    fun setCalendarDayVacation(date: LocalDate) {
        updateCalendarDay(date, WorkDayKind.Vacation, FullAbsenceDayMinutes, "Urlaub")
    }

    fun setCalendarDaySick(date: LocalDate) {
        updateCalendarDay(date, WorkDayKind.Sick, FullAbsenceDayMinutes, "Krank")
    }

    fun clearCalendarDay(date: LocalDate) {
        val updatedHistory = history.value.withoutDay(date)

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    fun deleteAllEntries() {
        val updatedHistory = WorkHistory(defaultConfig = history.value.defaultConfig)

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    private fun currentDay(): WorkDay {
        val snapshot = timeProvider.now()
        return history.value.dayWithWeeklySummary(snapshot.date)
    }

    private fun updateRequiredBreak(transform: (Int) -> Int) {
        val snapshot = timeProvider.now()
        val current = history.value.dayWithWeeklySummary(snapshot.date)
        val updatedConfig = current.config.copy(
            requiredBreakMinutes = transform(current.config.requiredBreakMinutes)
                .coerceIn(MinRequiredBreakMinutes, MaxRequiredBreakMinutes)
        )
        val updatedHistory = history.value
            .withDefaultConfig(updatedConfig)
            .withDay(snapshot.date, current.copy(config = updatedConfig))

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    private fun adjustCalendarDay(date: LocalDate, deltaMinutes: Int) {
        val current = history.value.dayFor(date)
        val updatedMinutes = (current.workedMinutes + deltaMinutes).coerceIn(0, MaxManualDayMinutes)
        updateCalendarDay(date, WorkDayKind.Work, updatedMinutes, "Manueller Eintrag")
    }

    private fun updateCalendarDay(
        date: LocalDate,
        kind: WorkDayKind,
        workedMinutes: Int,
        title: String
    ) {
        val current = history.value.dayFor(date)
        val updated = current.copy(
            kind = kind,
            status = if (workedMinutes > 0) WorkStatus.Finished else WorkStatus.NotStarted,
            startMinute = null,
            activeSessionStartMinute = null,
            pauseStartedMinute = null,
            workedMinutes = workedMinutes,
            breakMinutes = 0,
            lastBreakMinutes = null,
            weeklyWorkedBeforeTodayMinutes = 0,
            events = if (workedMinutes > 0) {
                listOf(WorkEvent(0, title, WorkEventKind.Target))
            } else {
                emptyList()
            }
        )
        val updatedHistory = history.value.withDay(date, updated)

        history.value = updatedHistory
        workDayStore.saveHistory(updatedHistory)
    }

    private fun WorkDay.start(now: Int): WorkDay = copy(
        kind = WorkDayKind.Work,
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

    private companion object {
        const val WatchCommandPrimary = "primary"
        const val WatchCommandStartDay = "startDay"
        const val WatchCommandStartBreak = "startBreak"
        const val WatchCommandResumeWork = "resumeWork"
        const val WatchCommandEndDay = "endDay"
        const val RequiredBreakStepMinutes = 5
        const val MinRequiredBreakMinutes = 0
        const val MaxRequiredBreakMinutes = 120
        const val ManualEditStepMinutes = 15
        const val FullAbsenceDayMinutes = 8 * 60
        const val MaxManualDayMinutes = 24 * 60
    }
}
