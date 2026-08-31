package com.iamapo.timetracker.report

interface ReportFileController {
    fun saveReport(
        suggestedFileName: String,
        pdfContent: String,
        onResult: (ReportFileResult) -> Unit
    )
}

sealed interface ReportFileResult {
    data object Success : ReportFileResult
    data object Cancelled : ReportFileResult
    data object Failure : ReportFileResult
}

object NoOpReportFileController : ReportFileController {
    override fun saveReport(
        suggestedFileName: String,
        pdfContent: String,
        onResult: (ReportFileResult) -> Unit
    ) {
        onResult(ReportFileResult.Failure)
    }
}
