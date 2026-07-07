package com.iamapo.timetracker.presentation

import kotlinx.datetime.LocalDate

data class TimeSnapshot(
    val date: LocalDate,
    val minuteOfDay: Int
)
