package com.iamapo.timetracker.lockscreen

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class LockScreenStatusCoordinator(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val controller: LockScreenStatusController
) {
    suspend fun run() {
        repository.history
            .combine(minuteTicks()) { history, _ ->
                val snapshot = timeProvider.now()
                LockScreenStatusMapper.map(
                    day = history.dayWithWeeklySummary(snapshot.date),
                    snapshot = snapshot,
                    enabled = history.lockScreenStatusEnabled
                )
            }
            .collect(controller::publish)
    }

    private fun minuteTicks() = flow {
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(60_000)
        }
    }
}
