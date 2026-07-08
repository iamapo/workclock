package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate

data class TimeSnapshot(
    val date: LocalDate,
    val minuteOfDay: Int
)
