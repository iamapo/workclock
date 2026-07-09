package com.iamapo.timetracker.presentation.state

data class WeekOverviewUiModel(
    val reached: String,
    val balance: String,
    val isPositiveBalance: Boolean,
    val days: List<WeekDayProgressUiModel>
)
