package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WeeklyBalance
import com.iamapo.timetracker.domain.WeeklyBalanceCalculator
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.MetricUiModel
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.presentation.state.TargetItemUiModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.presentation.state.WeekDayProgressUiModel
import com.iamapo.timetracker.presentation.state.WeekOverviewUiModel
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs
import workclock.composeapp.generated.resources.*

object TimeTrackerUiStateMapper {
    private val summaryCalculator = WorkDaySummaryCalculator()
    private val weeklyBalanceCalculator = WeeklyBalanceCalculator()
    private val timelineMapper = TimelineMapper()
    private val calendarMonthMapper = CalendarMonthMapper()

    fun map(
        day: WorkDay,
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay> = emptyMap(),
        lockScreenStatusEnabled: Boolean = false
    ): TimeTrackerUiState {
        val summary = summaryCalculator.calculate(day, snapshot)
        val weeklyBalance = weeklyBalanceCalculator.calculate(
            day = day,
            date = snapshot.date,
            todayWorkedMinutes = summary.workedMinutes
        )
        val displayedBreakMinutes = maxOf(day.config.requiredBreakMinutes, summary.breakMinutes)
        val weekOverview = weekOverview(
            snapshot = snapshot,
            history = history,
            todayWorkedMinutes = summary.workedMinutes,
            weeklyBalance = weeklyBalance,
            dailyTargetMinutes = day.config.dailyTargetMinutes
        )

        return TimeTrackerUiState(
            dateLabel = TimeTextFormatter.dateLabel(snapshot.date),
            title = localized(Res.string.app_name),
            statusLabel = statusLabel(day),
            workedTime = TimeTextFormatter.duration(summary.workedMinutes),
            remainingTime = TimeTextFormatter.duration(summary.remainingWorkMinutes),
            endTime = localized(Res.string.time_with_clock, TimeTextFormatter.clock(summary.endMinute)),
            breakRequirementLabel = localized(Res.string.including_break, TimeTextFormatter.duration(displayedBreakMinutes)),
            progress = summary.progress,
            primaryActionLabel = primaryActionLabel(day.status),
            secondaryActionLabel = secondaryActionLabel(day.status),
            targets = listOf(
                TargetItemUiModel(localized(Res.string.target_daily), TimeTextFormatter.shortDuration(day.config.dailyTargetMinutes)),
                TargetItemUiModel(localized(Res.string.break_label), TimeTextFormatter.duration(day.config.requiredBreakMinutes)),
                TargetItemUiModel(localized(Res.string.target_weekly), TimeTextFormatter.shortDuration(day.config.weeklyTargetMinutes))
            ),
            metrics = listOf(
                MetricUiModel(
                    label = localized(Res.string.worked),
                    value = TimeTextFormatter.compactDuration(summary.workedMinutes),
                    hint = localized(if (summary.workedMinutes == 0) Res.string.not_started_yet else Res.string.recorded_today),
                    emphasized = summary.workedMinutes > 0
                ),
                MetricUiModel(
                    label = localized(Res.string.break_label),
                    value = TimeTextFormatter.compactDuration(summary.breakMinutes),
                    hint = if (summary.missingBreakMinutes == 0) {
                        localized(Res.string.break_met)
                    } else {
                        localized(Res.string.missing_minutes, TimeTextFormatter.duration(summary.missingBreakMinutes))
                    }
                ),
                MetricUiModel(
                    label = localized(Res.string.week),
                    value = TimeTextFormatter.compactDuration(weeklyBalance.workedMinutes),
                    hint = localized(Res.string.balance_suffix, weekOverview.balance)
                )
            ),
            timeline = timelineMapper.map(day, summary.endMinute),
            monthTitle = TimeTextFormatter.monthTitle(snapshot.date),
            calendarDays = calendarMonthMapper.map(
                date = snapshot.date,
                endMinute = summary.endMinute,
                dailyTargetMinutes = day.config.dailyTargetMinutes,
                history = history
            ),
            calendarPreviewDays = calendarMonthMapper.mapPreview(
                date = snapshot.date,
                endMinute = summary.endMinute,
                dailyTargetMinutes = day.config.dailyTargetMinutes,
                history = history
            ),
            plannedWeek = TimeTextFormatter.clockLikeDuration(day.config.weeklyTargetMinutes),
            reachedWeek = TimeTextFormatter.clockLikeDuration(weeklyBalance.workedMinutes),
            weekOverview = weekOverview,
            settings = SettingsUiModel(
                dailyTarget = TimeTextFormatter.clockLikeDuration(day.config.dailyTargetMinutes),
                canDecreaseDailyTarget = day.config.dailyTargetMinutes > MinDailyTargetMinutes,
                canIncreaseDailyTarget = day.config.dailyTargetMinutes < MaxDailyTargetMinutes,
                requiredBreak = TimeTextFormatter.duration(day.config.requiredBreakMinutes),
                canDecreaseRequiredBreak = day.config.requiredBreakMinutes > MinRequiredBreakMinutes,
                canIncreaseRequiredBreak = day.config.requiredBreakMinutes < MaxRequiredBreakMinutes,
                weeklyTarget = TimeTextFormatter.clockLikeDuration(day.config.weeklyTargetMinutes),
                canDecreaseWeeklyTarget = day.config.weeklyTargetMinutes > MinWeeklyTargetMinutes,
                canIncreaseWeeklyTarget = day.config.weeklyTargetMinutes < MaxWeeklyTargetMinutes,
                lockScreenStatusEnabled = lockScreenStatusEnabled
            ),
            watchState = watchState(day.status),
            watchRemaining = if (day.status == WorkStatus.Finished) {
                "0:00"
            } else {
                TimeTextFormatter.watchDuration(summary.remainingWorkMinutes)
            },
            watchCaption = when (day.status) {
                WorkStatus.NotStarted -> localized(Res.string.watch_ready)
                WorkStatus.Finished -> localized(Res.string.watch_finished, TimeTextFormatter.clock(summary.endMinute))
                WorkStatus.Working,
                WorkStatus.Paused -> localized(Res.string.watch_until, TimeTextFormatter.clock(summary.endMinute))
            }
        )
    }

