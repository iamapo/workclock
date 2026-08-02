package com.iamapo.timetracker.presentation.state

import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val displayedMonth: LocalDate,
    val canNavigateToNextMonth: Boolean,
    val monthTitle: String,
    val days: List<CalendarDayUiModel>,
    val previewDays: List<CalendarDayUiModel>,
    val weekOverview: WeekOverviewUiModel,
    val dailyTarget: String,
    val plannedWeek: String,
    val reachedWeek: String
)
