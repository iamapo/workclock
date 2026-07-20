package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkClockBackupSerializerTest {
    private val history = WorkHistory(
        lockScreenStatusEnabled = true,
        days = mapOf(
            LocalDate(2026, 7, 20) to WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 480,
                breakMinutes = 30,
                events = listOf(
                    WorkEvent(8 * 60, "Arbeitsbeginn", WorkEventKind.Work),
                    WorkEvent(16 * 60 + 30, "Arbeitstag beendet", WorkEventKind.Target)
                )
            )
        )
    )

    @Test
    fun encodesAndDecodesPortableBackup() {
        val decoded = WorkClockBackupSerializer.decode(
            WorkClockBackupSerializer.encode(history, createdAtEpochMillis = 1_752_000_000_000)
        )

        assertEquals(history, decoded?.history)
        assertEquals(1_752_000_000_000, decoded?.createdAtEpochMillis)
    }

    @Test
    fun rejectsModifiedBackup() {
        val encoded = WorkClockBackupSerializer.encode(history, createdAtEpochMillis = 1)
        val modified = encoded.replace("workedMinutes=480", "workedMinutes=481")

        assertNull(WorkClockBackupSerializer.decode(modified))
    }

    @Test
    fun rejectsRawAppStorageAsBackup() {
        assertNull(WorkClockBackupSerializer.decode(WorkHistorySerializer.encodeHistory(history)))
    }

    @Test
    fun rejectsSemanticallyInvalidBackup() {
        val invalidHistory = history.copy(
            days = mapOf(LocalDate(2026, 7, 20) to WorkDay(workedMinutes = -1))
        )

        assertNull(
            WorkClockBackupSerializer.decode(
                WorkClockBackupSerializer.encode(invalidHistory, createdAtEpochMillis = 1)
            )
        )
    }
}
