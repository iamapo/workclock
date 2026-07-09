package com.iamapo.timetracker.lockscreen

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LockScreenStatusMapperTest {
    @Test
    fun workingStatusUsesTotalWorkedTimeForTimer() {
        val now = 10_000_000L
        val status = LockScreenStatusMapper.map(
            day = WorkDay(
                status = WorkStatus.Working,
                activeSessionStartMinute = 13 * 60,
                workedMinutes = 3 * 60,
                breakMinutes = 30
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 9),
                minuteOfDay = 14 * 60,
                epochMillis = now
            ),
            enabled = true
        )

        assertEquals(true, status.visible)
        assertEquals("Arbeitszeit heute", status.phaseLabel)
        assertEquals(4 * 60, status.elapsedMinutes)
        assertEquals(now - 4 * 60 * 60_000L, status.startedAtEpochMillis)
        assertEquals(4 * 60, status.workedMinutes)
    }

    @Test
    fun pausedStatusKeepsCurrentPauseTimer() {
        val now = 10_000_000L
        val status = LockScreenStatusMapper.map(
            day = WorkDay(
                status = WorkStatus.Paused,
                pauseStartedMinute = 12 * 60,
                workedMinutes = 3 * 60,
                breakMinutes = 30
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 9),
                minuteOfDay = 12 * 60 + 15,
                epochMillis = now
            ),
            enabled = true
        )

        assertEquals("Pause seit", status.phaseLabel)
        assertEquals(15, status.elapsedMinutes)
        assertEquals(now - 15 * 60_000L, status.startedAtEpochMillis)
        assertEquals(3 * 60, status.workedMinutes)
        assertEquals(45, status.breakMinutes)
    }
}
