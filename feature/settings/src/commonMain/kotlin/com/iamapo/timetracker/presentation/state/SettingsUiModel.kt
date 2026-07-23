package com.iamapo.timetracker.presentation.state

import com.iamapo.timetracker.domain.GermanFederalState

data class WorkdaySettingUiModel(
    val isoDayNumber: Int,
    val target: String,
    val canDecrease: Boolean,
    val canIncrease: Boolean
)

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
    val lockScreenStatusEnabled: Boolean,
    val workdays: List<WorkdaySettingUiModel> = emptyList(),
    val automaticHolidaysEnabled: Boolean = false,
    val holidayFederalState: GermanFederalState? = null
)
