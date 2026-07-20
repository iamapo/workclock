package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.TimeTrackerAction
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.datetime.LocalDate

class HandleTimeTrackingCommandUseCase(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val trackWorkDay: TrackWorkDayUseCase = TrackWorkDayUseCase(repository, timeProvider)
) {
    operator fun invoke(command: TimeTrackingCommand): Boolean {
        val snapshot = timeProvider.now()
        return invoke(command, snapshot.date, snapshot.minuteOfDay)
    }

    operator fun invoke(command: TimeTrackingCommand, date: LocalDate, minuteOfDay: Int): Boolean {
        val status = repository.history.value.dayWithWeeklySummary(date).status
        val action = command.toAction(status)
        if (!action.isAllowedFor(status)) return false
        trackWorkDay(action, date, minuteOfDay)
        return true
    }

    private fun TimeTrackingCommand.toAction(status: WorkStatus): TimeTrackerAction = when (this) {
        TimeTrackingCommand.Primary -> when (status) {
            WorkStatus.NotStarted -> TimeTrackerAction.StartDay
            WorkStatus.Working -> TimeTrackerAction.StartBreak
            WorkStatus.Paused -> TimeTrackerAction.ResumeWork
            WorkStatus.Finished -> TimeTrackerAction.StartNewDay
        }
        TimeTrackingCommand.StartDay -> TimeTrackerAction.StartDay
        TimeTrackingCommand.StartBreak -> TimeTrackerAction.StartBreak
        TimeTrackingCommand.ResumeWork -> TimeTrackerAction.ResumeWork
        TimeTrackingCommand.StartNewDay -> TimeTrackerAction.StartNewDay
        TimeTrackingCommand.EndDay -> TimeTrackerAction.EndDay
    }

    private fun TimeTrackerAction.isAllowedFor(status: WorkStatus): Boolean = when (this) {
        TimeTrackerAction.StartDay -> status == WorkStatus.NotStarted
        TimeTrackerAction.StartBreak -> status == WorkStatus.Working
        TimeTrackerAction.ResumeWork -> status == WorkStatus.Paused
        TimeTrackerAction.EndDay -> status == WorkStatus.Working || status == WorkStatus.Paused
        TimeTrackerAction.StartNewDay -> status == WorkStatus.Finished
    }
}
