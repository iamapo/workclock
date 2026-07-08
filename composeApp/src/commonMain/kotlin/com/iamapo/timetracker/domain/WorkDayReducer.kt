package com.iamapo.timetracker.domain

class WorkDayReducer {
    fun reduce(
        day: WorkDay,
        action: TimeTrackerAction,
        nowMinute: Int,
        defaultConfig: WorkDayConfig
    ): WorkDay = when (action) {
        TimeTrackerAction.StartDay -> day.start(nowMinute)
        TimeTrackerAction.StartBreak -> day.startBreak(nowMinute)
        TimeTrackerAction.ResumeWork -> day.resume(nowMinute)
        TimeTrackerAction.EndDay -> day.finish(nowMinute)
        TimeTrackerAction.StartNewDay -> WorkDay(config = defaultConfig).start(nowMinute)
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
}
