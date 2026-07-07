package com.iamapo.timetracker.domain

data class WorkEvent(
    val minuteOfDay: Int,
    val title: String,
    val kind: WorkEventKind
)
