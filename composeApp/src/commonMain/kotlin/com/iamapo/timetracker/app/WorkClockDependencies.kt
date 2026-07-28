package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler

data class WorkClockDependencies(
    val timeProvider: TimeProvider,
    val repository: WorkHistoryRepository,
    val workDayStore: WorkDayStore,
    val backupFileController: BackupFileController,
    val lockScreenStatusController: LockScreenStatusController,
    val reminderScheduler: ReminderScheduler
)

fun createWorkClockDependencies(
    workDayStore: WorkDayStore,
    backupFileController: BackupFileController,
    lockScreenStatusController: LockScreenStatusController,
    reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    timeProvider: TimeProvider = SystemTimeProvider()
): WorkClockDependencies = WorkClockDependencies(
    timeProvider = timeProvider,
    repository = PersistedWorkHistoryRepository(
        store = workDayStore,
        today = timeProvider.now().date
    ),
    workDayStore = workDayStore,
    backupFileController = backupFileController,
    lockScreenStatusController = lockScreenStatusController,
    reminderScheduler = reminderScheduler
)
