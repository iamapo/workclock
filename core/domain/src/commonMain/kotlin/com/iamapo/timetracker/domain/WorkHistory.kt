package com.iamapo.timetracker.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class WorkHistory(
    val defaultConfig: WorkDayConfig = WorkDayConfig(),
    val lockScreenStatusEnabled: Boolean = false,
    val workSchedule: WorkSchedule = WorkSchedule.fromConfig(defaultConfig),
    val automaticHolidaysEnabled: Boolean = false,
    val holidayFederalState: GermanFederalState? = null,
    val days: Map<LocalDate, WorkDay> = emptyMap()
) {
    fun dayFor(date: LocalDate): WorkDay {
        val dailyTargetMinutes = targetMinutes(date)
        val config = defaultConfig.copy(
            dailyTargetMinutes = dailyTargetMinutes,
            requiredBreakMinutes = if (dailyTargetMinutes == 0) 0 else defaultConfig.requiredBreakMinutes,
            weeklyTargetMinutes = targetMinutesForWeek(date)
        )
        return (days[date] ?: WorkDay()).copy(config = config)
    }

    fun dayWithWeeklySummary(date: LocalDate): WorkDay =
        dayFor(date).copy(
            weeklyWorkedBeforeTodayMinutes = workedBefore(date),
            weeklyBalanceCarryMinutes = balanceBeforeWeek(date),
            weeklyExpectedBeforeTodayMinutes = expectedBefore(date)
        )

    fun scheduledTargetMinutes(date: LocalDate): Int = workSchedule.targetMinutes(date.dayOfWeek)

    fun targetMinutes(date: LocalDate): Int = if (holiday(date) != null) {
        0
    } else {
        scheduledTargetMinutes(date)
    }

    fun holiday(date: LocalDate): GermanHoliday? {
        val federalState = holidayFederalState ?: return null
        if (!automaticHolidaysEnabled) return null
        return GermanHolidayCalendar.holiday(date, federalState)
    }

    fun targetMinutesForWeek(date: LocalDate): Int {
        val weekStart = startOfWeek(date)
        return (0 until DaysPerWeek).sumOf { index ->
            targetMinutes(weekStart + DatePeriod(days = index))
        }
    }

    fun withDay(date: LocalDate, day: WorkDay): WorkHistory =
        copy(days = days + (date to day))

    fun withoutDay(date: LocalDate): WorkHistory =
        copy(days = days - date)

    fun withDefaultConfig(config: WorkDayConfig): WorkHistory =
        copy(defaultConfig = config)

    private fun workedBefore(date: LocalDate): Int {
        val weekStart = startOfWeek(date)
        return days.entries
            .filter { (dayDate, _) -> dayDate >= weekStart && dayDate < date }
            .sumOf { (_, day) -> day.workedMinutes }
    }

    private fun expectedBefore(date: LocalDate): Int {
        val weekStart = startOfWeek(date)
        var current = weekStart
        var expectedMinutes = 0
        while (current < date) {
            expectedMinutes += targetMinutes(current)
            current += DatePeriod(days = 1)
        }
        return expectedMinutes
    }

    private fun balanceBeforeWeek(date: LocalDate): Int {
        val currentWeekStart = startOfWeek(date)
        val firstRecordedDate = days.keys.minOrNull() ?: return 0
        var weekStart = startOfWeek(firstRecordedDate)
        var balanceMinutes = 0

        while (weekStart < currentWeekStart) {
            val nextWeekStart = weekStart + OneWeek
            val weekEntries = days.entries
                .filter { (dayDate, _) -> dayDate >= weekStart && dayDate < nextWeekStart }

            if (weekEntries.isNotEmpty()) {
                balanceMinutes += weekEntries.sumOf { (_, day) -> day.workedMinutes } -
                    targetMinutesForWeek(weekStart)
            }
            weekStart = nextWeekStart
        }

        return balanceMinutes
    }

    private fun startOfWeek(date: LocalDate): LocalDate =
        date - DatePeriod(days = date.dayOfWeek.isoDayNumber - 1)

    private companion object {
        val OneWeek = DatePeriod(days = 7)
        const val DaysPerWeek = 7
    }
}
