package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.presentation.state.SettingsUiModel

object SettingsStateMapper {
    fun map(config: WorkDayConfig, lockScreenStatusEnabled: Boolean): SettingsUiModel =
        SettingsUiModel(
            dailyTarget = TimeTextFormatter.clockLikeDuration(config.dailyTargetMinutes),
            canDecreaseDailyTarget = config.dailyTargetMinutes > MinDailyTargetMinutes,
            canIncreaseDailyTarget = config.dailyTargetMinutes < MaxDailyTargetMinutes,
            requiredBreak = TimeTextFormatter.duration(config.requiredBreakMinutes),
            canDecreaseRequiredBreak = config.requiredBreakMinutes > MinRequiredBreakMinutes,
            canIncreaseRequiredBreak = config.requiredBreakMinutes < MaxRequiredBreakMinutes,
            weeklyTarget = TimeTextFormatter.clockLikeDuration(config.weeklyTargetMinutes),
            canDecreaseWeeklyTarget = config.weeklyTargetMinutes > MinWeeklyTargetMinutes,
            canIncreaseWeeklyTarget = config.weeklyTargetMinutes < MaxWeeklyTargetMinutes,
            lockScreenStatusEnabled = lockScreenStatusEnabled
        )

    private const val MinDailyTargetMinutes = 60
    private const val MaxDailyTargetMinutes = 16 * 60
    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
    private const val MinWeeklyTargetMinutes = 60
    private const val MaxWeeklyTargetMinutes = 80 * 60
}
