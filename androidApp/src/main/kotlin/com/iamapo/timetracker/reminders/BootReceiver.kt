package com.iamapo.timetracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iamapo.timetracker.data.AndroidWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.domain.SystemTimeProvider

/**
 * AlarmManager alarms are cleared on reboot, so a break/target that was already
 * running before the restart needs its reminder re-scheduled once here.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val timeProvider = SystemTimeProvider()
        val snapshot = timeProvider.now()
        val repository = PersistedWorkHistoryRepository(AndroidWorkDayStore(appContext), snapshot.date)
        val history = repository.history.value

        val schedule = ReminderScheduleMapper.map(
            day = history.dayWithWeeklySummary(snapshot.date),
            snapshot = snapshot,
            enabled = history.remindersEnabled
        )
        AndroidReminderScheduler(appContext).apply(schedule)
    }
}
