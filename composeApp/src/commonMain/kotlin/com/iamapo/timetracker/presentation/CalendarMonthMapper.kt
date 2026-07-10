package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import workclock.composeapp.generated.resources.*

internal class CalendarMonthMapper {
    fun map(
        date: LocalDate,
        endMinute: Int,
        dailyTargetMinutes: Int,
        history: Map<LocalDate, WorkDay>
    ): List<CalendarDayUiModel> {
        val firstOfMonth = LocalDate(date.year, date.month, 1)
        val startDate = firstOfMonth - DatePeriod(days = firstOfMonth.dayOfWeek.isoDayNumber - 1)

        return mapDays(date, startDate, CalendarVisibleDayCount, endMinute, dailyTargetMinutes, history)
    }

    fun mapPreview(
        date: LocalDate,
        endMinute: Int,
        dailyTargetMinutes: Int,
        history: Map<LocalDate, WorkDay>
    ): List<CalendarDayUiModel> {
        val currentWeekStart = date - DatePeriod(days = date.dayOfWeek.isoDayNumber - 1)
        val previousWeekStart = currentWeekStart - DatePeriod(days = DaysPerWeek)
        return mapDays(date, previousWeekStart, PreviewDayCount, endMinute, dailyTargetMinutes, history)
    }

    private fun mapDays(
        date: LocalDate,
        startDate: LocalDate,
        dayCount: Int,
        endMinute: Int,
        dailyTargetMinutes: Int,
        history: Map<LocalDate, WorkDay>
    ): List<CalendarDayUiModel> =
        (0 until dayCount).map { index ->
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
                note = calendarNote(current, style, endMinute, dailyTargetMinutes, history),
                style = style,
                isToday = isToday,
                isCurrentMonth = !isOutsideMonth,
                workedMinutes = calendarDay?.workedMinutes ?: 0
            )
        }

    private fun calendarNote(
        date: LocalDate,
        style: CalendarDayStyle,
        endMinute: Int,
        dailyTargetMinutes: Int,
        history: Map<LocalDate, WorkDay>
    ): String = when (style) {
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> ""
        CalendarDayStyle.Vacation -> history[date]
            ?.let { localized(Res.string.calendar_vacation_duration, TimeTextFormatter.calendarDuration(it.workedMinutes)) }
            ?: localized(Res.string.vacation)
        CalendarDayStyle.Sick -> history[date]
            ?.let { localized(Res.string.calendar_sick_duration, TimeTextFormatter.calendarDuration(it.workedMinutes)) }
            ?: localized(Res.string.sick)
        CalendarDayStyle.Done -> history[date]
            ?.let { TimeTextFormatter.calendarDuration(it.workedMinutes) }
            ?: "-"
        CalendarDayStyle.Today -> {
            val day = history[date]
            if (day?.status == WorkStatus.Finished) {
                TimeTextFormatter.calendarDuration(day.workedMinutes)
            } else {
                localized(Res.string.until_time, TimeTextFormatter.clock(endMinute))
            }
        }
        CalendarDayStyle.Planned -> TimeTextFormatter.calendarDuration(dailyTargetMinutes)
    }

    private companion object {
        const val CalendarVisibleDayCount = 42
        const val PreviewDayCount = 14
        const val DaysPerWeek = 7
    }
}
