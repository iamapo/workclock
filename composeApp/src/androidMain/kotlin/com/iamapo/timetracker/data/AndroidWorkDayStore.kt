package com.iamapo.timetracker.data

import android.content.Context
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate

class AndroidWorkDayStore(context: Context) : WorkDayStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE
    )

    override fun loadHistory(today: LocalDate): WorkHistory =
        preferences.getString(HistoryKey, null)
            ?.let(WorkDayPersistenceCodec::decodeHistory)
            ?: loadLegacyWorkDay(today)

    override fun saveHistory(history: WorkHistory) {
        preferences.edit()
            .putString(HistoryKey, WorkDayPersistenceCodec.encodeHistory(history))
            .apply()
    }

    private fun loadLegacyWorkDay(today: LocalDate): WorkHistory {
        val day = preferences.getString(CurrentWorkDayKey, null)
            ?.let(WorkDayPersistenceCodec::decode)
            ?: return WorkHistory()

        return WorkHistory(days = mapOf(today to day))
    }

    private companion object {
        const val PreferencesName = "time_tracker"
        const val HistoryKey = "work_day_history"
        const val CurrentWorkDayKey = "current_work_day"
    }
}
