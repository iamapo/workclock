package com.iamapo.timetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.iamapo.timetracker.app.AndroidWorkClockContainer
import com.iamapo.timetracker.ui.TimeTrackerRoute

class MainActivity : ComponentActivity() {
    private var notificationPermissionResult: ((Boolean) -> Unit)? = null
    private lateinit var container: AndroidWorkClockContainer

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionResult?.invoke(granted)
        notificationPermissionResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AndroidWorkClockContainer(application, this) { onResult ->
            requestNotificationPermission(onResult)
        }
        requestNotificationPermissionIfNeeded()
        setContent {
            TimeTrackerRoute(container = container)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        container.reminderScheduler.requestAuthorization { _ -> }
    }

    internal fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
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