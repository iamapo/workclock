package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.ui.state.CalendarDayStyle
import com.iamapo.timetracker.ui.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.state.MetricUiModel
import com.iamapo.timetracker.ui.state.SettingsUiModel
import com.iamapo.timetracker.ui.state.TargetItemUiModel
import com.iamapo.timetracker.ui.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.state.TimelineItemUiModel
import com.iamapo.timetracker.ui.state.TimelineKind
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min

object TimeTrackerUiStateMapper {
    fun map(
        day: WorkDay,
        snapshot: TimeSnapshot,
        history: Map<LocalDate, WorkDay> = emptyMap()
    ): TimeTrackerUiState {
        val workedMinutes = day.workedMinutes + activeWorkMinutes(day, snapshot.minuteOfDay)
        val breakMinutes = day.breakMinutes + activeBreakMinutes(day, snapshot.minuteOfDay)
        val remainingWorkMinutes = max(day.config.dailyTargetMinutes - workedMinutes, 0)
        val missingBreakMinutes = max(day.config.requiredBreakMinutes - breakMinutes, 0)
        val endMinute = when (day.status) {
            WorkStatus.NotStarted -> snapshot.minuteOfDay + day.config.dailyTargetMinutes + day.config.requiredBreakMinutes
            WorkStatus.Working -> snapshot.minuteOfDay + remainingWorkMinutes + missingBreakMinutes
            WorkStatus.Paused -> snapshot.minuteOfDay + remainingWorkMinutes + missingBreakMinutes
            WorkStatus.Finished -> snapshot.minuteOfDay
        }
        val weeklyWorkedMinutes = day.weeklyWorkedBeforeTodayMinutes + workedMinutes
        val progress = min(workedMinutes.toFloat() / day.config.dailyTargetMinutes.toFloat(), 1f)

        return TimeTrackerUiState(
            dateLabel = formatDateLabel(snapshot.date),
            title = "WorkClock",
            statusLabel = statusLabel(day),
            workedTime = formatDuration(workedMinutes),
            remainingTime = formatDuration(remainingWorkMinutes),
            endTime = formatClock(endMinute) + " Uhr",
            breakRequirementLabel = "inkl. " + formatDuration(day.config.requiredBreakMinutes) + " Pflichtpause",
            progress = progress,
            primaryActionLabel = primaryActionLabel(day.status),
            secondaryActionLabel = secondaryActionLabel(day.status),
            targets = listOf(
                TargetItemUiModel("Tagesziel", formatShortDuration(day.config.dailyTargetMinutes)),
                TargetItemUiModel("Pflichtpause", formatDuration(day.config.requiredBreakMinutes)),
                TargetItemUiModel("Wochenziel", formatShortDuration(day.config.weeklyTargetMinutes))
            ),
            metrics = listOf(
                MetricUiModel(
                    label = "Gearbeitet",
                    value = formatCompactDuration(workedMinutes),
                    hint = if (workedMinutes == 0) "noch nicht gestartet" else "heute erfasst",
                    emphasized = workedMinutes > 0
                ),
                MetricUiModel(
                    label = "Pause",
                    value = formatCompactDuration(breakMinutes),
                    hint = if (missingBreakMinutes == 0) "Pflichtpause erfüllt" else formatDuration(missingBreakMinutes) + " fehlen"
                ),
                MetricUiModel(
                    label = "Fehlt",
                    value = formatCompactDuration(remainingWorkMinutes),
                    hint = "bis " + formatClock(endMinute)
                )
            ),
            timeline = timeline(day, endMinute),
            monthTitle = monthTitle(snapshot.date),
            calendarDays = calendarDays(snapshot.date, endMinute, history),
            plannedWeek = formatClockLikeDuration(day.config.weeklyTargetMinutes),
            reachedWeek = formatClockLikeDuration(weeklyWorkedMinutes),
            settings = SettingsUiModel(
                dailyTarget = formatClockLikeDuration(day.config.dailyTargetMinutes),
                requiredBreak = formatDuration(day.config.requiredBreakMinutes),
                canDecreaseRequiredBreak = day.config.requiredBreakMinutes > MinRequiredBreakMinutes,
                canIncreaseRequiredBreak = day.config.requiredBreakMinutes < MaxRequiredBreakMinutes,
                weeklyTarget = formatClockLikeDuration(day.config.weeklyTargetMinutes)
            ),
            watchState = watchState(day.status),
            watchRemaining = if (day.status == WorkStatus.Finished) "0:00" else formatWatchDuration(remainingWorkMinutes),
            watchCaption = if (day.status == WorkStatus.NotStarted) "bereit zum Start" else "noch bis " + formatClock(endMinute)
        )
    }

    private fun activeWorkMinutes(day: WorkDay, nowMinute: Int): Int =
        if (day.status == WorkStatus.Working && day.activeSessionStartMinute != null) {
            elapsedMinutes(day.activeSessionStartMinute, nowMinute)
        } else {
            0
        }

    private fun activeBreakMinutes(day: WorkDay, nowMinute: Int): Int =
        if (day.status == WorkStatus.Paused && day.pauseStartedMinute != null) {
            elapsedMinutes(day.pauseStartedMinute, nowMinute)
        } else {
            0
        }

    private fun elapsedMinutes(startMinute: Int, endMinute: Int): Int =
        if (endMinute >= startMinute) endMinute - startMinute else 24 * 60 - startMinute + endMinute

