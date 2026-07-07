package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate

interface WorkDayStore {
    fun loadHistory(today: LocalDate): WorkHistory
    fun saveHistory(history: WorkHistory)
}

object NoOpWorkDayStore : WorkDayStore {
    override fun loadHistory(today: LocalDate): WorkHistory = WorkHistory()

    override fun saveHistory(history: WorkHistory) = Unit
}
