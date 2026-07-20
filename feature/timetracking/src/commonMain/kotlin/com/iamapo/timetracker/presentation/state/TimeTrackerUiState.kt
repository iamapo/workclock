package com.iamapo.timetracker.presentation.state

import com.iamapo.timetracker.domain.TimeTrackingCommand

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
    val primaryCommand: TimeTrackingCommand,
    val secondaryActionLabel: String?,
    val targets: List<TargetItemUiModel>,
    val metrics: List<MetricUiModel>,
    val timeline: List<TimelineItemUiModel>,
    val watchState: String,
    val watchRemaining: String,
    val watchCaption: String
)
