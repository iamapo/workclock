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
}
