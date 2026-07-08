package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EditCalendarDayUseCaseTest {
    @Test
    fun adjustsDayInFifteenMinuteSteps() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository()
        val useCase = EditCalendarDayUseCase(repository)

        useCase.increaseDay(date)
        useCase.increaseDay(date)
        useCase.decreaseDay(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(15, day.workedMinutes)
        assertEquals(WorkStatus.Finished, day.status)
        assertEquals(WorkDayKind.Work, day.kind)
    }

    @Test
    fun marksVacationAsFullAbsenceDay() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository()
        val useCase = EditCalendarDayUseCase(repository)

        useCase.setVacation(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(8 * 60, day.workedMinutes)
        assertEquals(WorkDayKind.Vacation, day.kind)
        assertEquals("Urlaub", day.events.single().title)
    }

    @Test
    fun clearsDayEntry() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(days = mapOf(date to WorkDay(workedMinutes = 8 * 60)))
        )
        val useCase = EditCalendarDayUseCase(repository)

        useCase.clearDay(date)

        assertFalse(repository.history.value.days.containsKey(date))
    }
}
