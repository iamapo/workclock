package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import platform.Foundation.NSUserDefaults

class IosWorkDayStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : WorkDayStore {
    override fun loadHistory(today: LocalDate): WorkHistory =
        defaults.stringForKey(HistoryKey)
            ?.let(WorkDayPersistenceCodec::decodeHistory)
            ?: loadLegacyWorkDay(today)

    override fun saveHistory(history: WorkHistory) {
        defaults.setObject(
            WorkDayPersistenceCodec.encodeHistory(history),
            forKey = HistoryKey
        )
    }

    private fun loadLegacyWorkDay(today: LocalDate): WorkHistory {
        val day = defaults.stringForKey(CurrentWorkDayKey)
            ?.let(WorkDayPersistenceCodec::decode)
            ?: return WorkHistory()

        return WorkHistory(days = mapOf(today to day))
    }

    private companion object {
        const val HistoryKey = "work_day_history"
        const val CurrentWorkDayKey = "current_work_day"
    }
}
