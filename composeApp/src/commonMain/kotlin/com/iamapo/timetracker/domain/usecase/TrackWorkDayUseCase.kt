package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.WorkDayReducer
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.datetime.LocalDate

class TrackWorkDayUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val reducer: WorkDayReducer = WorkDayReducer()
) {
    operator fun invoke(action: TimeTrackerAction) {
        val snapshot = timeProvider.now()
        invoke(
            action = action,
            date = snapshot.date,
            minuteOfDay = snapshot.minuteOfDay
        )
    }

    operator fun invoke(
        action: TimeTrackerAction,
        date: LocalDate,
        minuteOfDay: Int
    ) {
        repository.update { history ->
            val current = history.dayWithWeeklySummary(date)
            val updated = reducer.reduce(
                day = current,
                action = action,
                nowMinute = minuteOfDay,
                defaultConfig = history.defaultConfig
            )
            history.withDay(date, updated)
        }
    }
}
