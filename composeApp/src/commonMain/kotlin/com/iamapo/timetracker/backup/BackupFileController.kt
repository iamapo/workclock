package com.iamapo.timetracker.backup

interface BackupFileController {
    fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    )

    fun openBackup(onResult: (BackupReadResult) -> Unit)
}

sealed interface BackupFileResult {
    data object Success : BackupFileResult
    data object Cancelled : BackupFileResult
    data object Failure : BackupFileResult
}

sealed interface BackupReadResult {
    data class Success(val content: String) : BackupReadResult
    data object Cancelled : BackupReadResult
    data object Failure : BackupReadResult
}

object NoOpBackupFileController : BackupFileController {
    override fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    ) {
        onResult(BackupFileResult.Failure)
    }

    override fun openBackup(onResult: (BackupReadResult) -> Unit) {
        onResult(BackupReadResult.Failure)
    }
}
