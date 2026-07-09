package com.iamapo.timetracker.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

data class WorkHistory(
    val defaultConfig: WorkDayConfig = WorkDayConfig(),
    val lockScreenStatusEnabled: Boolean = false,
    val days: Map<LocalDate, WorkDay> = emptyMap()
) {
    fun dayFor(date: LocalDate): WorkDay = days[date] ?: WorkDay(config = defaultConfig)

    fun dayWithWeeklySummary(date: LocalDate): WorkDay =
        dayFor(date).copy(weeklyWorkedBeforeTodayMinutes = workedBefore(date))

    fun withDay(date: LocalDate, day: WorkDay): WorkHistory =
        copy(days = days + (date to day))

    fun withoutDay(date: LocalDate): WorkHistory =
        copy(days = days - date)

    fun withDefaultConfig(config: WorkDayConfig): WorkHistory =
        copy(defaultConfig = config)

    private fun workedBefore(date: LocalDate): Int {
        val weekStart = date - DatePeriod(days = date.dayOfWeek.isoDayNumber - 1)
        return days.entries
            .filter { (dayDate, _) -> dayDate >= weekStart && dayDate < date }
            .sumOf { (_, day) -> day.workedMinutes }
    }
}
