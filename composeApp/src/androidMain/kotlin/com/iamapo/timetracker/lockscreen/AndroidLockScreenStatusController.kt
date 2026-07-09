package com.iamapo.timetracker.lockscreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class AndroidLockScreenStatusController(context: Context) : LockScreenStatusController {
    private val applicationContext = context.applicationContext
    private var lastPublishedStatus: LockScreenStatus? = null

    override fun publish(status: LockScreenStatus) {
        if (status == lastPublishedStatus) return
        lastPublishedStatus = status

        if (!status.visible || !canPostNotifications()) {
            LockScreenStatusService.stop(applicationContext)
            return
        }

        LockScreenStatusService.start(applicationContext, status)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
