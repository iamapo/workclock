package com.iamapo.timetracker.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class AndroidReminderScheduler(
    context: Context,
    private val permissionRequester: (((Boolean) -> Unit) -> Unit)? = null
) : ReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun apply(schedule: ReminderSchedule) {
        applyOne(RequestCodeBreak, schedule.breakReminder)
        applyOne(RequestCodeDaily, schedule.dailyTargetReminder)
        applyOne(RequestCodeWeekly, schedule.weeklyTargetReminder)
    }

    override fun requestAuthorization(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            appContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
            return
        }

        permissionRequester?.invoke(onResult) ?: onResult(false)
    }

    private fun applyOne(requestCode: Int, reminder: Reminder?) {
        val pendingIntent = pendingIntentFor(requestCode, reminder)

        if (reminder == null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
            return
        }

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager?.canScheduleExactAlarms() == true

        if (canScheduleExact) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtEpochMillis,
                pendingIntent
            )
        } else {
            // Falls back to inexact delivery when the user hasn't granted the special
            // "schedule exact alarms" permission while still allowing delivery during Doze.
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtEpochMillis,
                pendingIntent
            )
        }
    }

    private fun pendingIntentFor(requestCode: Int, reminder: Reminder?): PendingIntent {
        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            if (reminder != null) {
                putExtra(ReminderAlarmReceiver.ExtraTitle, reminder.title)
                putExtra(ReminderAlarmReceiver.ExtraBody, reminder.body)
                putExtra(ReminderAlarmReceiver.ExtraNotificationId, requestCode)
            }
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val RequestCodeBreak = 101
        const val RequestCodeDaily = 102
        const val RequestCodeWeekly = 103
    }
}
