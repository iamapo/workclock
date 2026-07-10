package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
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

object TimeTrackerUiStateMapper {
    private val summaryCalculator = WorkDaySummaryCalculator()
    private val timelineMapper = TimelineMapper()
    private val calendarMonthMapper = CalendarMonthMapper()

    fun map(
        day: WorkDay,
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay> = emptyMap(),
        lockScreenStatusEnabled: Boolean = false
    ): TimeTrackerUiState {
        val summary = summaryCalculator.calculate(day, snapshot)
        val displayedBreakMinutes = maxOf(day.config.requiredBreakMinutes, summary.breakMinutes)
        val weekOverview = weekOverview(
            day = day,
            snapshot = snapshot,
            history = history,
            todayWorkedMinutes = summary.workedMinutes,
            weeklyWorkedMinutes = summary.weeklyWorkedMinutes
        )

        return TimeTrackerUiState(
            dateLabel = TimeTextFormatter.dateLabel(snapshot.date),
            title = "WorkClock",
            statusLabel = statusLabel(day),
            workedTime = TimeTextFormatter.duration(summary.workedMinutes),
            remainingTime = TimeTextFormatter.duration(summary.remainingWorkMinutes),
            endTime = TimeTextFormatter.clock(summary.endMinute) + " Uhr",
            breakRequirementLabel = "inkl. " + TimeTextFormatter.duration(displayedBreakMinutes) + " Pause",
            progress = summary.progress,
            primaryActionLabel = primaryActionLabel(day.status),
            secondaryActionLabel = secondaryActionLabel(day.status),
            targets = listOf(
                TargetItemUiModel("Tagesziel", TimeTextFormatter.shortDuration(day.config.dailyTargetMinutes)),
                TargetItemUiModel("Pause", TimeTextFormatter.duration(day.config.requiredBreakMinutes)),
                TargetItemUiModel("Wochenziel", TimeTextFormatter.shortDuration(day.config.weeklyTargetMinutes))
            ),
            metrics = listOf(
                MetricUiModel(
                    label = "Gearbeitet",
                    value = TimeTextFormatter.compactDuration(summary.workedMinutes),
                    hint = if (summary.workedMinutes == 0) "noch nicht gestartet" else "heute erfasst",
                    emphasized = summary.workedMinutes > 0
                ),
                MetricUiModel(
                    label = "Pause",
                    value = TimeTextFormatter.compactDuration(summary.breakMinutes),
                    hint = if (summary.missingBreakMinutes == 0) {
                        "Pause erfüllt"
                    } else {
                        TimeTextFormatter.duration(summary.missingBreakMinutes) + " fehlen"
                    }
                ),
                MetricUiModel(
                    label = "Woche",
                    value = TimeTextFormatter.compactDuration(summary.weeklyWorkedMinutes),
                    hint = weekOverview.balance + " Saldo"
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
            reachedWeek = TimeTextFormatter.clockLikeDuration(summary.weeklyWorkedMinutes),
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
                WorkStatus.NotStarted -> "bereit zum Start"
                WorkStatus.Finished -> "beendet um " + TimeTextFormatter.clock(summary.endMinute)
                WorkStatus.Working,
                WorkStatus.Paused -> "noch bis " + TimeTextFormatter.clock(summary.endMinute)
            }
        )
    }

    private fun statusLabel(day: WorkDay): String = when (day.status) {
        WorkStatus.NotStarted -> "Bereit zum Start"
        WorkStatus.Working -> day.startMinute?.let { "Arbeite seit " + TimeTextFormatter.clock(it) } ?: "Arbeite"
        WorkStatus.Paused -> "Pause läuft"
        WorkStatus.Finished -> "Arbeitstag beendet"
    }

    private fun primaryActionLabel(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> "Start"
        WorkStatus.Working -> "Pause"
        WorkStatus.Paused -> "Weiterarbeiten"
        WorkStatus.Finished -> "Neuen Tag starten"
    }

    private fun secondaryActionLabel(status: WorkStatus): String? = when (status) {
        WorkStatus.Working,
        WorkStatus.Paused -> "Beenden"
        WorkStatus.NotStarted,
        WorkStatus.Finished -> null
    }

    private fun watchState(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> "Bereit"
        WorkStatus.Working -> "Aktiv"
        WorkStatus.Paused -> "Pause"
        WorkStatus.Finished -> "Fertig"
    }

    private fun weekOverview(
        day: WorkDay,
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay>,
        todayWorkedMinutes: Int,
        weeklyWorkedMinutes: Int
    ): WeekOverviewUiModel {
        val expectedWorkdays = snapshot.date.dayOfWeek.isoDayNumber.coerceIn(1, WorkdaysPerWeek)
        val expectedMinutes = day.config.weeklyTargetMinutes * expectedWorkdays / WorkdaysPerWeek
        val balanceMinutes = day.weeklyBalanceCarryMinutes + weeklyWorkedMinutes - expectedMinutes
        val carryMinutes = if (snapshot.date.dayOfWeek.isoDayNumber >= WorkdaysPerWeek) {
            balanceMinutes
        } else {
            day.weeklyBalanceCarryMinutes
        }
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
                progress = if (day.config.dailyTargetMinutes == 0) {
                    0f
                } else {
                    workedMinutes.toFloat() / day.config.dailyTargetMinutes.toFloat()
                },
                isToday = date == snapshot.date
            )
        }

        return WeekOverviewUiModel(
            reached = TimeTextFormatter.clockLikeDuration(weeklyWorkedMinutes),
            balance = balanceLabel(balanceMinutes),
            isPositiveBalance = balanceMinutes >= 0,
            carry = carryMinutes.takeIf { it != 0 }?.let(::balanceLabel),
            isPositiveCarry = carryMinutes >= 0,
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
        0 -> "Mo"
        1 -> "Di"
        2 -> "Mi"
        3 -> "Do"
        else -> "Fr"
    }

    private const val MinDailyTargetMinutes = 60
    private const val MaxDailyTargetMinutes = 16 * 60
    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
    private const val MinWeeklyTargetMinutes = 60
    private const val MaxWeeklyTargetMinutes = 80 * 60
    private const val WorkdaysPerWeek = 5
}
