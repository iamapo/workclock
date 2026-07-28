package com.iamapo.timetracker.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iamapo.timetracker.MainActivity
import com.iamapo.timetracker.R

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ExtraTitle) ?: return
        val body = intent.getStringExtra(ExtraBody) ?: return
        val notificationId = intent.getIntExtra(ExtraNotificationId, DefaultNotificationId)

        ensureNotificationChannel(context)

        val notification = Notification.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_workclock_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)?.notify(notificationId, notification)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val channel = NotificationChannel(
            ChannelId,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ExtraTitle = "title"
        const val ExtraBody = "body"
        const val ExtraNotificationId = "notificationId"
        private const val ChannelId = "reminders"
        private const val DefaultNotificationId = 100
    }
}
