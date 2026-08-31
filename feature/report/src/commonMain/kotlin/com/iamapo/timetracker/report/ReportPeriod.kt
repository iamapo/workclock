package com.iamapo.timetracker.report

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class ReportPeriodType {
    Week,
    Month
}

data class ReportPeriod(
    val type: ReportPeriodType,
    val anchorDate: LocalDate
) {
    val startDate: LocalDate = when (type) {
        ReportPeriodType.Week -> anchorDate - DatePeriod(days = anchorDate.dayOfWeek.isoDayNumber - 1)
        ReportPeriodType.Month -> LocalDate(anchorDate.year, anchorDate.month, 1)
    }

    val endDate: LocalDate = when (type) {
        ReportPeriodType.Week -> startDate + DatePeriod(days = 6)
        ReportPeriodType.Month -> startDate + DatePeriod(months = 1) - DatePeriod(days = 1)
    }

    fun previous(): ReportPeriod = when (type) {
        ReportPeriodType.Week -> copy(anchorDate = startDate - DatePeriod(days = 7))
        ReportPeriodType.Month -> copy(anchorDate = startDate - DatePeriod(months = 1))
    }

    fun next(): ReportPeriod = when (type) {
        ReportPeriodType.Week -> copy(anchorDate = startDate + DatePeriod(days = 7))
        ReportPeriodType.Month -> copy(anchorDate = startDate + DatePeriod(months = 1))
    }

    fun withType(newType: ReportPeriodType): ReportPeriod = ReportPeriod(newType, anchorDate)
}
