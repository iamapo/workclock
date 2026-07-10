package com.iamapo.timetracker.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class WorkHistory(
    val defaultConfig: WorkDayConfig = WorkDayConfig(),
    val lockScreenStatusEnabled: Boolean = false,
    val days: Map<LocalDate, WorkDay> = emptyMap()
) {
    fun dayFor(date: LocalDate): WorkDay = days[date] ?: WorkDay(config = defaultConfig)

    fun dayWithWeeklySummary(date: LocalDate): WorkDay =
        dayFor(date).copy(
            weeklyWorkedBeforeTodayMinutes = workedBefore(date),
            weeklyBalanceCarryMinutes = balanceBeforeWeek(date)
        )

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

    private fun balanceBeforeWeek(date: LocalDate): Int {
        val currentWeekStart = startOfWeek(date)
        val firstRecordedDate = days.keys.minOrNull() ?: return 0
        var weekStart = startOfWeek(firstRecordedDate)
        var weeklyTargetMinutes = days.entries
            .filter { (dayDate, _) -> dayDate >= weekStart && dayDate < weekStart + OneWeek }
            .maxByOrNull { (dayDate, _) -> dayDate }
            ?.value
            ?.config
            ?.weeklyTargetMinutes
            ?: defaultConfig.weeklyTargetMinutes
        var balanceMinutes = 0

        while (weekStart < currentWeekStart) {
            val nextWeekStart = weekStart + OneWeek
            val weekEntries = days.entries
                .filter { (dayDate, _) -> dayDate >= weekStart && dayDate < nextWeekStart }

            if (weekEntries.isNotEmpty()) {
                weekEntries.maxByOrNull { (dayDate, _) -> dayDate }
                    ?.value
                    ?.config
                    ?.weeklyTargetMinutes
                    ?.let { weeklyTargetMinutes = it }

                balanceMinutes += weekEntries.sumOf { (_, day) -> day.workedMinutes } - weeklyTargetMinutes
            }
            weekStart = nextWeekStart
        }

        return balanceMinutes
    }

    private fun startOfWeek(date: LocalDate): LocalDate =
        date - DatePeriod(days = date.dayOfWeek.isoDayNumber - 1)

    private companion object {
        val OneWeek = DatePeriod(days = 7)
    }
}
