package com.iamapo.timetracker.app

import com.iamapo.timetracker.backup.BackupFileController
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.DeleteWorkEntriesUseCase
import com.iamapo.timetracker.domain.usecase.EditCalendarDayUseCase
import com.iamapo.timetracker.domain.usecase.HandleTimeTrackingCommandUseCase
import com.iamapo.timetracker.domain.usecase.ObserveWorkHistoryUseCase
import com.iamapo.timetracker.domain.usecase.TrackWorkDayUseCase
import com.iamapo.timetracker.domain.usecase.UpdateWorkSettingsUseCase
import com.iamapo.timetracker.lockscreen.LockScreenStatusController
import com.iamapo.timetracker.presentation.AppCalendarStateMapper
import com.iamapo.timetracker.presentation.CalendarStateMapper
import com.iamapo.timetracker.presentation.CalendarViewModel
import com.iamapo.timetracker.presentation.SettingsViewModel
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.reminders.NoOpReminderScheduler
import com.iamapo.timetracker.reminders.ReminderScheduler
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

data class WorkClockPlatformDependencies(
    val workDayStore: WorkDayStore,
    val backupFileController: BackupFileController,
    val lockScreenStatusController: LockScreenStatusController,
    val reminderScheduler: ReminderScheduler = NoOpReminderScheduler,
    val timeProvider: TimeProvider = SystemTimeProvider()
)

internal fun workClockModules(
    dependencies: WorkClockPlatformDependencies
): List<Module> = listOf(
    platformModule(dependencies),
    dataModule,
    domainModule,
    presentationModule
)

private fun platformModule(dependencies: WorkClockPlatformDependencies) = module {
    single<WorkDayStore> { dependencies.workDayStore }
    single<BackupFileController> { dependencies.backupFileController }
    single<LockScreenStatusController> { dependencies.lockScreenStatusController }
    single<ReminderScheduler> { dependencies.reminderScheduler }
    single<TimeProvider> { dependencies.timeProvider }
}

private val dataModule = module {
    single<WorkHistoryRepository> {
        PersistedWorkHistoryRepository(
            store = get(),
            today = get<TimeProvider>().now().date
        )
    }
}

private val domainModule = module {
    factory { ObserveWorkHistoryUseCase(get()) }
    factory { TrackWorkDayUseCase(get(), get()) }
    factory { HandleTimeTrackingCommandUseCase(get(), get(), get()) }
    factory { EditCalendarDayUseCase(get()) }
    factory { UpdateWorkSettingsUseCase(get(), get()) }
    factory { DeleteWorkEntriesUseCase(get()) }
}

private val presentationModule = module {
    single<CalendarStateMapper> { AppCalendarStateMapper }

    viewModel {
        TimeTrackerViewModel(
            timeProvider = get(),
            repository = get(),
            observeWorkHistory = get(),
            trackWorkDay = get(),
            handleTimeTrackingCommand = get()
        )
    }
    viewModel {
        CalendarViewModel(
            repository = get(),
            timeProvider = get(),
            stateMapper = get(),
            editDay = get()
        )
    }
    viewModel {
        SettingsViewModel(
            repository = get(),
            timeProvider = get(),
            updateSettings = get(),
            deleteWorkEntries = get()
        )
    }
}
