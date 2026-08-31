package com.iamapo.timetracker.report

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportPeriodTest {
    @Test
    fun weekStartsOnMondayAndEndsOnSunday() {
        val period = ReportPeriod(ReportPeriodType.Week, LocalDate(2026, 8, 31))

        assertEquals(LocalDate(2026, 8, 31), period.startDate)
        assertEquals(LocalDate(2026, 9, 6), period.endDate)
    }

    @Test
    fun monthUsesCalendarBoundaries() {
        val period = ReportPeriod(ReportPeriodType.Month, LocalDate(2026, 2, 12))

        assertEquals(LocalDate(2026, 2, 1), period.startDate)
        assertEquals(LocalDate(2026, 2, 28), period.endDate)
    }
}
