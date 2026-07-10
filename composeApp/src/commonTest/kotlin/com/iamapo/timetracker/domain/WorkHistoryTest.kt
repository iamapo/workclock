package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkHistoryTest {
    @Test
    fun calculatesWorkedMinutesBeforeDateInSameWeek() {
        val history = WorkHistory(
            days = mapOf(
                LocalDate(2026, 7, 3) to WorkDay(workedMinutes = 8 * 60),
                LocalDate(2026, 7, 6) to WorkDay(workedMinutes = 7 * 60 + 30),
                LocalDate(2026, 7, 7) to WorkDay(workedMinutes = 2 * 60),
                LocalDate(2026, 7, 8) to WorkDay(workedMinutes = 5 * 60)
            )
        )

        val day = history.dayWithWeeklySummary(LocalDate(2026, 7, 7))

        assertEquals(7 * 60 + 30, day.weeklyWorkedBeforeTodayMinutes)
    }

    @Test
    fun usesDefaultConfigForNewDays() {
        val history = WorkHistory(
            defaultConfig = WorkDayConfig(requiredBreakMinutes = 35)
        )

        val day = history.dayFor(LocalDate(2026, 7, 7))

        assertEquals(35, day.config.requiredBreakMinutes)
    }

    @Test
    fun carriesPositiveWeeklyBalanceIntoFollowingWeek() {
        val history = WorkHistory(
            days = mapOf(
                LocalDate(2026, 7, 6) to WorkDay(workedMinutes = 8 * 60),
                LocalDate(2026, 7, 7) to WorkDay(workedMinutes = 8 * 60),
                LocalDate(2026, 7, 8) to WorkDay(workedMinutes = 8 * 60),
                LocalDate(2026, 7, 9) to WorkDay(workedMinutes = 8 * 60),
                LocalDate(2026, 7, 10) to WorkDay(workedMinutes = 8 * 60 + 30)
            )
        )

        val monday = history.dayWithWeeklySummary(LocalDate(2026, 7, 13))

        assertEquals(30, monday.weeklyBalanceCarryMinutes)
    }

    @Test
    fun carriesNegativeAndAccumulatedWeeklyBalance() {
        val history = WorkHistory(
            days = mapOf(
                LocalDate(2026, 6, 29) to WorkDay(workedMinutes = 40 * 60 + 30),
                LocalDate(2026, 7, 6) to WorkDay(workedMinutes = 39 * 60 + 15)
            )
        )

        val monday = history.dayWithWeeklySummary(LocalDate(2026, 7, 13))

        assertEquals(-15, monday.weeklyBalanceCarryMinutes)
    }

    @Test
    fun emptyWeeksDoNotCreateAnArtificialDeficit() {
        val history = WorkHistory(
            days = mapOf(
                LocalDate(2026, 6, 29) to WorkDay(workedMinutes = 40 * 60 + 30)
            )
        )

        val mondayAfterEmptyWeek = history.dayWithWeeklySummary(LocalDate(2026, 7, 13))

        assertEquals(30, mondayAfterEmptyWeek.weeklyBalanceCarryMinutes)
    }
}
