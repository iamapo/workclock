package com.iamapo.timetracker.presentation.state

data class TimelineItemUiModel(
    val time: String,
    val title: String,
    val kind: TimelineKind,
    val edit: TimelineEditUiModel? = null
)

data class TimelineEditUiModel(
    val eventIndex: Int,
    val minuteOfDay: Int,
    val earliestMinute: Int,
    val latestMinute: Int
)
