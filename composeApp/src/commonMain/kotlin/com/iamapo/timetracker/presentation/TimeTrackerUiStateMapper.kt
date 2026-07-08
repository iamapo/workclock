package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.MetricUiModel
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.presentation.state.TargetItemUiModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.datetime.LocalDate

object TimeTrackerUiStateMapper {
    private val summaryCalculator = WorkDaySummaryCalculator()
    private val timelineMapper = TimelineMapper()
    private val calendarMonthMapper = CalendarMonthMapper()

    fun map(
        day: WorkDay,
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay> = emptyMap()
    ): TimeTrackerUiState {
        val summary = summaryCalculator.calculate(day, snapshot)

        return TimeTrackerUiState(
            dateLabel = TimeTextFormatter.dateLabel(snapshot.date),
            title = "WorkClock",
            statusLabel = statusLabel(day),
            workedTime = TimeTextFormatter.duration(summary.workedMinutes),
            remainingTime = TimeTextFormatter.duration(summary.remainingWorkMinutes),
            endTime = TimeTextFormatter.clock(summary.endMinute) + " Uhr",
            breakRequirementLabel = "inkl. " + TimeTextFormatter.duration(day.config.requiredBreakMinutes) + " Pflichtpause",
            progress = summary.progress,
            primaryActionLabel = primaryActionLabel(day.status),
            secondaryActionLabel = secondaryActionLabel(day.status),
            targets = listOf(
                TargetItemUiModel("Tagesziel", TimeTextFormatter.shortDuration(day.config.dailyTargetMinutes)),
                TargetItemUiModel("Pflichtpause", TimeTextFormatter.duration(day.config.requiredBreakMinutes)),
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
                        "Pflichtpause erfüllt"
                    } else {
                        TimeTextFormatter.duration(summary.missingBreakMinutes) + " fehlen"
                    }
                ),
                MetricUiModel(
                    label = "Fehlt",
                    value = TimeTextFormatter.compactDuration(summary.remainingWorkMinutes),
                    hint = "bis " + TimeTextFormatter.clock(summary.endMinute)
                )
            ),
            timeline = timelineMapper.map(day, summary.endMinute),
            monthTitle = TimeTextFormatter.monthTitle(snapshot.date),
            calendarDays = calendarMonthMapper.map(snapshot.date, summary.endMinute, history),
            plannedWeek = TimeTextFormatter.clockLikeDuration(day.config.weeklyTargetMinutes),
            reachedWeek = TimeTextFormatter.clockLikeDuration(summary.weeklyWorkedMinutes),
            settings = SettingsUiModel(
                dailyTarget = TimeTextFormatter.clockLikeDuration(day.config.dailyTargetMinutes),
                requiredBreak = TimeTextFormatter.duration(day.config.requiredBreakMinutes),
                canDecreaseRequiredBreak = day.config.requiredBreakMinutes > MinRequiredBreakMinutes,
                canIncreaseRequiredBreak = day.config.requiredBreakMinutes < MaxRequiredBreakMinutes,
                weeklyTarget = TimeTextFormatter.clockLikeDuration(day.config.weeklyTargetMinutes)
            ),
            watchState = watchState(day.status),
            watchRemaining = if (day.status == WorkStatus.Finished) {
                "0:00"
            } else {
                TimeTextFormatter.watchDuration(summary.remainingWorkMinutes)
            },
            watchCaption = if (day.status == WorkStatus.NotStarted) {
                "bereit zum Start"
            } else {
                "noch bis " + TimeTextFormatter.clock(summary.endMinute)
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
        WorkStatus.NotStarted -> "Tag starten"
        WorkStatus.Working -> "Pause starten"
        WorkStatus.Paused -> "Weiterarbeiten"
        WorkStatus.Finished -> "Neuen Tag starten"
    }

    private fun secondaryActionLabel(status: WorkStatus): String? = when (status) {
        WorkStatus.Working,
        WorkStatus.Paused -> "Tag beenden"
        WorkStatus.NotStarted,
        WorkStatus.Finished -> null
    }

    private fun watchState(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> "Bereit"
        WorkStatus.Working -> "Aktiv"
        WorkStatus.Paused -> "Pause"
        WorkStatus.Finished -> "Fertig"
    }

    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
}
