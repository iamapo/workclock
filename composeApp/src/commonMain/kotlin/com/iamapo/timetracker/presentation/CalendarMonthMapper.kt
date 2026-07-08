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

internal class CalendarMonthMapper {
    fun map(
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
            ?.let { "Urlaub " + TimeTextFormatter.calendarDuration(it.workedMinutes) }
            ?: "Urlaub"
        CalendarDayStyle.Sick -> history[date]
            ?.let { "Krank " + TimeTextFormatter.calendarDuration(it.workedMinutes) }
            ?: "Krank"
        CalendarDayStyle.Done -> history[date]
            ?.let { TimeTextFormatter.calendarDuration(it.workedMinutes) }
            ?: "-"
        CalendarDayStyle.Today -> {
            val day = history[date]
            if (day?.status == WorkStatus.Finished) {
                TimeTextFormatter.calendarDuration(day.workedMinutes)
            } else {
                "bis " + TimeTextFormatter.clock(endMinute)
            }
        }
        CalendarDayStyle.Planned -> "8:00"
    }

    private companion object {
        const val CalendarVisibleDayCount = 42
    }
}
