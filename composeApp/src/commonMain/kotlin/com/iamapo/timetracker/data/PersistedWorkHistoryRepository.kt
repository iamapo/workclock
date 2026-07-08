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

    override val history: StateFlow<WorkHistory> = mutableHistory

    override fun update(transform: (WorkHistory) -> WorkHistory) {
        val updated = transform(mutableHistory.value)
        mutableHistory.value = updated
        store.saveHistory(updated)
    }
}
