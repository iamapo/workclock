package com.iamapo.timetracker.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlin.math.max
import com.iamapo.timetracker.resources.*

object TimeTextFormatter {
    fun dateLabel(date: LocalDate): String =
        localized(Res.string.date_format, weekdayName(date.dayOfWeek.isoDayNumber), date.day, monthName(date.month.number))

    fun monthTitle(date: LocalDate): String = localized(Res.string.month_year, monthName(date.month.number), date.year)

    fun duration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours == 0 -> localized(Res.string.minutes_only, minutes)
            minutes == 0 -> localized(Res.string.hours_only, hours)
            else -> localized(Res.string.hours_minutes, hours, minutes)
        }
    }

    fun shortDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) localized(Res.string.hours_only, hours) else duration(totalMinutes)
    }

    fun compactDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    fun clockLikeDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return hours.toString() + ":" + minutes.toString().padStart(2, '0') + " h"
    }

    fun calendarDuration(totalMinutes: Int): String = digitalDuration(totalMinutes)

    fun watchDuration(totalMinutes: Int): String = digitalDuration(totalMinutes)

    fun clock(minuteOfDay: Int): String {
        val normalized = ((minuteOfDay % MinutesPerDay) + MinutesPerDay) % MinutesPerDay
        val hours = normalized / 60
        val minutes = normalized % 60
        return hours.toString().padStart(2, '0') + ":" + minutes.toString().padStart(2, '0')
    }

    private fun digitalDuration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return hours.toString() + ":" + minutes.toString().padStart(2, '0')
    }

    private fun weekdayName(isoDayNumber: Int): String = when (isoDayNumber) {
        1 -> localized(Res.string.monday); 2 -> localized(Res.string.tuesday); 3 -> localized(Res.string.wednesday)
        4 -> localized(Res.string.thursday); 5 -> localized(Res.string.friday); 6 -> localized(Res.string.saturday)
        else -> localized(Res.string.sunday)
    }

    private fun monthName(monthNumber: Int): String = localized(listOf(
        Res.string.january, Res.string.february, Res.string.march, Res.string.april,
        Res.string.may, Res.string.june, Res.string.july, Res.string.august,
        Res.string.september, Res.string.october, Res.string.november, Res.string.december
    )[monthNumber - 1])

    private const val MinutesPerDay = 24 * 60
}
