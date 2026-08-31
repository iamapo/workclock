package com.iamapo.timetracker.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.datetime.LocalDate

enum class ReportUiStatus {
    None,
    Exported,
    Failure
}

class ReportStateHolder(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val fileController: ReportFileController,
    private val reportBuilder: WorkTimeReportBuilder = WorkTimeReportBuilder(),
    private val pdfGenerator: WorkTimePdfGenerator = WorkTimePdfGenerator()
) {
    var period by mutableStateOf(ReportPeriod(ReportPeriodType.Week, timeProvider.now().date))
        private set

    var status by mutableStateOf(ReportUiStatus.None)
        private set

    val canNavigateNext: Boolean
        get() = period.next().startDate <= timeProvider.now().date

    fun selectPeriodType(type: ReportPeriodType) {
        period = period.withType(type)
        status = ReportUiStatus.None
    }

    fun showPreviousPeriod() {
        period = period.previous()
        status = ReportUiStatus.None
    }

    fun showNextPeriod() {
        val next = period.next()
        if (next.startDate <= timeProvider.now().date) {
            period = next
            status = ReportUiStatus.None
        }
    }

    fun exportPdf() {
        status = ReportUiStatus.None
        val snapshot = timeProvider.now()
        val report = reportBuilder.build(repository.history.value, snapshot, period)
        val pdfContent = pdfGenerator.generate(report)
        fileController.saveReport(
            suggestedFileName = suggestedFileName(period),
            pdfContent = pdfContent
        ) { result ->
            status = when (result) {
                ReportFileResult.Success -> ReportUiStatus.Exported
                ReportFileResult.Failure -> ReportUiStatus.Failure
                ReportFileResult.Cancelled -> ReportUiStatus.None
            }
        }
    }

    private fun suggestedFileName(period: ReportPeriod): String {
        val start = period.startDate
        val suffix = when (period.type) {
            ReportPeriodType.Week -> "${formatDateForFile(start)}_${formatDateForFile(period.endDate)}"
            ReportPeriodType.Month -> start.year.toString() + "-" + start.monthNumber().toString().padStart(2, '0')
        }
        return "workclock-arbeitszeit-$suffix.pdf"
    }

    private fun formatDateForFile(date: LocalDate): String =
        date.year.toString() + "-" + date.monthNumber().toString().padStart(2, '0') + "-" +
            date.day.toString().padStart(2, '0')

    private fun LocalDate.monthNumber(): Int = month.ordinal + 1
}

@Composable
fun rememberReportStateHolder(
    repository: WorkHistoryRepository,
    timeProvider: TimeProvider,
    fileController: ReportFileController
): ReportStateHolder = remember(repository, timeProvider, fileController) {
    ReportStateHolder(repository, timeProvider, fileController)
}
