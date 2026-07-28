package com.iamapo.timetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.iamapo.timetracker.backup.AndroidBackupFileController
import com.iamapo.timetracker.data.AndroidWorkDayStore
import com.iamapo.timetracker.lockscreen.AndroidLockScreenStatusController
import com.iamapo.timetracker.reminders.AndroidReminderScheduler
import com.iamapo.timetracker.ui.TimeTrackerRoute
import com.iamapo.timetracker.app.createWorkClockDependencies

class MainActivity : ComponentActivity() {
    private val backupFileController = AndroidBackupFileController(this)
    private var notificationPermissionResult: ((Boolean) -> Unit)? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionResult?.invoke(granted)
        notificationPermissionResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        val dependencies = createWorkClockDependencies(
            workDayStore = AndroidWorkDayStore(applicationContext),
            backupFileController = backupFileController,
            lockScreenStatusController = AndroidLockScreenStatusController(applicationContext),
            reminderScheduler = AndroidReminderScheduler(
                context = applicationContext,
                permissionRequester = ::requestNotificationPermission
            )
        )
        setContent {
            TimeTrackerRoute(dependencies = dependencies)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        requestNotificationPermission {}
    }

    private fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
            return
        }

        notificationPermissionResult = onResult
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
