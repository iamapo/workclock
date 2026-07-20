package com.iamapo.timetracker.presentation.state

data class CalendarUiState(
    val monthTitle: String,
    val days: List<CalendarDayUiModel>,
    val previewDays: List<CalendarDayUiModel>,
    val weekOverview: WeekOverviewUiModel,
    val dailyTarget: String,
    val plannedWeek: String,
    val reachedWeek: String
)
