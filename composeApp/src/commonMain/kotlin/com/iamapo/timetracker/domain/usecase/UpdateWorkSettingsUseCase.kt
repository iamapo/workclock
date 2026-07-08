package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository

class UpdateWorkSettingsUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider
) {
    fun increaseRequiredBreak() {
        updateRequiredBreak { current -> current + RequiredBreakStepMinutes }
    }

    fun decreaseRequiredBreak() {
        updateRequiredBreak { current -> current - RequiredBreakStepMinutes }
    }

    private fun updateRequiredBreak(transform: (Int) -> Int) {
        val snapshot = timeProvider.now()
        repository.update { history ->
            val current = history.dayWithWeeklySummary(snapshot.date)
            val updatedConfig = current.config.copy(
                requiredBreakMinutes = transform(current.config.requiredBreakMinutes)
                    .coerceIn(MinRequiredBreakMinutes, MaxRequiredBreakMinutes)
            )
            history
                .withDefaultConfig(updatedConfig)
                .withDay(snapshot.date, current.copy(config = updatedConfig))
        }
    }

    private companion object {
        const val RequiredBreakStepMinutes = 5
        const val MinRequiredBreakMinutes = 0
        const val MaxRequiredBreakMinutes = 120
    }
}
