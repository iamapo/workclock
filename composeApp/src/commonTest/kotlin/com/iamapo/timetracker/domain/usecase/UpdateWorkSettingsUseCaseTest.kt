package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateWorkSettingsUseCaseTest {
    @Test
    fun updatesDefaultAndCurrentDayBreakRequirement() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(requiredBreakMinutes = 30))
        )
        val useCase = UpdateWorkSettingsUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(date, 9 * 60))
        )

        useCase.increaseRequiredBreak()

        val history = repository.history.value
        assertEquals(35, history.defaultConfig.requiredBreakMinutes)
        assertEquals(35, history.dayFor(date).config.requiredBreakMinutes)
    }

    @Test
    fun updatesLockScreenStatusSetting() {
        val repository = FakeWorkHistoryRepository(WorkHistory())
        val useCase = UpdateWorkSettingsUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(LocalDate(2026, 7, 8), 9 * 60))
        )

        useCase.setLockScreenStatusEnabled(true)

        assertEquals(true, repository.history.value.lockScreenStatusEnabled)
    }
}
