package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WeeklyBalance
import com.iamapo.timetracker.domain.WeeklyBalanceCalculator
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.WorkDaySummaryCalculator
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.presentation.state.MetricUiModel
import com.iamapo.timetracker.presentation.state.TargetItemUiModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import com.iamapo.timetracker.resources.*

object TimeTrackerUiStateMapper {
    private val summaryCalculator = WorkDaySummaryCalculator()
    private val weeklyBalanceCalculator = WeeklyBalanceCalculator()
    private val timelineMapper = TimelineMapper()

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
            primaryCommand = primaryCommand(day.status),
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
                    hint = localized(Res.string.balance_suffix, balanceLabel(weeklyBalance.balanceMinutes))
                )
            ),
            timeline = timelineMapper.map(day, summary.endMinute),
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

    private fun primaryCommand(status: WorkStatus): TimeTrackingCommand = when (status) {
        WorkStatus.NotStarted -> TimeTrackingCommand.StartDay
        WorkStatus.Working -> TimeTrackingCommand.StartBreak
        WorkStatus.Paused -> TimeTrackingCommand.ResumeWork
        WorkStatus.Finished -> TimeTrackingCommand.StartNewDay
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

    private fun balanceLabel(minutes: Int): String {
        val prefix = when {
            minutes > 0 -> "+"
            minutes < 0 -> "-"
            else -> ""
        }
        return prefix + TimeTextFormatter.duration(abs(minutes))
    }

}
