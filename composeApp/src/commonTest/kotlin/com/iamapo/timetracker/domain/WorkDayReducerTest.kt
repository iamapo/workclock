package com.iamapo.timetracker.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkDayReducerTest {
    private val reducer = WorkDayReducer()

    @Test
    fun startDayCreatesActiveWorkSession() {
        val day = reducer.reduce(
            day = WorkDay(),
            action = TimeTrackerAction.StartDay,
            nowMinute = 9 * 60,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Working, day.status)
        assertEquals(9 * 60, day.startMinute)
        assertEquals(9 * 60, day.activeSessionStartMinute)
        assertEquals("Arbeitsbeginn", day.events.single().title)
    }

    @Test
    fun startBreakStoresWorkedSessionMinutes() {
        val workingDay = WorkDay(
            status = WorkStatus.Working,
            activeSessionStartMinute = 9 * 60,
            workedMinutes = 45
        )

        val day = reducer.reduce(
            day = workingDay,
            action = TimeTrackerAction.StartBreak,
            nowMinute = 11 * 60,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Paused, day.status)
        assertNull(day.activeSessionStartMinute)
        assertEquals(11 * 60, day.pauseStartedMinute)
        assertEquals(2 * 60 + 45, day.workedMinutes)
    }

    @Test
    fun resumeWorkStoresBreakSessionMinutes() {
        val pausedDay = WorkDay(
            status = WorkStatus.Paused,
            pauseStartedMinute = 12 * 60,
            breakMinutes = 10
        )

        val day = reducer.reduce(
            day = pausedDay,
            action = TimeTrackerAction.ResumeWork,
            nowMinute = 12 * 60 + 30,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Working, day.status)
        assertEquals(12 * 60 + 30, day.activeSessionStartMinute)
        assertNull(day.pauseStartedMinute)
        assertEquals(40, day.breakMinutes)
        assertEquals(30, day.lastBreakMinutes)
    }

    @Test
    fun finishWorkingDayStoresFinalWorkSession() {
        val workingDay = WorkDay(
            status = WorkStatus.Working,
            activeSessionStartMinute = 15 * 60,
            workedMinutes = 5 * 60
        )

        val day = reducer.reduce(
            day = workingDay,
            action = TimeTrackerAction.EndDay,
            nowMinute = 17 * 60,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Finished, day.status)
        assertNull(day.activeSessionStartMinute)
        assertEquals(7 * 60, day.workedMinutes)
        assertEquals("Arbeitstag beendet", day.events.single().title)
    }

    @Test
    fun reopenFinishedWorkingDayContinuesFromItsRecordedEnd() {
        val finishedDay = WorkDay(
            status = WorkStatus.Finished,
            startMinute = 9 * 60,
            workedMinutes = 7 * 60 + 30,
            breakMinutes = 30,
            events = listOf(
                WorkEvent(9 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break),
                WorkEvent(12 * 60 + 30, "Weitergearbeitet", WorkEventKind.Work),
                WorkEvent(17 * 60, "Arbeitstag beendet", WorkEventKind.Target)
            )
        )

        val reopened = reducer.reduce(
            day = finishedDay,
            action = TimeTrackerAction.ReopenDay,
            nowMinute = 17 * 60 + 5,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Working, reopened.status)
        assertEquals(17 * 60, reopened.activeSessionStartMinute)
        assertEquals(7 * 60 + 30, reopened.workedMinutes)
        assertEquals(3, reopened.events.size)
    }

    @Test
    fun reopenFinishedPausedDayContinuesItsBreakFromTheRecordedEnd() {
        val finishedDay = WorkDay(
            status = WorkStatus.Finished,
            startMinute = 9 * 60,
            workedMinutes = 3 * 60,
            breakMinutes = 30,
            lastBreakMinutes = 30,
            events = listOf(
                WorkEvent(9 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break),
                WorkEvent(12 * 60 + 30, "Arbeitstag beendet", WorkEventKind.Target)
            )
        )

        val reopened = reducer.reduce(
            day = finishedDay,
            action = TimeTrackerAction.ReopenDay,
            nowMinute = 12 * 60 + 35,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Paused, reopened.status)
        assertEquals(12 * 60 + 30, reopened.pauseStartedMinute)
        assertEquals(30, reopened.breakMinutes)
        assertEquals(2, reopened.events.size)
    }

    @Test
    fun changeEventTimeCorrectsARunningDay() {
        val runningDay = WorkDay(
            status = WorkStatus.Paused,
            startMinute = 9 * 60,
            pauseStartedMinute = 13 * 60,
            workedMinutes = 4 * 60,
            events = listOf(
                WorkEvent(9 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(13 * 60, "Pause gestartet", WorkEventKind.Break)
            )
        )

        val day = reducer.reduce(
            day = runningDay,
            action = TimeTrackerAction.ChangeEventTime(eventIndex = 1, minuteOfDay = 12 * 60 + 30),
            nowMinute = 13 * 60 + 45,
            defaultConfig = WorkDayConfig()
        )

        assertEquals(WorkStatus.Paused, day.status)
        assertEquals(12 * 60 + 30, day.pauseStartedMinute)
        assertEquals(3 * 60 + 30, day.workedMinutes)
        assertEquals(12 * 60 + 30, day.events[1].minuteOfDay)
    }
}
