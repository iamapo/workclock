package com.iamapo.timetracker.ui.state

data class MetricUiModel(
    val label: String,
    val value: String,
    val hint: String,
    val emphasized: Boolean = false
)
