package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkDayPersistenceCodecTest {
    @Test
    fun encodesAndDecodesWorkDay() {
        val day = WorkDay(
            status = WorkStatus.Working,
            startMinute = 8 * 60 + 42,
            activeSessionStartMinute = 12 * 60 + 26,
            pauseStartedMinute = null,
            workedMinutes = 3 * 60 + 5,
            breakMinutes = 32,
            lastBreakMinutes = 24,
            weeklyWorkedBeforeTodayMinutes = 16 * 60 + 28,
            events = listOf(
                WorkEvent(8 * 60 + 42, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60 + 2, "Pause | gestartet", WorkEventKind.Break),
                WorkEvent(12 * 60 + 26, "Weiter%gearbeitet", WorkEventKind.Work)
            ),
            config = WorkDayConfig(
                dailyTargetMinutes = 7 * 60 + 30,
                requiredBreakMinutes = 30,
                weeklyTargetMinutes = 38 * 60
            )
        )

        val decoded = WorkDayPersistenceCodec.decode(WorkDayPersistenceCodec.encode(day))

        assertEquals(day, decoded)
    }

    @Test
    fun returnsNullForInvalidData() {
        assertNull(WorkDayPersistenceCodec.decode("not persisted state"))
    }

    @Test
    fun encodesAndDecodesWorkHistory() {
        val monday = WorkDay(
            status = WorkStatus.Finished,
            workedMinutes = 8 * 60,
            breakMinutes = 45,
            events = listOf(
                WorkEvent(9 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(17 * 60 + 45, "Arbeitstag beendet", WorkEventKind.Target)
            )
        )
        val tuesday = WorkDay(
            status = WorkStatus.Paused,
            startMinute = 9 * 60 + 5,
            pauseStartedMinute = 12 * 60,
            workedMinutes = 2 * 60 + 55,
            events = listOf(
                WorkEvent(9 * 60 + 5, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break)
            )
        )
        val history = WorkHistory(
            days = mapOf(
                LocalDate(2026, 7, 6) to monday,
                LocalDate(2026, 7, 7) to tuesday
            )
        )

        val decoded = WorkDayPersistenceCodec.decodeHistory(
            WorkDayPersistenceCodec.encodeHistory(history)
        )

        assertEquals(history, decoded)
    }

    @Test
    fun returnsNullForInvalidHistoryData() {
        assertNull(WorkDayPersistenceCodec.decodeHistory("not persisted history"))
    }
}
