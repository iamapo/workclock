package com.iamapo.timetracker.backup

enum class BackupUiStatus {
    None,
    Exported,
    Imported,
    ImportUndone,
    InvalidFile,
    Failure
}

data class PendingBackupImport(
    val backup: com.iamapo.timetracker.data.WorkClockBackup,
    val firstDate: String?,
    val lastDate: String?
) {
    val dayCount: Int = backup.history.days.size
}
