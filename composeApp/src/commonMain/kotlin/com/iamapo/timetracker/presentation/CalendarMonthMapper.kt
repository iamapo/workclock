package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.GermanHolidayKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import com.iamapo.timetracker.resources.*

internal class CalendarMonthMapper {
    fun map(
        date: LocalDate,
        endMinute: Int,
        history: WorkHistory
    ): List<CalendarDayUiModel> {
        val firstOfMonth = LocalDate(date.year, date.month, 1)
        val startDate = firstOfMonth - DatePeriod(days = firstOfMonth.dayOfWeek.isoDayNumber - 1)

        return mapDays(date, startDate, CalendarVisibleDayCount, endMinute, history)
    }

    fun mapPreview(
        date: LocalDate,
        endMinute: Int,
        history: WorkHistory
    ): List<CalendarDayUiModel> {
        val currentWeekStart = date - DatePeriod(days = date.dayOfWeek.isoDayNumber - 1)
        val previousWeekStart = currentWeekStart - DatePeriod(days = DaysPerWeek)
        return mapDays(date, previousWeekStart, PreviewDayCount, endMinute, history)
    }

    private fun mapDays(
        date: LocalDate,
        startDate: LocalDate,
        dayCount: Int,
        endMinute: Int,
        history: WorkHistory
    ): List<CalendarDayUiModel> =
        (0 until dayCount).map { index ->
            val current = startDate + DatePeriod(days = index)
            val calendarDay = history.days[current]
            val holiday = history.holiday(current)
            val isWeekend = current.dayOfWeek.isoDayNumber >= 6
            val isOutsideMonth = current.month != date.month
            val isToday = current == date
            val style = when {
                isOutsideMonth -> CalendarDayStyle.Muted
                calendarDay?.kind == WorkDayKind.Vacation -> CalendarDayStyle.Vacation
                calendarDay?.kind == WorkDayKind.Sick -> CalendarDayStyle.Sick
                calendarDay == null && holiday != null -> CalendarDayStyle.Holiday
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
                workedMinutes = calendarDay?.workedMinutes ?: 0,
                scheduledTargetMinutes = history.scheduledTargetMinutes(current),
                holidayName = holiday?.kind?.let(::holidayName),
                startMinute = calendarDay?.startMinute,
                breakMinutes = calendarDay?.breakMinutes ?: 0,
                endMinute = calendarDay?.events?.lastOrNull()?.minuteOfDay
            )
        }

    private fun calendarNote(
        date: LocalDate,
        style: CalendarDayStyle,
        endMinute: Int,
        history: WorkHistory
    ): String = when (style) {
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend,
        CalendarDayStyle.Vacation,
        CalendarDayStyle.Sick -> ""
        CalendarDayStyle.Holiday -> history.holiday(date)?.kind?.let(::holidayName).orEmpty()
        CalendarDayStyle.Done -> history.days[date]
            ?.let { TimeTextFormatter.calendarDuration(it.workedMinutes) }
            ?: "-"
        CalendarDayStyle.Today -> {
            val day = history.days[date]
            if (day?.status == WorkStatus.Finished) {
                TimeTextFormatter.calendarDuration(day.workedMinutes)
            } else {
                localized(Res.string.until_time, TimeTextFormatter.clock(endMinute))
            }
        }
        CalendarDayStyle.Planned -> TimeTextFormatter.calendarDuration(history.targetMinutes(date))
    }

    private fun holidayName(kind: GermanHolidayKind): String = localized(when (kind) {
        GermanHolidayKind.NewYear -> Res.string.holiday_new_year
        GermanHolidayKind.Epiphany -> Res.string.holiday_epiphany
        GermanHolidayKind.InternationalWomensDay -> Res.string.holiday_womens_day
        GermanHolidayKind.GoodFriday -> Res.string.holiday_good_friday
        GermanHolidayKind.EasterSunday -> Res.string.holiday_easter_sunday
        GermanHolidayKind.EasterMonday -> Res.string.holiday_easter_monday
        GermanHolidayKind.LabourDay -> Res.string.holiday_labour_day
        GermanHolidayKind.AscensionDay -> Res.string.holiday_ascension
        GermanHolidayKind.WhitSunday -> Res.string.holiday_whit_sunday
        GermanHolidayKind.WhitMonday -> Res.string.holiday_whit_monday
        GermanHolidayKind.CorpusChristi -> Res.string.holiday_corpus_christi
        GermanHolidayKind.AssumptionDay -> Res.string.holiday_assumption
        GermanHolidayKind.WorldChildrensDay -> Res.string.holiday_childrens_day
        GermanHolidayKind.GermanUnityDay -> Res.string.holiday_german_unity
        GermanHolidayKind.ReformationDay -> Res.string.holiday_reformation
        GermanHolidayKind.AllSaintsDay -> Res.string.holiday_all_saints
        GermanHolidayKind.DayOfRepentanceAndPrayer -> Res.string.holiday_repentance
        GermanHolidayKind.ChristmasDay -> Res.string.holiday_christmas
        GermanHolidayKind.SecondChristmasDay -> Res.string.holiday_second_christmas
    })

    private companion object {
        const val CalendarVisibleDayCount = 42
        const val PreviewDayCount = 14
        const val DaysPerWeek = 7
    }
}
