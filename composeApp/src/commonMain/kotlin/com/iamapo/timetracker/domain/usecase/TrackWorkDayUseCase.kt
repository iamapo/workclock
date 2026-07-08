package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.WorkDayReducer
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository

class TrackWorkDayUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val reducer: WorkDayReducer = WorkDayReducer()
) {
    operator fun invoke(action: TimeTrackerAction) {
        val snapshot = timeProvider.now()
        repository.update { history ->
            val current = history.dayWithWeeklySummary(snapshot.date)
            val updated = reducer.reduce(
                day = current,
                action = action,
                nowMinute = snapshot.minuteOfDay,
                defaultConfig = history.defaultConfig
            )
            history.withDay(snapshot.date, updated)
        }
    }
}
