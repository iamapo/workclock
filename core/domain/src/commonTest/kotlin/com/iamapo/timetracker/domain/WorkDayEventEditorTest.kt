package com.iamapo.timetracker.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkDayEventEditorTest {
    @Test
    fun correctingResumeTimeRecalculatesBreakAndWorkedMinutes() {
        // The break was ended at 15:00 although work actually resumed at 12:30.
        val day = runningDay(
            status = WorkStatus.Working,
            events = listOf(
                WorkEvent(8 * 60 + 42, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60 + 2, "Pause gestartet", WorkEventKind.Break),
                WorkEvent(15 * 60, "Weitergearbeitet", WorkEventKind.Work)
            )
        )

        val corrected = WorkDayEventEditor.changeEventTime(
            day = day,
            eventIndex = 2,
            minuteOfDay = 12 * 60 + 30,
            nowMinute = 15 * 60
        )

        assertEquals(28, corrected.breakMinutes)
        assertEquals(28, corrected.lastBreakMinutes)
        assertEquals(3 * 60 + 20, corrected.workedMinutes)
        assertEquals(12 * 60 + 30, corrected.activeSessionStartMinute)
        assertNull(corrected.pauseStartedMinute)
        assertEquals(8 * 60 + 42, corrected.startMinute)
    }

    @Test
    fun correctingStartTimeShortensWorkedMinutes() {
        val day = runningDay(
            status = WorkStatus.Paused,
            events = listOf(
                WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break)
            )
        )

        val corrected = WorkDayEventEditor.changeEventTime(
            day = day,
            eventIndex = 0,
            minuteOfDay = 9 * 60,
            nowMinute = 12 * 60 + 20
        )

        assertEquals(9 * 60, corrected.startMinute)
        assertEquals(3 * 60, corrected.workedMinutes)
        assertEquals(0, corrected.breakMinutes)
        assertEquals(12 * 60, corrected.pauseStartedMinute)
        assertNull(corrected.activeSessionStartMinute)
    }

    @Test
    fun timeBeforePreviousEventIsRejected() {
        val day = runningDay(
            status = WorkStatus.Paused,
            events = listOf(
                WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break)
            )
        )

        val corrected = WorkDayEventEditor.changeEventTime(
            day = day,
            eventIndex = 1,
            minuteOfDay = 7 * 60,
            nowMinute = 12 * 60 + 20
        )

        assertEquals(day, corrected)
    }

    @Test
    fun timeInTheFutureIsRejected() {
        val day = runningDay(
            status = WorkStatus.Paused,
            events = listOf(
                WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break)
            )
        )

        val corrected = WorkDayEventEditor.changeEventTime(
            day = day,
            eventIndex = 1,
            minuteOfDay = 13 * 60,
            nowMinute = 12 * 60 + 20
        )

        assertEquals(day, corrected)
    }

    @Test
    fun editableRangeSpansNeighbouringEvents() {
        val day = runningDay(
            status = WorkStatus.Working,
            events = listOf(
                WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break),
                WorkEvent(12 * 60 + 30, "Weitergearbeitet", WorkEventKind.Work)
            )
        )

        assertEquals(0..12 * 60, WorkDayEventEditor.editableRange(day, 0, 15 * 60))
        assertEquals(8 * 60..12 * 60 + 30, WorkDayEventEditor.editableRange(day, 1, 15 * 60))
        assertEquals(12 * 60..15 * 60, WorkDayEventEditor.editableRange(day, 2, 15 * 60))
    }

    @Test
    fun finishedAndNotStartedDaysAreNotEditable() {
        val finishedDay = runningDay(
            status = WorkStatus.Finished,
            events = listOf(
                WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(16 * 60, "Arbeitstag beendet", WorkEventKind.Target)
            )
        )

        assertNull(WorkDayEventEditor.editableRange(finishedDay, 0, 17 * 60))
        assertNull(WorkDayEventEditor.editableRange(WorkDay(), 0, 17 * 60))
    }

    @Test
    fun vacationDaysAreNotEditable() {
        val vacationDay = WorkDay(
            kind = WorkDayKind.Vacation,
            status = WorkStatus.Working,
            workedMinutes = 8 * 60,
            events = listOf(WorkEvent(0, "Urlaub", WorkEventKind.Target))
        )

        assertNull(WorkDayEventEditor.editableRange(vacationDay, 0, 17 * 60))
    }

    private fun runningDay(status: WorkStatus, events: List<WorkEvent>): WorkDay {
        val lastEvent = events.last()
        return WorkDay(
            status = status,
            startMinute = events.first().minuteOfDay,
            activeSessionStartMinute = lastEvent.minuteOfDay.takeIf { lastEvent.kind == WorkEventKind.Work },
            pauseStartedMinute = lastEvent.minuteOfDay.takeIf { lastEvent.kind == WorkEventKind.Break },
            events = events
        )
    }
}
