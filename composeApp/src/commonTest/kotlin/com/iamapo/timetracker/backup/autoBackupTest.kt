package com.iamapo.timetracker.backup

import com.iamapo.timetracker.data.WorkClockBackupSerializer
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkHistory
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AutoBackupTest {
    private val today = LocalDate(2026, 11, 5)
    private val snapshot = TimeSnapshot(
        date = today,
        minuteOfDay = 8 * 60,
        epochMillis = 1_793_000_000_000
    )
    private val history = WorkHistory(days = mapOf(today to WorkDay()))

    @Test
    fun writesRestorableBackup() {
        val controller = FakeController()

        val result = AutoBackup.run(controller, history, snapshot)

        assertEquals(AutoBackupResult.Saved, result)
        val content = assertNotNull(controller.files[AutoBackup.fileName(today)])
        assertEquals(history, WorkClockBackupSerializer.decode(content)?.history)
    }

    @Test
    fun writesOnlyOncePerDay() {
        val controller = FakeController()
        controller.files[AutoBackup.fileName(today)] = "morning"

        val result = AutoBackup.run(controller, history, snapshot)

        assertEquals(AutoBackupResult.Saved, result)
        assertEquals("morning", controller.files[AutoBackup.fileName(today)])
    }

    @Test
    fun keepsNewestFourteenAndForeignFiles() {
        val controller = FakeController()
        val foreignFile = "workclock-backup-2020-01-01.workclock"
        controller.files[foreignFile] = "manual export"
        (1..20).forEach { day ->
            controller.files[AutoBackup.fileName(LocalDate(2026, 10, day))] = "old"
        }

        AutoBackup.run(controller, history, snapshot)

        assertEquals(AutoBackup.KEEP_COUNT + 1, controller.files.size)
        assertTrue(foreignFile in controller.files)
        assertTrue(AutoBackup.fileName(today) in controller.files)
        assertTrue(AutoBackup.fileName(LocalDate(2026, 10, 8)) in controller.files)
        assertTrue(AutoBackup.fileName(LocalDate(2026, 10, 7)) !in controller.files)
    }

    @Test
    fun emptyHistoryNeverRotatesExistingBackups() {
        val controller = FakeController()
        (1..14).forEach { day ->
            controller.files[AutoBackup.fileName(LocalDate(2026, 10, day))] = "old"
        }

        val result = AutoBackup.run(controller, WorkHistory(), snapshot)

        assertEquals(AutoBackupResult.NothingToSave, result)
        assertEquals(14, controller.files.size)
    }

    @Test
    fun failedWriteKeepsOldBackups() {
        val controller = FakeController(canWrite = false)
        (1..14).forEach { day ->
            controller.files[AutoBackup.fileName(LocalDate(2026, 10, day))] = "old"
        }

        val result = AutoBackup.run(controller, history, snapshot)

        assertEquals(AutoBackupResult.Failed, result)
        assertEquals(14, controller.files.size)
    }

    @Test
    fun unreadableFolderFails() {
        val result = AutoBackup.run(NoOpBackupFileController, history, snapshot)

        assertEquals(AutoBackupResult.Failed, result)
    }

    private class FakeController(
        private val canWrite: Boolean = true
    ) : BackupFileController by NoOpBackupFileController {
        val files = mutableMapOf<String, String>()

        override fun listAutoBackupFiles(): List<String> = files.keys.toList()

        override fun writeAutoBackup(fileName: String, content: String): Boolean {
            if (canWrite) {
                files[fileName] = content
            }
            return canWrite
        }

        override fun deleteAutoBackupFile(fileName: String) {
            files.remove(fileName)
        }
    }
}
