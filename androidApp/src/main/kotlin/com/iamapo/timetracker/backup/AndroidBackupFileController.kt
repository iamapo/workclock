package com.iamapo.timetracker.backup

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.iamapo.timetracker.data.WorkClockBackupSerializer
import java.io.ByteArrayOutputStream

class AndroidBackupFileController(
    private val activity: ComponentActivity
) : BackupFileController {
    private var pendingExport: PendingExport? = null
    private var pendingImport: ((BackupReadResult) -> Unit)? = null

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

    override fun saveBackup(
        suggestedFileName: String,
        content: String,
        onResult: (BackupFileResult) -> Unit
    ) {
        if (pendingExport != null || pendingImport != null) {
            onResult(BackupFileResult.Failure)
            return
        }

        pendingExport = PendingExport(content, onResult)
        exportLauncher.launch(suggestedFileName)
    }

    override fun openBackup(onResult: (BackupReadResult) -> Unit) {
        if (pendingExport != null || pendingImport != null) {
            onResult(BackupReadResult.Failure)
            return
        }

        pendingImport = onResult
        importLauncher.launch(arrayOf("*/*"))
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

    private data class PendingExport(
        val content: String,
        val onResult: (BackupFileResult) -> Unit
    )

    private companion object {
        const val BackupMimeType = "application/octet-stream"
        const val DefaultBufferSize = 8 * 1024
    }
}
