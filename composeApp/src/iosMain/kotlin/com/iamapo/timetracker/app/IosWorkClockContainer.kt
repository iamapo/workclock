package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.backup.IosBackupFileController
import com.iamapo.timetracker.data.AppleWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.data.sync.WorkDaySync
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.sync.CloudReplica
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import com.iamapo.timetracker.report.IosReportFileController
import com.iamapo.timetracker.report.ReportFileController
import platform.UIKit.UIViewController

class IosWorkClockContainer(
    override val lockScreenStatusController: LockScreenStatusController = NoOpLockScreenStatusController,
    override val reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    cloudReplica: CloudReplica? = null
) : WorkClockContainer {

    private val _timeProvider: TimeProvider = SystemTimeProvider()
    override val timeProvider: TimeProvider = _timeProvider

    private val appleWorkDayStore = AppleWorkDayStore()
    private val _workDayStore: WorkDayStore = appleWorkDayStore
    override val workDayStore: WorkDayStore = _workDayStore

    override val repository: WorkHistoryRepository = PersistedWorkHistoryRepository(
        store = _workDayStore,
        today = _timeProvider.now().date
    )

    internal val workDaySync: WorkDaySync? = cloudReplica?.let { replica ->
        WorkDaySync(repository, appleWorkDayStore, replica, _timeProvider)
    }

    private val _backupFileController: BackupFileController = IosBackupFileController { _presenterHolder.presenter?.invoke() }
    override val backupFileController: BackupFileController = _backupFileController

    override val reportFileController: ReportFileController = IosReportFileController {
        _presenterHolder.presenter?.invoke()
    }

    private val _presenterHolder = PresenterHolder()

    internal fun setPresenter(presenter: () -> UIViewController?) {
        _presenterHolder.presenter = presenter
    }

    private class PresenterHolder {
        var presenter: (() -> UIViewController?)? = null
    }
}
