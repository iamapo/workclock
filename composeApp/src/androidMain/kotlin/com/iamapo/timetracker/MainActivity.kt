package com.iamapo.timetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.iamapo.timetracker.data.AndroidWorkDayStore
import com.iamapo.timetracker.lockscreen.AndroidLockScreenStatusController
import com.iamapo.timetracker.ui.TimeTrackerRoute

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // The controller publishes the next visible status once permission is available.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            val workDayStore = remember { AndroidWorkDayStore(applicationContext) }
            val lockScreenStatusController = remember {
                AndroidLockScreenStatusController(applicationContext)
            }
            TimeTrackerRoute(
                workDayStore = workDayStore,
                lockScreenStatusController = lockScreenStatusController
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
