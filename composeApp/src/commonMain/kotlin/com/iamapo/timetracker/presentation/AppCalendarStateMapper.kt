package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarUiState

object AppCalendarStateMapper : CalendarStateMapper {
    override fun map(history: WorkHistory, snapshot: TimeSnapshot): CalendarUiState {
        val state = TimeTrackerUiStateMapper.map(
            history.dayWithWeeklySummary(snapshot.date), snapshot, history.days,
            history.lockScreenStatusEnabled
        )
        return CalendarUiState(state.monthTitle, state.calendarDays, state.weekOverview, state.settings.dailyTarget)
    }
}
