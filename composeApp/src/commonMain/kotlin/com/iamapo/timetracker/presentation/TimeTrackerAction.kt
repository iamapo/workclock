package com.iamapo.timetracker.presentation

sealed interface TimeTrackerAction {
    data object StartDay : TimeTrackerAction
    data object StartBreak : TimeTrackerAction
    data object ResumeWork : TimeTrackerAction
    data object EndDay : TimeTrackerAction
    data object StartNewDay : TimeTrackerAction
}
