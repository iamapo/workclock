package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteWorkEntriesUseCaseTest {
    @Test
    fun deletesEntriesButKeepsDefaultConfig() {
        val config = WorkDayConfig(requiredBreakMinutes = 40)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(
                defaultConfig = config,
                days = mapOf(LocalDate(2026, 7, 8) to WorkDay(workedMinutes = 8 * 60))
            )
        )
        val useCase = DeleteWorkEntriesUseCase(repository)

        useCase()

        assertTrue(repository.history.value.days.isEmpty())
        assertEquals(config, repository.history.value.defaultConfig)
    }
}
