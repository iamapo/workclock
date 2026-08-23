package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.backup.NoOpBackupFileController
import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.DeleteWorkEntriesUseCase
import com.iamapo.timetracker.domain.usecase.EditCalendarDayUseCase
import com.iamapo.timetracker.domain.usecase.HandleTimeTrackingCommandUseCase
import com.iamapo.timetracker.domain.usecase.ObserveWorkHistoryUseCase
import com.iamapo.timetracker.domain.usecase.TrackWorkDayUseCase
import com.iamapo.timetracker.domain.usecase.UpdateWorkSettingsUseCase
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.lockscreen.NoOpLockScreenStatusController
import com.iamapo.timetracker.presentation.CalendarStateMapper
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import org.koin.dsl.koinApplication

class WorkClockModulesTest {
    @Test
    fun dependencyGraphResolvesCoreServices() {
        val platformDependencies = WorkClockPlatformDependencies(
            workDayStore = NoOpWorkDayStore,
            backupFileController = NoOpBackupFileController,
            lockScreenStatusController = NoOpLockScreenStatusController,
            reminderScheduler = NoOpReminderScheduler
        )
        val application = koinApplication {
            modules(workClockModules(platformDependencies))
        }

        try {
            val koin = application.koin

            assertSame(NoOpWorkDayStore, koin.get<WorkDayStore>())
            assertSame(NoOpBackupFileController, koin.get<BackupFileController>())
            assertSame(NoOpLockScreenStatusController, koin.get<LockScreenStatusController>())
            assertSame(NoOpReminderScheduler, koin.get<ReminderScheduler>())
            assertIs<PersistedWorkHistoryRepository>(koin.get<WorkHistoryRepository>())
            assertSame(koin.get<WorkHistoryRepository>(), koin.get())

            assertNotSame(koin.get<ObserveWorkHistoryUseCase>(), koin.get())
            assertNotSame(koin.get<TrackWorkDayUseCase>(), koin.get())
            assertNotSame(koin.get<HandleTimeTrackingCommandUseCase>(), koin.get())
            assertNotSame(koin.get<EditCalendarDayUseCase>(), koin.get())
            assertNotSame(koin.get<UpdateWorkSettingsUseCase>(), koin.get())
            assertNotSame(koin.get<DeleteWorkEntriesUseCase>(), koin.get())
            koin.get<CalendarStateMapper>()
        } finally {
            application.close()
        }
    }
}
