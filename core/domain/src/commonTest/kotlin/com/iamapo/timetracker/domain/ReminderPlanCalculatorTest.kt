package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReminderPlanCalculatorTest {
    private val calculator = ReminderPlanCalculator()
    private val date = LocalDate(2026, 7, 13)

    @Test
    fun schedulesBreakReminderWhenPausedAndBelowRequiredBreak() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Paused,
                pauseStartedMinute = 12 * 60,
                breakMinutes = 10,
                config = WorkDayConfig(requiredBreakMinutes = 30)
            ),
            snapshot = snapshot(minuteOfDay = 12 * 60 + 5)
        )

        assertEquals(15, plan.breakReminderDelayMinutes)
        assertNull(plan.dailyTargetReminderDelayMinutes)
        assertNull(plan.weeklyTargetReminderDelayMinutes)
    }

    @Test
    fun suppressesBreakReminderWhenRequiredBreakAlreadyMetBeforeThisPause() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Paused,
                pauseStartedMinute = 12 * 60,
                breakMinutes = 35,
                config = WorkDayConfig(requiredBreakMinutes = 30)
            ),
            snapshot = snapshot(minuteOfDay = 12 * 60 + 5)
        )

        assertNull(plan.breakReminderDelayMinutes)
    }

    @Test
    fun suppressesBreakReminderWhenCurrentPauseAlreadyReachedRequiredBreak() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Paused,
                pauseStartedMinute = 12 * 60,
                breakMinutes = 10,
                config = WorkDayConfig(requiredBreakMinutes = 30)
            ),
            snapshot = snapshot(minuteOfDay = 12 * 60 + 20)
        )

        assertNull(plan.breakReminderDelayMinutes)
    }

    @Test
    fun doesNotScheduleBreakReminderWhileWorking() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                config = WorkDayConfig(requiredBreakMinutes = 30)
            ),
            snapshot = snapshot(minuteOfDay = 9 * 60 + 30)
        )

        assertNull(plan.breakReminderDelayMinutes)
    }

    @Test
    fun schedulesDailyTargetReminderWhenWorkingAndBelowTarget() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                workedMinutes = 6 * 60,
                config = WorkDayConfig(dailyTargetMinutes = 8 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 9 * 60)
        )

        assertEquals(2 * 60, plan.dailyTargetReminderDelayMinutes)
    }

    @Test
    fun suppressesDailyTargetReminderWhenAlreadyMetBeforeThisSession() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                workedMinutes = 9 * 60,
                config = WorkDayConfig(dailyTargetMinutes = 8 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 9 * 60)
        )

        assertNull(plan.dailyTargetReminderDelayMinutes)
    }

    @Test
    fun suppressesDailyTargetReminderWhenCurrentSessionAlreadyReachedTarget() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                workedMinutes = 6 * 60,
                config = WorkDayConfig(dailyTargetMinutes = 8 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 11 * 60)
        )

        assertNull(plan.dailyTargetReminderDelayMinutes)
    }

    @Test
    fun schedulesWeeklyTargetReminderWhenWorkingAndBelowWeeklyTarget() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                workedMinutes = 0,
                weeklyWorkedBeforeTodayMinutes = 32 * 60,
                config = WorkDayConfig(weeklyTargetMinutes = 40 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 9 * 60)
        )

        assertEquals(8 * 60, plan.weeklyTargetReminderDelayMinutes)
    }

    @Test
    fun suppressesWeeklyTargetReminderOnceAlreadyCrossedOnAPreviousDay() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 9 * 60,
                workedMinutes = 0,
                weeklyWorkedBeforeTodayMinutes = 41 * 60,
                config = WorkDayConfig(weeklyTargetMinutes = 40 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 9 * 60)
        )

        assertNull(plan.weeklyTargetReminderDelayMinutes)
    }

    @Test
    fun suppressesWeeklyTargetReminderOnceAlreadyCrossedToday() {
        val plan = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 15 * 60,
                workedMinutes = 8 * 60,
                weeklyWorkedBeforeTodayMinutes = 32 * 60,
                config = WorkDayConfig(weeklyTargetMinutes = 40 * 60)
            ),
            snapshot = snapshot(minuteOfDay = 15 * 60)
        )

        assertNull(plan.weeklyTargetReminderDelayMinutes)
    }

    @Test
    fun everythingIsNullWhenNotStarted() {
        val plan = calculator.calculate(
            day = WorkDay(status = WorkStatus.NotStarted),
            snapshot = snapshot(minuteOfDay = 8 * 60)
        )

        assertNull(plan.breakReminderDelayMinutes)
        assertNull(plan.dailyTargetReminderDelayMinutes)
        assertNull(plan.weeklyTargetReminderDelayMinutes)
    }

    private fun snapshot(minuteOfDay: Int) = TimeSnapshot(date = date, minuteOfDay = minuteOfDay)
}
