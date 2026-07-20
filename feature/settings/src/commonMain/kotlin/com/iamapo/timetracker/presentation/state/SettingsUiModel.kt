package com.iamapo.timetracker.presentation.state

data class SettingsUiModel(
    val dailyTarget: String,
    val canDecreaseDailyTarget: Boolean,
    val canIncreaseDailyTarget: Boolean,
    val requiredBreak: String,
    val canDecreaseRequiredBreak: Boolean,
    val canIncreaseRequiredBreak: Boolean,
    val weeklyTarget: String,
    val canDecreaseWeeklyTarget: Boolean,
    val canIncreaseWeeklyTarget: Boolean,
    val lockScreenStatusEnabled: Boolean
)
