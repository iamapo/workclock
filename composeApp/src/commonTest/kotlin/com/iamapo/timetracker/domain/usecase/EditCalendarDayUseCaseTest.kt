package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEventKind
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
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(dailyTargetMinutes = 7 * 60 + 30))
        )
        val useCase = EditCalendarDayUseCase(repository)

        useCase.setVacation(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(7 * 60 + 30, day.workedMinutes)
        assertEquals(WorkDayKind.Vacation, day.kind)
        assertEquals("Urlaub", day.events.single().title)
    }

    @Test
    fun marksSickAsConfiguredTargetDay() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(dailyTargetMinutes = 6 * 60))
        )
        val useCase = EditCalendarDayUseCase(repository)

        useCase.setSick(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(6 * 60, day.workedMinutes)
        assertEquals(WorkDayKind.Sick, day.kind)
        assertEquals("Krank", day.events.single().title)
    }

    @Test
    fun storesForgottenWorkDayWithDefaultEightHourTimeline() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository()
        val useCase = EditCalendarDayUseCase(repository)

        useCase.setForgottenWorkDay(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(WorkDayKind.Work, day.kind)
        assertEquals(WorkStatus.Finished, day.status)
        assertEquals(8 * 60, day.workedMinutes)
        assertEquals(30, day.breakMinutes)
        assertEquals(8 * 60 + 30, day.startMinute)
        assertEquals(
            listOf(
                "Arbeitsbeginn",
                "Pause gestartet",
                "Weitergearbeitet",
                "Arbeitstag beendet"
            ),
            day.events.map { it.title }
        )
        assertEquals(listOf(510, 720, 750, 1020), day.events.map { it.minuteOfDay })
        assertEquals(WorkEventKind.Target, day.events.last().kind)
    }

    @Test
    fun storesForgottenWorkDayWithConfiguredTarget() {
        val date = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = WorkDayConfig(dailyTargetMinutes = 7 * 60 + 30))
        )
        val useCase = EditCalendarDayUseCase(repository)

        useCase.setForgottenWorkDay(date)

        val day = repository.history.value.days.getValue(date)
        assertEquals(7 * 60 + 30, day.workedMinutes)
        assertEquals(30, day.breakMinutes)
        assertEquals(16 * 60 + 30, day.events.last().minuteOfDay)
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
