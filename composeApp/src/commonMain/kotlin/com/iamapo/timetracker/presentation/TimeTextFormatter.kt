package com.iamapo.timetracker.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlin.math.max

internal object TimeTextFormatter {
    fun dateLabel(date: LocalDate): String =
        weekdayName(date.dayOfWeek.isoDayNumber) + ", " + date.day + ". " + monthName(date.month.number)

    fun monthTitle(date: LocalDate): String = monthName(date.month.number) + " " + date.year

    fun duration(totalMinutes: Int): String {
        val safeMinutes = max(totalMinutes, 0)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        return when {
            hours == 0 -> "$minutes min"
            minutes == 0 -> "$hours h"
            else -> "$hours h $minutes min"
        }
    }

    fun shortDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours h" else duration(totalMinutes)
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

    private const val MinutesPerDay = 24 * 60
}
