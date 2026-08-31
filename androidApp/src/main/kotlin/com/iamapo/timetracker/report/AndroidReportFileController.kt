package com.iamapo.timetracker.report

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class AndroidReportFileController(
    private val activity: ComponentActivity
) : ReportFileController {
    private var pendingExport: PendingExport? = null

    private val exportLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument(PdfMimeType)
    ) { uri ->
        val pending = pendingExport ?: return@registerForActivityResult
        pendingExport = null

        if (uri == null) {
            pending.onResult(ReportFileResult.Cancelled)
            return@registerForActivityResult
        }

        val result = runCatching {
            activity.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(pending.pdfContent.encodeToByteArray())
            } ?: error("Could not open report destination")
        }.fold(
            onSuccess = { ReportFileResult.Success },
            onFailure = { ReportFileResult.Failure }
        )
        pending.onResult(result)
    }

    override fun saveReport(
        suggestedFileName: String,
        pdfContent: String,
        onResult: (ReportFileResult) -> Unit
    ) {
        if (pendingExport != null) {
            onResult(ReportFileResult.Failure)
            return
        }
        pendingExport = PendingExport(pdfContent, onResult)
        exportLauncher.launch(suggestedFileName)
    }

    private data class PendingExport(
        val pdfContent: String,
        val onResult: (ReportFileResult) -> Unit
    )

    private companion object {
        const val PdfMimeType = "application/pdf"
    }
}
