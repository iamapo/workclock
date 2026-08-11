package com.iamapo.timetracker.data

import com.iamapo.timetracker.data.sync.WorkDaySync
import com.iamapo.timetracker.domain.SystemTimeProvider
import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.usecase.HandleTimeTrackingCommandUseCase
import com.iamapo.timetracker.domain.usecase.TrackWorkDayUseCase
import com.iamapo.timetracker.domain.sync.CloudReplica

/** Gives the Mac app the same work day logic as the phone apps. */
class MacWorkClock(replica: CloudReplica) {
    private val timeProvider = SystemTimeProvider()
    private val store = AppleWorkDayStore()
    private val repository = PersistedWorkHistoryRepository(store, timeProvider.now().date)
    private val trackWorkDay = TrackWorkDayUseCase(repository, timeProvider)
    private val handleCommand = HandleTimeTrackingCommandUseCase(repository, timeProvider, trackWorkDay)
    private val workDaySync = WorkDaySync(repository, store, replica, timeProvider)

    fun today(): WorkDay = repository.history.value.dayFor(timeProvider.now().date)

    fun execute(command: TimeTrackingCommand): Boolean = handleCommand(command)

    fun reopenDay() {
        trackWorkDay(TimeTrackerAction.ReopenDay)
    }

    fun changeEventTime(eventIndex: Int, minuteOfDay: Int) {
        trackWorkDay(TimeTrackerAction.ChangeEventTime(eventIndex, minuteOfDay))
    }

    suspend fun sync(): Boolean = workDaySync.sync()
}
