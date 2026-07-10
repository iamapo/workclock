package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveWorkHistoryUseCase(
    private val repository: WorkHistoryRepository
) {
    operator fun invoke(): StateFlow<WorkHistory> = repository.history
}
