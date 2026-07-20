package com.iamapo.timetracker.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.iamapo.timetracker.data.WorkClockBackupSerializer
import com.iamapo.timetracker.data.WorkDayStore
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository

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
