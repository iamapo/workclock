package com.iamapo.timetracker.report

import java.io.File
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class WorkTimePdfPreviewTest {
    @Test
    fun writesVisualPreviewWhenRequested() {
        val outputPath = System.getenv("WORKCLOCK_REPORT_PREVIEW") ?: return
        val report = WorkTimeReport(
            period = ReportPeriod(ReportPeriodType.Month, LocalDate(2026, 8, 1)),
            generatedOn = LocalDate(2026, 8, 31),
            rows = (1..31).map { day ->
                val weekday = LocalDate(2026, 8, day).dayOfWeek.ordinal
                val isWorkday = weekday < 5
                val type = when (day) {
                    10 -> ReportDayType.Vacation
                    21 -> ReportDayType.Sick
                    else -> if (isWorkday) ReportDayType.Work else ReportDayType.DayOff
                }
                val worked = when (type) {
                    ReportDayType.Work -> if (day % 3 == 0) 7 * 60 + 45 else 8 * 60 + 15
                    ReportDayType.Vacation, ReportDayType.Sick -> 8 * 60
                    else -> 0
                }
                WorkTimeReportRow(
                    date = LocalDate(2026, 8, day),
                    dayType = type,
                    startMinute = (8 * 60 + 30).takeIf { type == ReportDayType.Work },
                    endMinute = (17 * 60).takeIf { type == ReportDayType.Work },
                    workedMinutes = worked,
                    breakMinutes = 30.takeIf { type == ReportDayType.Work } ?: 0,
                    targetMinutes = if (isWorkday) 8 * 60 else 0,
                    balanceMinutes = worked - if (isWorkday) 8 * 60 else 0
                )
            }
        )

        val output = File(outputPath)
        output.parentFile?.mkdirs()
        output.writeBytes(WorkTimePdfGenerator().generate(report).encodeToByteArray())
        assertTrue(output.isFile && output.length() > 1_000)
    }
}
