package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

class PersistedWorkHistoryRepository(
    private val store: WorkDayStore,
    today: LocalDate
) : WorkHistoryRepository {
    private val mutableHistory = MutableStateFlow(store.loadHistory(today))
    private val updateLock = RepositoryUpdateLock()

    override val history: StateFlow<WorkHistory> = mutableHistory

    override fun update(transform: (WorkHistory) -> WorkHistory) {
        updateLock.withLock {
            val updated = transform(mutableHistory.value)
            store.saveHistory(updated)
            mutableHistory.value = updated
        }
    }
}
