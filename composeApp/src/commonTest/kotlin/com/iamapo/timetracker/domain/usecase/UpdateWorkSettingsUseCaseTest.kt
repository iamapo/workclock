package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateWorkSettingsUseCaseTest {
    @Test
    fun updatesDefaultAndCurrentDayDailyTarget() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(dailyTargetMinutes = 8 * 60))
        )
        val useCase = UpdateWorkSettingsUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(date, 9 * 60))
        )

        useCase.decreaseDailyTarget()

        val history = repository.history.value
        assertEquals(7 * 60 + 45, history.defaultConfig.dailyTargetMinutes)
        assertEquals(7 * 60 + 45, history.dayFor(date).config.dailyTargetMinutes)
    }

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
    fun updatesDefaultAndCurrentDayWeeklyTarget() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(weeklyTargetMinutes = 40 * 60))
        )
        val useCase = UpdateWorkSettingsUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(date, 9 * 60))
        )

        useCase.increaseWeeklyTarget()

        val history = repository.history.value
        assertEquals(40 * 60 + 30, history.defaultConfig.weeklyTargetMinutes)
        assertEquals(40 * 60 + 30, history.dayFor(date).config.weeklyTargetMinutes)
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
