package com.iamapo.timetracker.reminders

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.flow.map

class ReminderScheduleCoordinator(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val scheduler: ReminderScheduler
) {
    suspend fun run() {
        repository.history
            .map { history ->
                val snapshot = timeProvider.now()
                ReminderScheduleMapper.map(
                    day = history.dayWithWeeklySummary(snapshot.date),
                    snapshot = snapshot,
                    enabled = history.remindersEnabled
                )
            }
            .collect(scheduler::apply)
    }
}
