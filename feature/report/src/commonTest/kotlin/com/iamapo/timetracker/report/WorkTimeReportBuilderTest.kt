package com.iamapo.timetracker.report

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkTimeReportBuilderTest {
    @Test
    fun buildsRowsAndTotalsForSelectedWeek() {
        val monday = LocalDate(2026, 8, 31)
        val history = WorkHistory().copy(
            days = mapOf(
                monday to WorkDay(
                    status = WorkStatus.Finished,
                    workedMinutes = 8 * 60 + 15,
                    breakMinutes = 30
                ),
                LocalDate(2026, 9, 1) to WorkDay(
                    kind = WorkDayKind.Vacation,
                    status = WorkStatus.Finished,
                    workedMinutes = 8 * 60
                )
            )
        )

        val report = WorkTimeReportBuilder().build(
            history = history,
            snapshot = TimeSnapshot(LocalDate(2026, 9, 2), minuteOfDay = 12 * 60),
            period = ReportPeriod(ReportPeriodType.Week, monday)
        )

        assertEquals(3, report.rows.size)
        assertEquals(16 * 60 + 15, report.workedMinutes)
        assertEquals(24 * 60, report.targetMinutes)
        assertEquals(-7 * 60 - 45, report.balanceMinutes)
        assertEquals(ReportDayType.Vacation, report.rows[1].dayType)
        assertEquals(ReportDayType.Missing, report.rows[2].dayType)
    }
}
