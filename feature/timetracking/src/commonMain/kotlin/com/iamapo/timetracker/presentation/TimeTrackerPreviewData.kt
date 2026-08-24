package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

object TimeTrackerPreviewData {
    val snapshot: TimeSnapshot = TimeSnapshot(
        date = LocalDate(2026, Month.JULY, 7),
        minuteOfDay = 14 * 60 + 33
    )

    // Computed lazily (not as eager `val`s) because mapping resolves localized
    // string resources, which requires a live Android context. An eager `val`
    // would run this at object-init time and permanently break Compose Preview
    // rendering (all properties of a Kotlin object share one static initializer).
    fun uiState(): TimeTrackerUiState = uiStateWorking()

    fun uiStateNotStarted(): TimeTrackerUiState =
        TimeTrackerUiStateMapper.map(WorkDay.previewNotStarted(), snapshot)

    fun uiStateWorking(): TimeTrackerUiState =
        TimeTrackerUiStateMapper.map(WorkDay.previewWorking(), snapshot)

    fun uiStatePaused(): TimeTrackerUiState =
        TimeTrackerUiStateMapper.map(WorkDay.previewPaused(), snapshot)

    fun uiStateFinished(): TimeTrackerUiState =
        TimeTrackerUiStateMapper.map(
            WorkDay.previewFinished(),
            snapshot.copy(minuteOfDay = 17 * 60 + 16)
        )

    fun uiStateWeekend(): TimeTrackerUiState {
        val weekendSnapshot = TimeSnapshot(
            date = LocalDate(2026, Month.JULY, 11),
            minuteOfDay = 11 * 60
        )
        val history = WorkHistory(
            days = (6..10).associate { dayOfMonth ->
                LocalDate(2026, Month.JULY, dayOfMonth) to WorkDay(
                    status = WorkStatus.Finished,
                    workedMinutes = 8 * 60
                )
            }
        )

        return TimeTrackerUiStateMapper.map(
            day = history.dayWithWeeklySummary(weekendSnapshot.date),
            snapshot = weekendSnapshot,
            history = history.days
        )
    }
}
