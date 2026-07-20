package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.lockscreen.LockScreenStatusController

data class WorkClockDependencies(
    val timeProvider: TimeProvider,
    val repository: WorkHistoryRepository,
    val workDayStore: WorkDayStore,
    val backupFileController: BackupFileController,
    val lockScreenStatusController: LockScreenStatusController
)

fun createWorkClockDependencies(
    workDayStore: WorkDayStore,
    backupFileController: BackupFileController,
    lockScreenStatusController: LockScreenStatusController,
    timeProvider: TimeProvider = SystemTimeProvider()
): WorkClockDependencies = WorkClockDependencies(
    timeProvider = timeProvider,
    repository = PersistedWorkHistoryRepository(
        store = workDayStore,
        today = timeProvider.now().date
    ),
    workDayStore = workDayStore,
    backupFileController = backupFileController,
    lockScreenStatusController = lockScreenStatusController
)
