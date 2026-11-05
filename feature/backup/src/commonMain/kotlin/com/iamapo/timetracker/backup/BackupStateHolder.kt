package com.iamapo.timetracker.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.iamapo.timetracker.data.WorkClockBackupSerializer
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class BackupStateHolder(
    private val workDayStore: WorkDayStore,
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val backupFileController: BackupFileController
) {
    var status by mutableStateOf(BackupUiStatus.None)
        private set

    var pendingImport by mutableStateOf<PendingBackupImport?>(null)
        private set

    var canUndoImport by mutableStateOf(workDayStore.loadPreImportHistory() != null)
        private set

    var autoBackupEnabled by mutableStateOf(backupFileController.hasAutoBackupFolder())
        private set

    var autoBackupFailed by mutableStateOf(false)
        private set

    var lastAutoBackupDate by mutableStateOf<LocalDate?>(null)
        private set

    fun changeAutoBackup(enabled: Boolean) {
        status = BackupUiStatus.None
        if (enabled) {
            backupFileController.chooseAutoBackupFolder(::onAutoBackupFolderChosen)
            return
        }

        backupFileController.clearAutoBackupFolder()
        autoBackupEnabled = false
        autoBackupFailed = false
        lastAutoBackupDate = null
    }

    /** Runs until cancelled and backs up on start and whenever the history changes. */
    suspend fun runAutoBackups() {
        if (!autoBackupEnabled) return
        repository.history.collect(::backUpIfDue)
    }

    private fun onAutoBackupFolderChosen(result: BackupFileResult) {
        when (result) {
            BackupFileResult.Success -> autoBackupEnabled = true
            BackupFileResult.Failure -> status = BackupUiStatus.Failure
            BackupFileResult.Cancelled -> Unit
        }
    }

    private suspend fun backUpIfDue(history: WorkHistory) {
        val snapshot = timeProvider.now()
        if (lastAutoBackupDate == snapshot.date) return

        // Cloud folders can block for a while, so never touch them on the main thread.
        val result = withContext(Dispatchers.Default) {
            AutoBackup.run(backupFileController, history, snapshot)
        }
        autoBackupFailed = result == AutoBackupResult.Failed
        if (result == AutoBackupResult.Saved) {
            lastAutoBackupDate = snapshot.date
        }
    }

    fun exportBackup() {
        status = BackupUiStatus.None
        val snapshot = timeProvider.now()
        val content = WorkClockBackupSerializer.encode(
            history = repository.history.value,
            createdAtEpochMillis = snapshot.epochMillis
        )

        backupFileController.saveBackup(
            suggestedFileName = "workclock-backup-${snapshot.date}.${WorkClockBackupSerializer.FileExtension}",
            content = content
        ) { result ->
            status = when (result) {
                BackupFileResult.Success -> BackupUiStatus.Exported
                BackupFileResult.Failure -> BackupUiStatus.Failure
                BackupFileResult.Cancelled -> BackupUiStatus.None
            }
        }
    }

    fun importBackup() {
        status = BackupUiStatus.None
        backupFileController.openBackup { result ->
            when (result) {
                is BackupReadResult.Success -> prepareImport(result.content)
                BackupReadResult.Failure -> status = BackupUiStatus.Failure
                BackupReadResult.Cancelled -> status = BackupUiStatus.None
            }
        }
    }

    fun cancelImport() {
        pendingImport = null
    }

    fun confirmImport() {
        val candidate = pendingImport ?: return
        pendingImport = null

        runCatching {
            workDayStore.savePreImportHistory(repository.history.value)
            repository.update { candidate.backup.history }
        }.onSuccess {
            canUndoImport = true
            status = BackupUiStatus.Imported
        }.onFailure {
            status = BackupUiStatus.Failure
        }
    }

    fun undoImport() {
        val previousHistory = workDayStore.loadPreImportHistory()
        if (previousHistory == null) {
            canUndoImport = false
            status = BackupUiStatus.Failure
            return
        }

        runCatching {
            repository.update { previousHistory }
            workDayStore.clearPreImportHistory()
        }.onSuccess {
            canUndoImport = false
            status = BackupUiStatus.ImportUndone
        }.onFailure {
            status = BackupUiStatus.Failure
        }
    }

    private fun prepareImport(content: String) {
        val backup = WorkClockBackupSerializer.decode(content)
        if (backup == null) {
            status = BackupUiStatus.InvalidFile
            return
        }

        val dates = backup.history.days.keys
        pendingImport = PendingBackupImport(
            backup = backup,
            firstDate = dates.minOrNull()?.toString(),
            lastDate = dates.maxOrNull()?.toString()
        )
    }
}

enum class AutoBackupResult {
    Saved,
    NothingToSave,
    Failed
}

object AutoBackup {
    const val KEEP_COUNT = 14

    private const val FILE_PREFIX = "workclock-auto-"
    private val fileNameRegex = Regex(
        "$FILE_PREFIX\\d{4}-\\d{2}-\\d{2}\\.${WorkClockBackupSerializer.FileExtension}"
    )

    fun fileName(date: LocalDate): String =
        "$FILE_PREFIX$date.${WorkClockBackupSerializer.FileExtension}"

    /** Only ever selects our own dated files, so other files in the folder stay untouched. */
    fun obsoleteFiles(fileNames: List<String>): List<String> =
        fileNames
            .filter(fileNameRegex::matches)
            .sortedDescending()
            .drop(KEEP_COUNT)

    fun run(
        controller: BackupFileController,
        history: WorkHistory,
        snapshot: TimeSnapshot
    ): AutoBackupResult {
        // An empty history must not rotate the good backups out of the folder.
        if (history.days.isEmpty()) return AutoBackupResult.NothingToSave

        val existingFiles = controller.listAutoBackupFiles() ?: return AutoBackupResult.Failed
        val fileName = fileName(snapshot.date)
        if (fileName in existingFiles) return AutoBackupResult.Saved

        val content = WorkClockBackupSerializer.encode(
            history = history,
            createdAtEpochMillis = snapshot.epochMillis
        )
        if (!controller.writeAutoBackup(fileName, content)) return AutoBackupResult.Failed

        obsoleteFiles(existingFiles + fileName).forEach(controller::deleteAutoBackupFile)
        return AutoBackupResult.Saved
    }
}

@Composable
fun rememberBackupStateHolder(
    workDayStore: WorkDayStore,
    repository: WorkHistoryRepository,
    timeProvider: TimeProvider,
    backupFileController: BackupFileController
): BackupStateHolder = remember(workDayStore, repository, timeProvider, backupFileController) {
    BackupStateHolder(
        workDayStore = workDayStore,
        repository = repository,
        timeProvider = timeProvider,
        backupFileController = backupFileController
    )
}
