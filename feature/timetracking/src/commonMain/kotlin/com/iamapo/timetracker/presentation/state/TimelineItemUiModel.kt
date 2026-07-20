package com.iamapo.timetracker.presentation.state

data class TimelineItemUiModel(
    val time: String,
    val title: String,
    val kind: TimelineKind
)
