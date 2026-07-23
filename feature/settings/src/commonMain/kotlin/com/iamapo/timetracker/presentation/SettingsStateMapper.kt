package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.presentation.state.WorkdaySettingUiModel
import kotlinx.datetime.DayOfWeek

object SettingsStateMapper {
    fun map(history: WorkHistory): SettingsUiModel {
        val base = map(history.defaultConfig, history.lockScreenStatusEnabled)
        val workdays = Weekdays.mapIndexed { index, dayOfWeek ->
            val minutes = history.workSchedule.targetMinutes(dayOfWeek)
            WorkdaySettingUiModel(
                isoDayNumber = index + 1,
                target = TimeTextFormatter.clockLikeDuration(minutes),
                canDecrease = minutes > MinWeekdayTargetMinutes,
                canIncrease = minutes < MaxDailyTargetMinutes
            )
        }
        return base.copy(
            weeklyTarget = TimeTextFormatter.clockLikeDuration(history.workSchedule.weeklyTargetMinutes),
            workdays = workdays,
            automaticHolidaysEnabled = history.automaticHolidaysEnabled,
            holidayFederalState = history.holidayFederalState
        )
    }

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
    private const val MinWeekdayTargetMinutes = 0
    private const val MaxDailyTargetMinutes = 16 * 60
    private const val MinRequiredBreakMinutes = 0
    private const val MaxRequiredBreakMinutes = 120
    private const val MinWeeklyTargetMinutes = 60
    private const val MaxWeeklyTargetMinutes = 80 * 60
    private val Weekdays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
}
