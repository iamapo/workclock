package com.iamapo.timetracker.report

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class WorkTimePdfGeneratorTest {
    @Test
    fun generatesPortablePdfWithExpectedStructure() {
        val report = WorkTimeReport(
            period = ReportPeriod(ReportPeriodType.Month, LocalDate(2026, 8, 1)),
            generatedOn = LocalDate(2026, 8, 31),
            rows = (1..31).map { day ->
                WorkTimeReportRow(
                    date = LocalDate(2026, 8, day),
                    dayType = if (day % 7 in 1..5) ReportDayType.Work else ReportDayType.DayOff,
                    startMinute = 8 * 60 + 30,
                    endMinute = 17 * 60,
                    workedMinutes = 8 * 60,
                    breakMinutes = 30,
                    targetMinutes = 8 * 60,
                    balanceMinutes = 0
                )
            }
        )

        val pdf = WorkTimePdfGenerator().generate(report)

        assertTrue(pdf.startsWith("%PDF-1.4"))
        assertContains(pdf, "xref")
        assertTrue(pdf.endsWith("%%EOF\n"))
        assertTrue(pdf.all { it.code in 0..127 })
    }
}
