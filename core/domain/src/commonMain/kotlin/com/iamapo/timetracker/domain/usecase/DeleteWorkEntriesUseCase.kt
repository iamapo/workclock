package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository

class DeleteWorkEntriesUseCase(
    private val repository: WorkHistoryRepository
) {
    operator fun invoke() {
        repository.update { history ->
            WorkHistory(defaultConfig = history.defaultConfig)
        }
    }
}
