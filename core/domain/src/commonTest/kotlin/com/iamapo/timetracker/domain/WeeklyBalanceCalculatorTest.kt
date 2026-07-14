package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WeeklyBalanceCalculatorTest {
    private val calculator = WeeklyBalanceCalculator()

    @Test
    fun includesOvertimeFromCompletedMondayInCarry() {
        val balance = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 8 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            date = LocalDate(2026, 7, 13),
            todayWorkedMinutes = 8 * 60 + 30
        )

        assertEquals(8 * 60 + 30, balance.workedMinutes)
        assertEquals(8 * 60, balance.expectedMinutes)
        assertEquals(60, balance.balanceMinutes)
        assertEquals(60, balance.carryMinutes)
    }

    @Test
    fun excludesOngoingDayFromCarry() {
        val balance = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Working,
                workedMinutes = 4 * 60,
                weeklyWorkedBeforeTodayMinutes = 8 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            date = LocalDate(2026, 7, 14),
            todayWorkedMinutes = 4 * 60
        )

        assertEquals(12 * 60 + 30, balance.workedMinutes)
        assertEquals(16 * 60, balance.expectedMinutes)
        assertEquals(-3 * 60, balance.balanceMinutes)
        assertEquals(60, balance.carryMinutes)
    }

    @Test
    fun appliesUndertimeFromCompletedDayToCarry() {
        val balance = calculator.calculate(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 7 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            date = LocalDate(2026, 7, 13),
            todayWorkedMinutes = 7 * 60 + 30
        )

        assertEquals(0, balance.balanceMinutes)
        assertEquals(0, balance.carryMinutes)
    }
}
