package com.iamapo.timetracker.presentation.state

import kotlinx.datetime.LocalDate

data class CalendarDayUiModel(
    val date: LocalDate,
    val day: String,
    val note: String,
    val style: CalendarDayStyle,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val workedMinutes: Int,
    val scheduledTargetMinutes: Int = 0,
    val holidayName: String? = null,
    val startMinute: Int? = null,
    val breakMinutes: Int = 0,
    val endMinute: Int? = null
)
