package com.iamapo.timetracker.domain.repository

import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.coroutines.flow.StateFlow

interface WorkHistoryRepository {
    val history: StateFlow<WorkHistory>

    fun update(transform: (WorkHistory) -> WorkHistory)
}
