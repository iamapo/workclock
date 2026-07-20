package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

object TimeTrackerPreviewData {
    val snapshot: TimeSnapshot = TimeSnapshot(
        date = LocalDate(2026, Month.JULY, 7),
        minuteOfDay = 14 * 60 + 33
    )

    val uiState: TimeTrackerUiState =
        TimeTrackerUiStateMapper.map(WorkDay.preview(), snapshot)
}