    private fun statusLabel(day: WorkDay): String = when (day.status) {
        WorkStatus.NotStarted -> localized(Res.string.ready_to_start)
        WorkStatus.Working -> day.startMinute?.let { localized(Res.string.working_since, TimeTextFormatter.clock(it)) } ?: localized(Res.string.working)
        WorkStatus.Paused -> localized(Res.string.break_running)
        WorkStatus.Finished -> localized(Res.string.workday_finished)
    }

    private fun primaryActionLabel(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> localized(Res.string.action_start)
        WorkStatus.Working -> localized(Res.string.action_break)
        WorkStatus.Paused -> localized(Res.string.action_resume)
        WorkStatus.Finished -> localized(Res.string.action_new_day)
    }

    private fun secondaryActionLabel(status: WorkStatus): String? = when (status) {
        WorkStatus.Working,
        WorkStatus.Paused -> localized(Res.string.action_finish)
        WorkStatus.NotStarted,
        WorkStatus.Finished -> null
    }

    private fun watchState(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> localized(Res.string.state_ready)
        WorkStatus.Working -> localized(Res.string.state_active)
        WorkStatus.Paused -> localized(Res.string.state_break)
        WorkStatus.Finished -> localized(Res.string.state_finished)
    }

    private fun weekOverview(
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay>,
        todayWorkedMinutes: Int,
        weeklyBalance: WeeklyBalance,
        dailyTargetMinutes: Int
    ): WeekOverviewUiModel {
        val weekStart = snapshot.date - DatePeriod(days = snapshot.date.dayOfWeek.isoDayNumber - 1)
        val days = (0 until WorkdaysPerWeek).map { index ->
            val date = weekStart + DatePeriod(days = index)
            val workedMinutes = if (date == snapshot.date) {
                todayWorkedMinutes
            } else {
                history[date]?.workedMinutes ?: 0
            }
            WeekDayProgressUiModel(
                label = weekdayShortLabel(index),
                value = TimeTextFormatter.calendarDuration(workedMinutes),
                progress = if (dailyTargetMinutes == 0) {
                    0f
                } else {
                    workedMinutes.toFloat() / dailyTargetMinutes.toFloat()
                },
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

    private fun weekdayShortLabel(index: Int): String = when (index) {
        0 -> localized(Res.string.monday_short)
        1 -> localized(Res.string.tuesday_short)
        2 -> localized(Res.string.wednesday_short)
        3 -> localized(Res.string.thursday_short)
        else -> localized(Res.string.friday_short)
    }

    private const val MinDailyTargetMinutes = 60
    private const val MaxDailyTargetMinutes = 16 * 60
    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
    private const val MinWeeklyTargetMinutes = 60
    private const val MaxWeeklyTargetMinutes = 80 * 60
    private const val WorkdaysPerWeek = 5
}
