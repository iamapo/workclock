package com.iamapo.timetracker.lockscreen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.iamapo.timetracker.MainActivity
import com.iamapo.timetracker.R
import kotlin.math.max

class LockScreenStatusService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val status = intent?.toLockScreenStatus()
        if (status == null || !status.visible) {
            stopStatus()
            return START_NOT_STICKY
        }

        ensureNotificationChannel()
        startForegroundCompat(buildNotification(status))
        return START_STICKY
    }

    private fun buildNotification(status: LockScreenStatus): Notification {
        val chronometerBase = status.startedAtEpochMillis.takeIf { it > 0L }
            ?: (System.currentTimeMillis() - status.elapsedMinutes * MillisPerMinute)
        val contentText = "${status.phaseLabel} • ${status.subtitle}"

        val notification = Notification.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_workclock_notification)
            .setContentTitle(status.title)
            .setContentText(contentText)
            .setStyle(Notification.BigTextStyle().bigText(contentText))
            .setContentIntent(openAppIntent())
            .setCategory(Notification.CATEGORY_STOPWATCH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(chronometerBase)
            .setUsesChronometer(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                }
            }
            .build()

        notification.extras.putBoolean(ExtraRequestPromotedOngoing, true)
        return notification
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(ChannelId) != null) return

        val channel = NotificationChannel(
            ChannelId,
            "Sperrbildschirm-Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zeigt Arbeitszeit und Pausen live auf dem Sperrbildschirm."
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NotificationId, notification)
        }
    }

    private fun stopStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun Intent.toLockScreenStatus(): LockScreenStatus? {
        val visible = getBooleanExtra(ExtraVisible, false)
        val phase = getStringExtra(ExtraPhase) ?: LockScreenStatus.PhaseHidden
        return LockScreenStatus(
            visible = visible,
            phase = phase,
            title = getStringExtra(ExtraTitle) ?: "WorkClock",
            phaseLabel = getStringExtra(ExtraPhaseLabel).orEmpty(),
            subtitle = getStringExtra(ExtraSubtitle).orEmpty(),
            startedAtEpochMillis = getLongExtra(ExtraStartedAtEpochMillis, 0L),
            elapsedMinutes = max(getIntExtra(ExtraElapsedMinutes, 0), 0),
            workedMinutes = max(getIntExtra(ExtraWorkedMinutes, 0), 0),
            breakMinutes = max(getIntExtra(ExtraBreakMinutes, 0), 0)
        )
    }

    companion object {
        private const val ChannelId = "lock_screen_status"
        private const val NotificationId = 42
        private const val MillisPerMinute = 60_000L
        private const val ExtraRequestPromotedOngoing = "android.requestPromotedOngoing"
        private const val ExtraVisible = "visible"
        private const val ExtraPhase = "phase"
        private const val ExtraTitle = "title"
        private const val ExtraPhaseLabel = "phaseLabel"
        private const val ExtraSubtitle = "subtitle"
        private const val ExtraStartedAtEpochMillis = "startedAtEpochMillis"
        private const val ExtraElapsedMinutes = "elapsedMinutes"
        private const val ExtraWorkedMinutes = "workedMinutes"
        private const val ExtraBreakMinutes = "breakMinutes"

        fun start(context: Context, status: LockScreenStatus) {
            val intent = Intent(context, LockScreenStatusService::class.java).apply {
                putExtra(ExtraVisible, status.visible)
                putExtra(ExtraPhase, status.phase)
                putExtra(ExtraTitle, status.title)
                putExtra(ExtraPhaseLabel, status.phaseLabel)
                putExtra(ExtraSubtitle, status.subtitle)
                putExtra(ExtraStartedAtEpochMillis, status.startedAtEpochMillis)
                putExtra(ExtraElapsedMinutes, status.elapsedMinutes)
                putExtra(ExtraWorkedMinutes, status.workedMinutes)
                putExtra(ExtraBreakMinutes, status.breakMinutes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockScreenStatusService::class.java))
        }
    }
}
