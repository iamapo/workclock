package com.iamapo.timetracker.backup

interface BackupFileController {
    fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    )

    fun openBackup(onResult: (BackupReadResult) -> Unit)

    fun hasAutoBackupFolder(): Boolean

    fun chooseAutoBackupFolder(onResult: (BackupFileResult) -> Unit)

    fun clearAutoBackupFolder()

    /** Returns the file names in the folder, or null if the folder cannot be read. */
    fun listAutoBackupFiles(): List<String>?

    fun writeAutoBackup(fileName: String, content: String): Boolean

    fun deleteAutoBackupFile(fileName: String)
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

    override fun hasAutoBackupFolder(): Boolean = false

    override fun chooseAutoBackupFolder(onResult: (BackupFileResult) -> Unit) {
        onResult(BackupFileResult.Failure)
    }

    override fun clearAutoBackupFolder() = Unit

    override fun listAutoBackupFiles(): List<String>? = null

    override fun writeAutoBackup(fileName: String, content: String): Boolean = false

    override fun deleteAutoBackupFile(fileName: String) = Unit
}
