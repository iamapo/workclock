package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarUiState
import kotlinx.datetime.LocalDate

interface CalendarStateMapper {
    fun map(
        history: WorkHistory,
        snapshot: TimeSnapshot,
        displayedMonth: LocalDate
    ): CalendarUiState

    fun map(history: WorkHistory, snapshot: TimeSnapshot): CalendarUiState =
        map(history, snapshot, snapshot.date)
}
