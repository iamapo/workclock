package com.iamapo.timetracker.presentation.state

data class TimeTrackerUiState(
    val dateLabel: String,
    val title: String,
    val statusLabel: String,
    val workedTime: String,
    val remainingTime: String,
    val endTime: String,
    val breakRequirementLabel: String,
    val progress: Float,
    val primaryActionLabel: String,
    val secondaryActionLabel: String?,
    val targets: List<TargetItemUiModel>,
    val metrics: List<MetricUiModel>,
    val timeline: List<TimelineItemUiModel>,
    val monthTitle: String,
    val calendarDays: List<CalendarDayUiModel>,
    val calendarPreviewDays: List<CalendarDayUiModel>,
    val plannedWeek: String,
    val reachedWeek: String,
    val weekOverview: WeekOverviewUiModel,
    val settings: SettingsUiModel,
    val watchState: String,
    val watchRemaining: String,
    val watchCaption: String
)
