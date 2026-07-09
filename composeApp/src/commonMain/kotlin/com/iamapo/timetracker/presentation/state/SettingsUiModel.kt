package com.iamapo.timetracker.presentation.state

data class SettingsUiModel(
    val dailyTarget: String,
    val requiredBreak: String,
    val canDecreaseRequiredBreak: Boolean,
    val canIncreaseRequiredBreak: Boolean,
    val weeklyTarget: String,
    val lockScreenStatusEnabled: Boolean
)
