package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeWorkHistoryRepository(
    initialHistory: WorkHistory = WorkHistory()
) : WorkHistoryRepository {
    private val mutableHistory = MutableStateFlow(initialHistory)

    override val history: StateFlow<WorkHistory> = mutableHistory

    override fun update(transform: (WorkHistory) -> WorkHistory) {
        mutableHistory.value = transform(mutableHistory.value)
    }
}

internal class FakeTimeProvider(
    private val snapshot: TimeSnapshot
) : TimeProvider {
    override fun now(): TimeSnapshot = snapshot
}
