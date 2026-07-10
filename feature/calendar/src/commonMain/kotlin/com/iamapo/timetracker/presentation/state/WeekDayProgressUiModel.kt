package com.iamapo.timetracker.presentation.state

data class WeekDayProgressUiModel(
    val label: String,
    val value: String,
    val progress: Float,
    val isToday: Boolean
)
