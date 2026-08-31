package com.iamapo.timetracker.app

import android.app.Application
import androidx.activity.ComponentActivity
import com.iamapo.timetracker.backup.AndroidBackupFileController
import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.data.AndroidWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.AndroidLockScreenStatusController
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.reminders.AndroidReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import com.iamapo.timetracker.report.AndroidReportFileController
import com.iamapo.timetracker.report.ReportFileController

class AndroidWorkClockContainer(
    private val application: Application,
    private val activity: ComponentActivity,
    private val reminderPermissionRequester: ((Boolean) -> Unit) -> Unit
) : WorkClockContainer {

    private val _timeProvider: TimeProvider = SystemTimeProvider()
    override val timeProvider: TimeProvider = _timeProvider

    private val _workDayStore: WorkDayStore = AndroidWorkDayStore(application)
    override val workDayStore: WorkDayStore = _workDayStore

    override val repository: WorkHistoryRepository = PersistedWorkHistoryRepository(
        store = _workDayStore,
        today = _timeProvider.now().date
    )

    private val _backupFileController: BackupFileController = AndroidBackupFileController(activity)
    override val backupFileController: BackupFileController = _backupFileController

    override val reportFileController: ReportFileController = AndroidReportFileController(activity)

    private val _lockScreenStatusController: LockScreenStatusController = AndroidLockScreenStatusController(application)
    override val lockScreenStatusController: LockScreenStatusController = _lockScreenStatusController

    override val reminderScheduler: ReminderScheduler = AndroidReminderScheduler(
        context = application,
        permissionRequester = reminderPermissionRequester
    )
}
