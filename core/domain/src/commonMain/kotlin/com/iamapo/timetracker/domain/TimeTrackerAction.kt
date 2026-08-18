package com.iamapo.timetracker.domain

sealed interface TimeTrackerAction {
    data object StartDay : TimeTrackerAction
    data object StartBreak : TimeTrackerAction
    data object ResumeWork : TimeTrackerAction
    data object EndDay : TimeTrackerAction
    data object ReopenDay : TimeTrackerAction
    data object StartNewDay : TimeTrackerAction
    data class ChangeEventTime(val eventIndex: Int, val minuteOfDay: Int) : TimeTrackerAction
}
