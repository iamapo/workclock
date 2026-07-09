package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackWorkDayUseCaseTest {
    @Test
    fun startsNewDayWithDefaultConfig() {
        val date = LocalDate(2026, 7, 8)
        val config = WorkDayConfig(requiredBreakMinutes = 45)
        val repository = FakeWorkHistoryRepository(
            WorkHistory(defaultConfig = config)
        )
        val useCase = TrackWorkDayUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(date, 9 * 60))
        )

        useCase(TimeTrackerAction.StartNewDay)

        val day = repository.history.value.days.getValue(date)
        assertEquals(WorkStatus.Working, day.status)
        assertEquals(config, day.config)
        assertEquals(9 * 60, day.startMinute)
    }

    @Test
    fun appliesActionAtProvidedDateAndMinute() {
        val phoneDate = LocalDate(2026, 7, 9)
        val watchDate = LocalDate(2026, 7, 8)
        val repository = FakeWorkHistoryRepository()
        val useCase = TrackWorkDayUseCase(
            repository = repository,
            timeProvider = FakeTimeProvider(TimeSnapshot(phoneDate, 10 * 60))
        )

        useCase(
            action = TimeTrackerAction.StartDay,
            date = watchDate,
            minuteOfDay = 8 * 60 + 15
        )

        val day = repository.history.value.days.getValue(watchDate)
        assertEquals(WorkStatus.Working, day.status)
        assertEquals(8 * 60 + 15, day.startMinute)
        assertEquals(null, repository.history.value.days[phoneDate])
    }
}
