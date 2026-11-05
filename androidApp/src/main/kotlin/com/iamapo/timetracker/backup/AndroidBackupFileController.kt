package com.iamapo.timetracker.backup

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.iamapo.timetracker.data.WorkClockBackupSerializer
import java.io.ByteArrayOutputStream

class AndroidBackupFileController(
    private val activity: ComponentActivity
) : BackupFileController {
    private var pendingExport: PendingExport? = null
    private var pendingImport: ((BackupReadResult) -> Unit)? = null
    private var pendingFolderChoice: ((BackupFileResult) -> Unit)? = null

    // The persisted grant is the only record of the folder, so it disappears with the access.
    private val autoBackupFolderUri: Uri?
        get() = activity.contentResolver.persistedUriPermissions
            .firstOrNull { permission ->
                permission.isWritePermission
            }
            ?.uri

    private val exportLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument(BackupMimeType)
    ) { uri ->
        val pending = pendingExport ?: return@registerForActivityResult
        pendingExport = null

        if (uri == null) {
            pending.onResult(BackupFileResult.Cancelled)
            return@registerForActivityResult
        }

        val result = runCatching {
            activity.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(pending.content.encodeToByteArray())
            } ?: error("Could not open backup destination")
        }.fold(
            onSuccess = { BackupFileResult.Success },
            onFailure = { BackupFileResult.Failure }
        )
        pending.onResult(result)
    }

    private val importLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val callback = pendingImport ?: return@registerForActivityResult
        pendingImport = null

        if (uri == null) {
            callback(BackupReadResult.Cancelled)
            return@registerForActivityResult
        }

        val result = runCatching { readBackup(uri) }.fold(
            onSuccess = { BackupReadResult.Success(it) },
            onFailure = { BackupReadResult.Failure }
        )
        callback(result)
    }

    private val folderLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val callback = pendingFolderChoice ?: return@registerForActivityResult
        pendingFolderChoice = null

        if (uri == null) {
            callback(BackupFileResult.Cancelled)
            return@registerForActivityResult
        }

        val result = runCatching {
            activity.contentResolver.takePersistableUriPermission(uri, FOLDER_ACCESS_FLAGS)
        }.fold(
            onSuccess = { BackupFileResult.Success },
            onFailure = { BackupFileResult.Failure }
        )
        callback(result)
    }

    override fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    ) {
        if (isPickerOpen) {
            onResult(BackupFileResult.Failure)
            return
        }

        pendingExport = PendingExport(content, onResult)
        exportLauncher.launch(suggestedFileName)
    }

    override fun openBackup(onResult: (BackupReadResult) -> Unit) {
        if (isPickerOpen) {
            onResult(BackupReadResult.Failure)
            return
        }

        pendingImport = onResult
        importLauncher.launch(arrayOf("*/*"))
    }

    override fun hasAutoBackupFolder(): Boolean = autoBackupFolderUri != null

    override fun chooseAutoBackupFolder(onResult: (BackupFileResult) -> Unit) {
        if (isPickerOpen) {
            onResult(BackupFileResult.Failure)
            return
        }

        pendingFolderChoice = onResult
        folderLauncher.launch(null)
    }

    override fun clearAutoBackupFolder() {
        val folderUri = autoBackupFolderUri ?: return
        activity.contentResolver.releasePersistableUriPermission(folderUri, FOLDER_ACCESS_FLAGS)
    }

    override fun listAutoBackupFiles(): List<String>? = runCatching {
        val folderUri = requireNotNull(autoBackupFolderUri)
        autoBackupDocumentMap(folderUri).keys.toList()
    }.getOrNull()

    override fun writeAutoBackup(fileName: String, content: String): Boolean {
        var documentUri: Uri? = null
        return runCatching {
            val folderUri = requireNotNull(autoBackupFolderUri)
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
            )
            val createdUri = DocumentsContract.createDocument(
                activity.contentResolver,
                parentUri,
                BackupMimeType,
                fileName
            ) ?: error("Could not create backup file")
            documentUri = createdUri

            activity.contentResolver.openOutputStream(createdUri, "wt")?.use { output ->
                output.write(content.encodeToByteArray())
            } ?: error("Could not open backup destination")
        }.onFailure {
            // A partial file would count as today's backup and block every retry.
            documentUri?.let(::deleteDocument)
        }.isSuccess
    }

    override fun deleteAutoBackupFile(fileName: String) {
        runCatching {
            val folderUri = requireNotNull(autoBackupFolderUri)
            autoBackupDocumentMap(folderUri)[fileName]?.let(::deleteDocument)
        }
    }

    private fun deleteDocument(uri: Uri) {
        runCatching {
            DocumentsContract.deleteDocument(activity.contentResolver, uri)
        }
    }

    private fun autoBackupDocumentMap(folderUri: Uri): Map<String, Uri> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID
        )
        val cursor = activity.contentResolver.query(childrenUri, projection, null, null, null)
            ?: error("Could not read backup folder")

        return cursor.use { rows ->
            buildMap {
                while (rows.moveToNext()) {
                    put(
                        rows.getString(0),
                        DocumentsContract.buildDocumentUriUsingTree(folderUri, rows.getString(1))
                    )
                }
            }
        }
    }

    private fun readBackup(uri: Uri): String {
        val bytes = activity.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DefaultBufferSize)
            var totalBytes = 0

            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                totalBytes += read
                require(totalBytes <= WorkClockBackupSerializer.MaxBackupBytes)
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: error("Could not open backup file")

        return bytes.decodeToString(throwOnInvalidSequence = true)
    }

    private val isPickerOpen: Boolean
        get() = pendingExport != null || pendingImport != null || pendingFolderChoice != null

    private data class PendingExport(
        val content: String,
        val onResult: (BackupFileResult) -> Unit
    )

    private companion object {
        const val BackupMimeType = "application/octet-stream"
        const val DefaultBufferSize = 8 * 1024
        const val FOLDER_ACCESS_FLAGS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}
