package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTrackerUiStateMapperTest {
    @Test
    fun previewDayCalculatesRemainingTimeEndTimeAndWeekTotal() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay.preview(),
            snapshot = TimeTrackerPreviewData.snapshot
        )

        assertEquals("2 h 48 min", state.remainingTime)
        assertEquals("17:34 Uhr", state.endTime)
        assertEquals("21:40 h", state.reachedWeek)
    }
}
