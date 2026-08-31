package com.iamapo.timetracker.report

import kotlinx.datetime.LocalDate

enum class ReportDayType {
    Work,
    Vacation,
    Sick,
    Holiday,
    DayOff,
    Missing
}

data class WorkTimeReportRow(
    val date: LocalDate,
    val dayType: ReportDayType,
    val startMinute: Int?,
    val endMinute: Int?,
    val workedMinutes: Int,
    val breakMinutes: Int,
    val targetMinutes: Int,
    val balanceMinutes: Int
)

data class WorkTimeReport(
    val period: ReportPeriod,
    val generatedOn: LocalDate,
    val rows: List<WorkTimeReportRow>
) {
    val workedMinutes: Int = rows.sumOf(WorkTimeReportRow::workedMinutes)
    val breakMinutes: Int = rows.sumOf(WorkTimeReportRow::breakMinutes)
    val targetMinutes: Int = rows.sumOf(WorkTimeReportRow::targetMinutes)
    val balanceMinutes: Int = rows.sumOf(WorkTimeReportRow::balanceMinutes)
}
