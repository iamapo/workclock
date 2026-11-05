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
    val dayProgressSegments: List<DayProgressSegmentUiModel>,
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

/** One work or break phase of the day; [fraction] is its share of the progress bar. */
data class DayProgressSegmentUiModel(
    val kind: TimelineKind,
    val fraction: Float
)

enum class DayScheduleUiKind {
    Workday,
    DayOff
}
