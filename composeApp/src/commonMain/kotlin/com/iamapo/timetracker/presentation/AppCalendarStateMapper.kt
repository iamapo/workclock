package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WeeklyBalance
import com.iamapo.timetracker.domain.WeeklyBalanceCalculator
import com.iamapo.timetracker.domain.WorkDaySummaryCalculator
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarUiState
import com.iamapo.timetracker.presentation.state.WeekDayProgressUiModel
import com.iamapo.timetracker.presentation.state.WeekOverviewUiModel
import com.iamapo.timetracker.resources.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs

object AppCalendarStateMapper : CalendarStateMapper {
    private val monthMapper = CalendarMonthMapper()
    private val summaryCalculator = WorkDaySummaryCalculator()
    private val weeklyBalanceCalculator = WeeklyBalanceCalculator()

    override fun map(history: WorkHistory, snapshot: TimeSnapshot): CalendarUiState {
        val day = history.dayWithWeeklySummary(snapshot.date)
        val summary = summaryCalculator.calculate(day, snapshot)
        val weeklyBalance = weeklyBalanceCalculator.calculate(day, snapshot.date, summary.workedMinutes)

        return CalendarUiState(
            monthTitle = TimeTextFormatter.monthTitle(snapshot.date),
            days = monthMapper.map(
                snapshot.date,
                summary.endMinute,
                history
            ),
            previewDays = monthMapper.mapPreview(
                snapshot.date,
                summary.endMinute,
                history
            ),
            weekOverview = weekOverview(
                snapshot,
                history,
                summary.workedMinutes,
                weeklyBalance
            ),
            dailyTarget = TimeTextFormatter.clockLikeDuration(day.config.dailyTargetMinutes),
            plannedWeek = TimeTextFormatter.clockLikeDuration(history.targetMinutesForWeek(snapshot.date)),
            reachedWeek = TimeTextFormatter.clockLikeDuration(weeklyBalance.workedMinutes)
        )
    }

    private fun weekOverview(
        snapshot: TimeSnapshot,
        history: WorkHistory,
        todayWorkedMinutes: Int,
        weeklyBalance: WeeklyBalance
    ): WeekOverviewUiModel {
        val weekStart = snapshot.date - DatePeriod(days = snapshot.date.dayOfWeek.isoDayNumber - 1)
        val days = (0 until WorkdaysPerWeek).map { index ->
            val date = weekStart + DatePeriod(days = index)
            val workedMinutes = if (date == snapshot.date) todayWorkedMinutes else history.days[date]?.workedMinutes ?: 0
            val targetMinutes = history.targetMinutes(date)
            WeekDayProgressUiModel(
                label = weekdayShortLabel(index),
                value = TimeTextFormatter.calendarDuration(workedMinutes),
                progress = if (targetMinutes == 0) 0f else workedMinutes.toFloat() / targetMinutes,
                isToday = date == snapshot.date
            )
        }
        return WeekOverviewUiModel(
            reached = TimeTextFormatter.clockLikeDuration(weeklyBalance.workedMinutes),
            balance = balanceLabel(weeklyBalance.balanceMinutes),
            isPositiveBalance = weeklyBalance.balanceMinutes >= 0,
            carry = balanceLabel(weeklyBalance.carryMinutes),
            isPositiveCarry = weeklyBalance.carryMinutes >= 0,
            days = days
        )
    }

    private fun balanceLabel(minutes: Int): String {
        val prefix = when {
            minutes > 0 -> "+"
            minutes < 0 -> "-"
            else -> ""
        }
        return prefix + TimeTextFormatter.duration(abs(minutes))
    }

    private fun weekdayShortLabel(index: Int): String = localized(
        listOf(
            Res.string.monday_short,
            Res.string.tuesday_short,
            Res.string.wednesday_short,
            Res.string.thursday_short,
            Res.string.friday_short
        )[index]
    )

    private const val WorkdaysPerWeek = 5
}