    private fun statusLabel(day: WorkDay): String = when (day.status) {
        WorkStatus.NotStarted -> "Bereit zum Start"
        WorkStatus.Working -> day.startMinute?.let { "Arbeite seit " + formatClock(it) } ?: "Arbeite"
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

    private fun timeline(day: WorkDay, endMinute: Int): List<TimelineItemUiModel> =
        day.events.map { event ->
            TimelineItemUiModel(
                time = formatClock(event.minuteOfDay),
                title = event.title,
                kind = when (event.kind) {
                    WorkEventKind.Work -> TimelineKind.Work
                    WorkEventKind.Break -> TimelineKind.Break
                    WorkEventKind.Target -> TimelineKind.Target
                }
            )
        } + TimelineItemUiModel(
            time = formatClock(endMinute),
            title = "Geplanter Feierabend",
            kind = TimelineKind.Target
        )

    private fun calendarDays(
        date: LocalDate,
        endMinute: Int,
        history: Map<LocalDate, WorkDay>
    ): List<CalendarDayUiModel> {
        val firstOfMonth = LocalDate(date.year, date.month, 1)
        val startDate = firstOfMonth - DatePeriod(days = firstOfMonth.dayOfWeek.isoDayNumber - 1)

        return (0 until CalendarVisibleDayCount).map { index ->
            val current = startDate + DatePeriod(days = index)
            val calendarDay = history[current]
            val isWeekend = current.dayOfWeek.isoDayNumber >= 6
            val isOutsideMonth = current.month != date.month
            val isToday = current == date
            val style = when {
                isOutsideMonth -> CalendarDayStyle.Muted
                calendarDay?.kind == WorkDayKind.Vacation -> CalendarDayStyle.Vacation
                calendarDay?.kind == WorkDayKind.Sick -> CalendarDayStyle.Sick
                isToday -> CalendarDayStyle.Today
                isWeekend -> CalendarDayStyle.Weekend
                current < date -> CalendarDayStyle.Done
                else -> CalendarDayStyle.Planned
            }
            CalendarDayUiModel(
                date = current,
                day = current.day.toString(),
                note = calendarNote(current, style, endMinute, history),
                style = style,
                isToday = isToday,
                isCurrentMonth = !isOutsideMonth,
                workedMinutes = calendarDay?.workedMinutes ?: 0
            )
        }
    }

    private fun calendarNote(
        date: LocalDate,
        style: CalendarDayStyle,
        endMinute: Int,
        history: Map<LocalDate, WorkDay>
    ): String = when (style) {
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> ""
        CalendarDayStyle.Vacation -> history[date]
            ?.let { "Urlaub " + formatCalendarDuration(it.workedMinutes) }
            ?: "Urlaub"
        CalendarDayStyle.Sick -> history[date]
            ?.let { "Krank " + formatCalendarDuration(it.workedMinutes) }
            ?: "Krank"
        CalendarDayStyle.Done -> history[date]
            ?.let { formatCalendarDuration(it.workedMinutes) }
            ?: "-"
        CalendarDayStyle.Today -> {
            val day = history[date]
            if (day?.status == WorkStatus.Finished) {
                formatCalendarDuration(day.workedMinutes)
            } else {
                "bis " + formatClock(endMinute)
            }
        }
        CalendarDayStyle.Planned -> "8:00"
    }

    private fun formatDateLabel(date: LocalDate): String =
        weekdayName(date.dayOfWeek.isoDayNumber) + ", " + date.day + ". " + monthName(date.month.number)

    private fun monthTitle(date: LocalDate): String = monthName(date.month.number) + " " + date.year

    private fun weekdayName(isoDayNumber: Int): String = when (isoDayNumber) {
        1 -> "Montag"
        2 -> "Dienstag"
        3 -> "Mittwoch"
        4 -> "Donnerstag"
        5 -> "Freitag"
        6 -> "Samstag"
        else -> "Sonntag"
    }

    private fun monthName(monthNumber: Int): String = when (monthNumber) {
        1 -> "Januar"
        2 -> "Februar"
        3 -> "März"
        4 -> "April"
        5 -> "Mai"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "August"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        else -> "Dezember"
    }

    private fun formatDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "$hours h"
            else -> "$hours h $minutes min"
        }
    }

    private fun formatShortDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours h" else formatDuration(totalMinutes)
    }

    private fun formatCompactDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    private fun formatClockLikeDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return hours.toString() + ":" + minutes.toString().padStart(2, '0') + " h"
    }

    private fun formatCalendarDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return hours.toString() + ":" + minutes.toString().padStart(2, '0')
    }

    private fun formatWatchDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return hours.toString() + ":" + minutes.toString().padStart(2, '0')
    }

    private fun formatClock(minuteOfDay: Int): String {
        val normalized = ((minuteOfDay % (24 * 60)) + 24 * 60) % (24 * 60)
        val hours = normalized / 60
        val minutes = normalized % 60
        return hours.toString().padStart(2, '0') + ":" + minutes.toString().padStart(2, '0')
    }

    private fun watchState(status: WorkStatus): String = when (status) {
        WorkStatus.NotStarted -> "Bereit"
        WorkStatus.Working -> "Aktiv"
        WorkStatus.Paused -> "Pause"
        WorkStatus.Finished -> "Fertig"
    }

    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
    private const val CalendarVisibleDayCount = 42
}
