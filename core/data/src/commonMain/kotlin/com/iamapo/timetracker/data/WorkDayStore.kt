package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate

interface WorkDayStore {
    fun loadHistory(today: LocalDate): WorkHistory
    fun saveHistory(history: WorkHistory)
    fun loadPreImportHistory(): WorkHistory?
    fun savePreImportHistory(history: WorkHistory)
    fun clearPreImportHistory()
}

object NoOpWorkDayStore : WorkDayStore {
    override fun loadHistory(today: LocalDate): WorkHistory = WorkHistory()

    override fun saveHistory(history: WorkHistory) = Unit

    override fun loadPreImportHistory(): WorkHistory? = null

    override fun savePreImportHistory(history: WorkHistory) = Unit

    override fun clearPreImportHistory() = Unit
}
