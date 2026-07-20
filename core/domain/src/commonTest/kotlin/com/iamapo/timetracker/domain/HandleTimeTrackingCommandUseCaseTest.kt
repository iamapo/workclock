package com.iamapo.timetracker.domain

import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.HandleTimeTrackingCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HandleTimeTrackingCommandUseCaseTest {
    private val date = LocalDate(2026, 7, 20)
    private val time = object : TimeProvider {
        override fun now() = TimeSnapshot(date, 9 * 60)
    }

    @Test
    fun primaryCommandUsesCurrentStatusTransition() {
        val repository = FakeRepository()
        val handle = HandleTimeTrackingCommandUseCase(repository, time)

        assertTrue(handle(TimeTrackingCommand.Primary))
        assertEquals(WorkStatus.Working, repository.history.value.dayWithWeeklySummary(date).status)
        assertTrue(handle(TimeTrackingCommand.Primary, date, 12 * 60))
        assertEquals(WorkStatus.Paused, repository.history.value.dayWithWeeklySummary(date).status)
    }

    @Test
    fun rejectsCommandThatDoesNotMatchCurrentStatus() {
        val repository = FakeRepository()
        val handle = HandleTimeTrackingCommandUseCase(repository, time)

        assertFalse(handle(TimeTrackingCommand.ResumeWork))
        assertEquals(WorkStatus.NotStarted, repository.history.value.dayWithWeeklySummary(date).status)
    }

    private class FakeRepository(initial: WorkHistory = WorkHistory()) : WorkHistoryRepository {
        private val mutableHistory = MutableStateFlow(initial)
        override val history: StateFlow<WorkHistory> = mutableHistory

        override fun update(transform: (WorkHistory) -> WorkHistory) {
            mutableHistory.value = transform(mutableHistory.value)
        }
    }
}
