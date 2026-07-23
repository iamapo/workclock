package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

data class WeeklyBalance(
    val workedMinutes: Int,
    val expectedMinutes: Int,
    val balanceMinutes: Int,
    val carryMinutes: Int
)

class WeeklyBalanceCalculator {
    fun calculate(
        day: WorkDay,
        date: LocalDate,
        todayWorkedMinutes: Int
    ): WeeklyBalance {
        val expectedBeforeTodayMinutes = day.weeklyExpectedBeforeTodayMinutes
            ?: day.config.weeklyTargetMinutes *
                (date.dayOfWeek.isoDayNumber - 1).coerceIn(0, WorkdaysPerWeek) /
                WorkdaysPerWeek
        val workedMinutes = day.weeklyWorkedBeforeTodayMinutes + todayWorkedMinutes
        val expectedMinutes = expectedBeforeTodayMinutes + day.config.dailyTargetMinutes
        val balanceMinutes = day.weeklyBalanceCarryMinutes + workedMinutes - expectedMinutes

        val includesToday = day.status == WorkStatus.Finished
        val workedOnCompletedDays = day.weeklyWorkedBeforeTodayMinutes +
            if (includesToday) todayWorkedMinutes else 0
        val expectedMinutesForCompletedDays = expectedBeforeTodayMinutes +
            if (includesToday) day.config.dailyTargetMinutes else 0
        val carryMinutes = day.weeklyBalanceCarryMinutes +
            workedOnCompletedDays - expectedMinutesForCompletedDays

        return WeeklyBalance(
            workedMinutes = workedMinutes,
            expectedMinutes = expectedMinutes,
            balanceMinutes = balanceMinutes,
            carryMinutes = carryMinutes
        )
    }

    private companion object {
        const val WorkdaysPerWeek = 5
    }
}
