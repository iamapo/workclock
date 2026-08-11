package com.iamapo.timetracker.data

import com.iamapo.timetracker.data.sync.DaySyncStore
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import platform.Foundation.NSUserDefaults

class AppleWorkDayStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : WorkDayStore, DaySyncStore {
    override fun loadHistory(today: LocalDate): WorkHistory =
        defaults.stringForKey(HistoryKey)
            ?.let(WorkHistorySerializer::decodeHistory)
            ?: loadLegacyWorkDay(today)

    override fun saveHistory(history: WorkHistory) {
        defaults.setObject(
            WorkHistorySerializer.encodeHistory(history),
            forKey = HistoryKey
        )
    }

    override fun loadPreImportHistory(): WorkHistory? =
        defaults.stringForKey(PreImportHistoryKey)
            ?.let(WorkHistorySerializer::decodeHistory)

    override fun savePreImportHistory(history: WorkHistory) {
        defaults.setObject(
            WorkHistorySerializer.encodeHistory(history),
            forKey = PreImportHistoryKey
        )
    }

    override fun clearPreImportHistory() {
        defaults.removeObjectForKey(PreImportHistoryKey)
    }

    override fun loadSyncState(): String? = defaults.stringForKey(SYNC_STATE_KEY)

    override fun saveSyncState(state: String) {
        defaults.setObject(state, forKey = SYNC_STATE_KEY)
    }

    private fun loadLegacyWorkDay(today: LocalDate): WorkHistory {
        val day = defaults.stringForKey(CurrentWorkDayKey)
            ?.let(WorkHistorySerializer::decode)
            ?: return WorkHistory()

        return WorkHistory(days = mapOf(today to day))
    }

    private companion object {
        const val HistoryKey = "work_day_history"
        const val CurrentWorkDayKey = "current_work_day"
        const val PreImportHistoryKey = "pre_import_work_day_history"
        const val SYNC_STATE_KEY = "work_day_sync_state"
    }
}
