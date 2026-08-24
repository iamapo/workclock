package com.iamapo.timetracker.presentation.state

import com.iamapo.timetracker.domain.TimeTrackingCommand

data class TimeTrackerUiState(
    val dayScheduleKind: DayScheduleUiKind,
    val dateLabel: String,
    val title: String,
    val statusLabel: String,
    val workedTime: String,
    val remainingTime: String,
    val endTime: String,
    val startTime: String,
    val currentTime: String,
    val breakRequirementLabel: String,
    val progress: Float,
    val workdayProgress: Float,
    val breakProgress: Float,
    val weeklyBalance: String,
    val primaryActionLabel: String,
    val primaryCommand: TimeTrackingCommand,
    val secondaryActionLabel: String?,
    val targets: List<TargetItemUiModel>,
    val metrics: List<MetricUiModel>,
    val timeline: List<TimelineItemUiModel>,
    val watchState: String,
    val watchProgress: Float,
    val watchRemaining: String,
    val watchCaption: String,
    val watchBreakStartedMinute: Int?
)

enum class DayScheduleUiKind {
    Workday,
    DayOff
}
