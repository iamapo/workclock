package com.iamapo.timetracker.domain

/**
 * Corrections for a running work day: an already recorded event is moved in time
 * and the day's totals are derived again from the resulting event list.
 *
 * Only days that are still running are editable here; finished days are corrected
 * through the calendar editor.
 */
object WorkDayEventEditor {
    fun editableRange(day: WorkDay, eventIndex: Int, nowMinute: Int): IntRange? {
        if (!day.supportsEventEditing(nowMinute)) return null
        if (eventIndex !in day.events.indices) return null

        val earliest = day.events.getOrNull(eventIndex - 1)?.minuteOfDay ?: 0
        val latest = day.events.getOrNull(eventIndex + 1)?.minuteOfDay ?: nowMinute
        return if (earliest <= latest) earliest..latest else null
    }

    fun changeEventTime(
        day: WorkDay,
        eventIndex: Int,
        minuteOfDay: Int,
        nowMinute: Int
    ): WorkDay {
        val range = editableRange(day, eventIndex, nowMinute) ?: return day
        if (minuteOfDay !in range) return day

        val events = day.events.toMutableList()
        events[eventIndex] = events[eventIndex].copy(minuteOfDay = minuteOfDay)
        return day.recalculatedFrom(events)
    }

    /**
     * Editing rewrites the totals from the events, so it is limited to days whose
     * events tell the full story: a running work day recorded in ascending order
     * and not reaching into the future.
     */
    private fun WorkDay.supportsEventEditing(nowMinute: Int): Boolean =
        kind == WorkDayKind.Work &&
            (status == WorkStatus.Working || status == WorkStatus.Paused) &&
            events.isNotEmpty() &&
            events.first().kind == WorkEventKind.Work &&
            events.last().minuteOfDay <= nowMinute &&
            events.zipWithNext().all { (previous, next) -> previous.minuteOfDay <= next.minuteOfDay }

    private fun WorkDay.recalculatedFrom(events: List<WorkEvent>): WorkDay {
        var workedMinutes = 0
        var breakMinutes = 0
        var lastBreakMinutes: Int? = null
        var openMinute = events.first().minuteOfDay
        var openKind = events.first().kind

        for (event in events.drop(1)) {
            val duration = event.minuteOfDay - openMinute
            when (openKind) {
                WorkEventKind.Work -> workedMinutes += duration
                WorkEventKind.Break -> {
                    breakMinutes += duration
                    lastBreakMinutes = duration
                }
                WorkEventKind.Target -> Unit
            }
            openMinute = event.minuteOfDay
            openKind = event.kind
        }

        return copy(
            startMinute = events.first().minuteOfDay,
            activeSessionStartMinute = openMinute.takeIf { openKind == WorkEventKind.Work },
            pauseStartedMinute = openMinute.takeIf { openKind == WorkEventKind.Break },
            workedMinutes = workedMinutes,
            breakMinutes = breakMinutes,
            lastBreakMinutes = lastBreakMinutes,
            events = events
        )
    }
}
