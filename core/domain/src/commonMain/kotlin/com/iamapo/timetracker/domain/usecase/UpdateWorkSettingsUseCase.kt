package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository

class UpdateWorkSettingsUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider
) {
    fun increaseDailyTarget() {
        updateConfig { current ->
            current.copy(
                dailyTargetMinutes = (current.dailyTargetMinutes + DailyTargetStepMinutes)
                    .coerceIn(MinDailyTargetMinutes, MaxDailyTargetMinutes)
            )
        }
    }

    fun decreaseDailyTarget() {
        updateConfig { current ->
            current.copy(
                dailyTargetMinutes = (current.dailyTargetMinutes - DailyTargetStepMinutes)
                    .coerceIn(MinDailyTargetMinutes, MaxDailyTargetMinutes)
            )
        }
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
        updateConfig { current ->
            current.copy(
                weeklyTargetMinutes = (current.weeklyTargetMinutes + WeeklyTargetStepMinutes)
                    .coerceIn(MinWeeklyTargetMinutes, MaxWeeklyTargetMinutes)
            )
        }
    }

    fun decreaseWeeklyTarget() {
        updateConfig { current ->
            current.copy(
                weeklyTargetMinutes = (current.weeklyTargetMinutes - WeeklyTargetStepMinutes)
                    .coerceIn(MinWeeklyTargetMinutes, MaxWeeklyTargetMinutes)
            )
        }
    }

    fun setLockScreenStatusEnabled(enabled: Boolean) {
        repository.update { history ->
            history.copy(lockScreenStatusEnabled = enabled)
        }
    }

    private fun updateConfig(transform: (WorkDayConfig) -> WorkDayConfig) {
        val snapshot = timeProvider.now()
        repository.update { history ->
            val current = history.dayWithWeeklySummary(snapshot.date)
            val updatedConfig = transform(current.config)
            history
                .withDefaultConfig(updatedConfig)
                .withDay(snapshot.date, current.copy(config = updatedConfig))
        }
    }

    private companion object {
        const val DailyTargetStepMinutes = 15
        const val MinDailyTargetMinutes = 60
        const val MaxDailyTargetMinutes = 16 * 60
        const val RequiredBreakStepMinutes = 5
        const val MinRequiredBreakMinutes = 0
        const val MaxRequiredBreakMinutes = 120
        const val WeeklyTargetStepMinutes = 30
        const val MinWeeklyTargetMinutes = 60
        const val MaxWeeklyTargetMinutes = 80 * 60
    }
}
