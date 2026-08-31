package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.backup.IosBackupFileController
import com.iamapo.timetracker.data.IosWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import com.iamapo.timetracker.report.IosReportFileController
import com.iamapo.timetracker.report.ReportFileController
import platform.UIKit.UIViewController

class IosWorkClockContainer : WorkClockContainer {

    private val _timeProvider: TimeProvider = SystemTimeProvider()
    override val timeProvider: TimeProvider = _timeProvider

    private val _workDayStore: WorkDayStore = IosWorkDayStore()
    override val workDayStore: WorkDayStore = _workDayStore

    override val repository: WorkHistoryRepository = PersistedWorkHistoryRepository(
        store = _workDayStore,
        today = _timeProvider.now().date
    )

    private val _backupFileController: BackupFileController = IosBackupFileController { _presenterHolder.presenter?.invoke() }
    override val backupFileController: BackupFileController = _backupFileController

    override val reportFileController: ReportFileController = IosReportFileController {
        _presenterHolder.presenter?.invoke()
    }

    private val _lockScreenStatusController: LockScreenStatusController = NoOpLockScreenStatusController
    override val lockScreenStatusController: LockScreenStatusController = _lockScreenStatusController

    override val reminderScheduler: ReminderScheduler = NoOpReminderScheduler

    private val _presenterHolder = PresenterHolder()

    internal fun setPresenter(presenter: () -> UIViewController?) {
        _presenterHolder.presenter = presenter
    }

    private class PresenterHolder {
        var presenter: (() -> UIViewController?)? = null
    }
}
