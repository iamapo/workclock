package com.iamapo.timetracker.report

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkDaySummaryCalculator
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class WorkTimeReportBuilder(
    private val summaryCalculator: WorkDaySummaryCalculator = WorkDaySummaryCalculator()
) {
    fun build(
        history: WorkHistory,
        snapshot: TimeSnapshot,
        period: ReportPeriod
    ): WorkTimeReport {
        val effectiveEnd = minOf(period.endDate, snapshot.date)
        val rows = buildList {
            var date = period.startDate
            while (date <= effectiveEnd) {
                add(rowFor(history, snapshot, date))
                date += DatePeriod(days = 1)
            }
        }
        return WorkTimeReport(period = period, generatedOn = snapshot.date, rows = rows)
    }

    private fun rowFor(
        history: WorkHistory,
        snapshot: TimeSnapshot,
        date: LocalDate
    ): WorkTimeReportRow {
        val storedDay = history.days[date]
        val day = history.dayFor(date)
        val workedMinutes = if (date == snapshot.date && day.status != WorkStatus.Finished) {
            summaryCalculator.calculate(day, snapshot).workedMinutes
        } else {
            day.workedMinutes
        }
        val breakMinutes = if (date == snapshot.date && day.status == WorkStatus.Paused) {
            summaryCalculator.calculate(day, snapshot).breakMinutes
        } else {
            day.breakMinutes
        }
        val targetMinutes = history.targetMinutes(date)
        val endMinute = day.events.lastOrNull()?.minuteOfDay
            ?.takeIf { day.kind == WorkDayKind.Work && day.status == WorkStatus.Finished }

        return WorkTimeReportRow(
            date = date,
            dayType = when {
                storedDay?.kind == WorkDayKind.Vacation -> ReportDayType.Vacation
                storedDay?.kind == WorkDayKind.Sick -> ReportDayType.Sick
                storedDay != null -> ReportDayType.Work
                history.holiday(date) != null -> ReportDayType.Holiday
                targetMinutes == 0 -> ReportDayType.DayOff
                else -> ReportDayType.Missing
            },
            startMinute = day.startMinute,
            endMinute = endMinute,
            workedMinutes = workedMinutes,
            breakMinutes = breakMinutes,
            targetMinutes = targetMinutes,
            balanceMinutes = workedMinutes - targetMinutes
        )
    }
}
