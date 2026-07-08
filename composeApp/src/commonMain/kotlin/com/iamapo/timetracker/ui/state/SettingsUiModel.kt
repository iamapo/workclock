package com.iamapo.timetracker.ui.state

data class SettingsUiModel(
    val dailyTarget: String,
    val requiredBreak: String,
    val canDecreaseRequiredBreak: Boolean,
    val canIncreaseRequiredBreak: Boolean,
    val weeklyTarget: String
)
