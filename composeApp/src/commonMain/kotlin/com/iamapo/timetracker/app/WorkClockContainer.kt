package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.reminders.ReminderScheduler
import com.iamapo.timetracker.report.ReportFileController

interface WorkClockContainer {
    val timeProvider: TimeProvider
    val repository: WorkHistoryRepository
    val workDayStore: WorkDayStore
    val backupFileController: BackupFileController
    val reportFileController: ReportFileController
    val lockScreenStatusController: LockScreenStatusController
    val reminderScheduler: ReminderScheduler
}
