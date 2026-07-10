package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarUiState

fun interface CalendarStateMapper {
    fun map(history: WorkHistory, snapshot: TimeSnapshot): CalendarUiState
}
