package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.GermanFederalState
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.datetime.DayOfWeek

class UpdateWorkSettingsUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider
) {
    fun increaseDailyTarget() {
        adjustActiveDays(DailyTargetStepMinutes)
    }

    fun decreaseDailyTarget() {
        adjustActiveDays(-DailyTargetStepMinutes)
    }

    fun increaseRequiredBreak() {
        updateConfig { current ->
            current.copy(
                requiredBreakMinutes = (current.requiredBreakMinutes + RequiredBreakStepMinutes)
                    .coerceIn(MinRequiredBreakMinutes, MaxRequiredBreakMinutes)
            )
        }
    }

    fun decreaseRequiredBreak() {
        updateConfig { current ->
            current.copy(
                requiredBreakMinutes = (current.requiredBreakMinutes - RequiredBreakStepMinutes)
                    .coerceIn(MinRequiredBreakMinutes, MaxRequiredBreakMinutes)
            )
        }
    }

    fun increaseWeeklyTarget() {
        adjustWeekdayTarget(DayOfWeek.FRIDAY, WeeklyTargetStepMinutes)
    }

    fun decreaseWeeklyTarget() {
        adjustWeekdayTarget(DayOfWeek.FRIDAY, -WeeklyTargetStepMinutes)
    }

    fun increaseWeekdayTarget(isoDayNumber: Int) {
        adjustWeekdayTarget(dayOfWeek(isoDayNumber), DailyTargetStepMinutes)
    }

    fun decreaseWeekdayTarget(isoDayNumber: Int) {
        adjustWeekdayTarget(dayOfWeek(isoDayNumber), -DailyTargetStepMinutes)
    }

    fun setHolidayFederalState(federalState: GermanFederalState) {
        repository.update { history ->
            history.copy(
                holidayFederalState = federalState,
                automaticHolidaysEnabled = true
            )
        }
    }

    fun setAutomaticHolidaysEnabled(enabled: Boolean) {
        repository.update { history ->
            history.copy(automaticHolidaysEnabled = enabled && history.holidayFederalState != null)
        }
    }

    fun setLockScreenStatusEnabled(enabled: Boolean) {
        repository.update { history ->
            history.copy(lockScreenStatusEnabled = enabled)
        }
    }

    private fun updateConfig(transform: (WorkDayConfig) -> WorkDayConfig) {
        repository.update { history ->
            history.withDefaultConfig(transform(history.defaultConfig))
        }
    }

    private fun adjustActiveDays(deltaMinutes: Int) {
        repository.update { history ->
            var schedule = history.workSchedule
            Weekdays.forEach { dayOfWeek ->
                val current = schedule.targetMinutes(dayOfWeek)
                if (current > 0) {
                    schedule = schedule.withTarget(
                        dayOfWeek,
                        (current + deltaMinutes).coerceIn(MinWeekdayTargetMinutes, MaxDailyTargetMinutes)
                    )
                }
            }
            history.copy(
                defaultConfig = history.defaultConfig.copy(
                    dailyTargetMinutes = schedule.targetMinutes(timeProvider.now().date.dayOfWeek),
                    weeklyTargetMinutes = schedule.weeklyTargetMinutes
                ),
                workSchedule = schedule
            )
        }
    }

    private fun adjustWeekdayTarget(
        dayOfWeek: DayOfWeek,
        deltaMinutes: Int
    ) {
        repository.update { history ->
            val current = history.workSchedule.targetMinutes(dayOfWeek)
            val updated = (current + deltaMinutes).coerceIn(MinWeekdayTargetMinutes, MaxDailyTargetMinutes)
            val schedule = history.workSchedule.withTarget(dayOfWeek, updated)
            history.copy(
                defaultConfig = history.defaultConfig.copy(
                    dailyTargetMinutes = schedule.targetMinutes(timeProvider.now().date.dayOfWeek),
                    weeklyTargetMinutes = schedule.weeklyTargetMinutes
                ),
                workSchedule = schedule
            )
        }
    }

    private fun dayOfWeek(isoDayNumber: Int): DayOfWeek = when (isoDayNumber) {
        1 -> DayOfWeek.MONDAY
        2 -> DayOfWeek.TUESDAY
        3 -> DayOfWeek.WEDNESDAY
        4 -> DayOfWeek.THURSDAY
        5 -> DayOfWeek.FRIDAY
        6 -> DayOfWeek.SATURDAY
        7 -> DayOfWeek.SUNDAY
        else -> error("Invalid ISO day number: $isoDayNumber")
    }

    private companion object {
        const val DailyTargetStepMinutes = 15
        const val MinWeekdayTargetMinutes = 0
        const val MaxDailyTargetMinutes = 16 * 60
        const val RequiredBreakStepMinutes = 5
        const val MinRequiredBreakMinutes = 0
        const val MaxRequiredBreakMinutes = 120
        const val WeeklyTargetStepMinutes = 30
        val Weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
    }
}
