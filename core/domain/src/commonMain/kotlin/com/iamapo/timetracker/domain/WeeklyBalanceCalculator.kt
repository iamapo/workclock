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
        val dayOfWeek = date.dayOfWeek.isoDayNumber
        val expectedWorkdays = dayOfWeek.coerceIn(1, WorkdaysPerWeek)
        val workedMinutes = day.weeklyWorkedBeforeTodayMinutes + todayWorkedMinutes
        val expectedMinutes = day.config.weeklyTargetMinutes * expectedWorkdays / WorkdaysPerWeek
        val balanceMinutes = day.weeklyBalanceCarryMinutes + workedMinutes - expectedMinutes

        val elapsedWorkdays = (dayOfWeek - 1).coerceIn(0, WorkdaysPerWeek)
        val includesToday = day.status == WorkStatus.Finished && dayOfWeek <= WorkdaysPerWeek
        val completedWorkdays = elapsedWorkdays + if (includesToday) 1 else 0
        val workedOnCompletedDays = day.weeklyWorkedBeforeTodayMinutes +
            if (includesToday) todayWorkedMinutes else 0
        val expectedMinutesForCompletedDays =
            day.config.weeklyTargetMinutes * completedWorkdays / WorkdaysPerWeek
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
