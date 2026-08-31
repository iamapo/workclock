package com.iamapo.timetracker.report

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlin.math.abs

class WorkTimePdfGenerator {
    fun generate(report: WorkTimeReport): String {
        val content = pageContent(report)
        val objects = listOf(
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [5 0 R] /Count 1 >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents 6 0 R >>",
            "<< /Length ${content.length} >>\nstream\n$content\nendstream",
            "<< /Title ${pdfText("WorkClock Arbeitszeitnachweis")} /Creator ${pdfText("WorkClock")} >>"
        )

        val output = StringBuilder("%PDF-1.4\n")
        val offsets = mutableListOf<Int>()
        objects.forEachIndexed { index, body ->
            offsets += output.length
            output.append(index + 1).append(" 0 obj\n").append(body).append("\nendobj\n")
        }
        val xrefOffset = output.length
        output.append("xref\n0 ").append(objects.size + 1).append("\n")
        output.append("0000000000 65535 f \n")
        offsets.forEach { offset ->
            output.append(offset.toString().padStart(10, '0')).append(" 00000 n \n")
        }
        output.append("trailer\n<< /Size ").append(objects.size + 1)
            .append(" /Root 1 0 R /Info 7 0 R >>\nstartxref\n")
            .append(xrefOffset).append("\n%%EOF\n")

        return output.toString().also { pdf ->
            check(pdf.all { it.code in 0..127 }) { "PDF output must remain ASCII for portable file writing" }
        }
    }

    private fun pageContent(report: WorkTimeReport): String = buildString {
        append("q\n")
        fillRect(0, 0, PageWidth, PageHeight, "0.973 0.965 0.945")
        fillRect(0, 824, PageWidth, 18, "0.925 0.310 0.220")

        text("WORKCLOCK", 42, 790, 10, bold = true, color = "0.925 0.310 0.220")
        text("ARBEITSZEITNACHWEIS", 42, 765, 21, bold = true)
        text(periodTitle(report.period), 42, 744, 11, color = "0.340 0.330 0.300")
        text("Erstellt am ${formatDate(report.generatedOn)}", 413, 790, 8, color = "0.440 0.430 0.400")

        summaryCard(42, "IST", duration(report.workedMinutes), "Erfasste Arbeitszeit")
        summaryCard(213, "SOLL", duration(report.targetMinutes), "Sollzeit bis Stichtag")
        summaryCard(384, "SALDO", signedDuration(report.balanceMinutes), "Ist minus Soll")

        fillRect(42, TableTop, 511, HeaderHeight, "0.145 0.180 0.215")
        tableHeader("DATUM", 47)
        tableHeader("TAG", 109)
        tableHeader("ART", 143)
        tableHeader("VON", 210)
        tableHeader("BIS", 250)
        tableHeader("IST", 291)
        tableHeader("PAUSE", 338)
        tableHeader("SOLL", 383)
        tableHeader("SALDO", 430)

        report.rows.forEachIndexed { index, row ->
            val rowTop = TableTop - RowHeight * (index + 1)
            if (index % 2 == 1) fillRect(42, rowTop, 511, RowHeight, "0.950 0.940 0.915")
            strokeLine(42, rowTop, 553, rowTop, "0.830 0.810 0.760")
            val baseline = rowTop + 5
            tableText(formatDate(row.date), 47, baseline)
            tableText(weekday(row.date), 109, baseline)
            tableText(dayType(row.dayType), 143, baseline)
            tableText(row.startMinute?.let(::clock) ?: "-", 210, baseline)
            tableText(row.endMinute?.let(::clock) ?: "-", 250, baseline)
            tableText(duration(row.workedMinutes), 291, baseline)
            tableText(duration(row.breakMinutes), 338, baseline)
            tableText(duration(row.targetMinutes), 383, baseline)
            tableText(signedDuration(row.balanceMinutes), 430, baseline, bold = true)
        }

        val totalTop = TableTop - RowHeight * (report.rows.size + 1) - 5
        fillRect(42, totalTop, 511, 24, "0.890 0.865 0.805")
        text("SUMME", 47, totalTop + 8, 8, bold = true)
        text(duration(report.workedMinutes), 291, totalTop + 8, 8, bold = true)
        text(duration(report.breakMinutes), 338, totalTop + 8, 8, bold = true)
        text(duration(report.targetMinutes), 383, totalTop + 8, 8, bold = true)
        text(signedDuration(report.balanceMinutes), 430, totalTop + 8, 8, bold = true)

        text(
            "Urlaub und Krankheit werden entsprechend dem Arbeitsplan als Arbeitszeit angerechnet.",
            42,
            42,
            7,
            color = "0.440 0.430 0.400"
        )
        text("WorkClock - Seite 1 von 1", 430, 42, 7, color = "0.440 0.430 0.400")
        append("Q\n")
    }

    private fun StringBuilder.summaryCard(x: Int, label: String, value: String, detail: String) {
        fillRect(x, 665, 159, 57, "0.935 0.920 0.880")
        text(label, x + 12, 705, 7, bold = true, color = "0.440 0.430 0.400")
        text(value, x + 12, 684, 15, bold = true)
        text(detail, x + 12, 672, 6, color = "0.440 0.430 0.400")
    }

    private fun StringBuilder.tableHeader(value: String, x: Int) =
        text(value, x, TableTop + 6, 6, bold = true, color = "1 1 1")

    private fun StringBuilder.tableText(value: String, x: Int, y: Int, bold: Boolean = false) =
        text(value, x, y, 7, bold = bold)

    private fun StringBuilder.text(
        value: String,
        x: Int,
        y: Int,
        size: Int,
        bold: Boolean = false,
        color: String = "0.145 0.135 0.115"
    ) {
        append("BT ").append(color).append(" rg /")
            .append(if (bold) "F2" else "F1")
            .append(' ').append(size).append(" Tf ")
            .append(x).append(' ').append(y).append(" Td ")
            .append(pdfText(value)).append(" Tj ET\n")
    }

    private fun StringBuilder.fillRect(x: Int, y: Int, width: Int, height: Int, color: String) {
        append(color).append(" rg ").append(x).append(' ').append(y).append(' ')
            .append(width).append(' ').append(height).append(" re f\n")
    }

    private fun StringBuilder.strokeLine(x1: Int, y1: Int, x2: Int, y2: Int, color: String) {
        append(color).append(" RG 0.5 w ").append(x1).append(' ').append(y1)
            .append(" m ").append(x2).append(' ').append(y2).append(" l S\n")
    }

    private fun periodTitle(period: ReportPeriod): String = when (period.type) {
        ReportPeriodType.Week -> "Woche ${isoWeekNumber(period.startDate)} | " +
            "${formatDate(period.startDate)} bis ${formatDate(period.endDate)}"
        ReportPeriodType.Month -> monthName(period.startDate.month.ordinal + 1) + " ${period.startDate.year}"
    }

    private fun isoWeekNumber(date: LocalDate): Int {
        val week = (date.dayOfYear - date.dayOfWeek.isoDayNumber + 10) / 7
        return when {
            week < 1 -> isoWeeksInYear(date.year - 1)
            week > isoWeeksInYear(date.year) -> 1
            else -> week
        }
    }

    private fun isoWeeksInYear(year: Int): Int {
        val januaryFirst = LocalDate(year, 1, 1).dayOfWeek.isoDayNumber
        return if (januaryFirst == 4 || januaryFirst == 3 && isLeapYear(year)) 53 else 52
    }

    private fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    private fun monthName(month: Int): String = listOf(
        "Januar", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    )[month - 1]

    private fun weekday(date: LocalDate): String =
        listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So").get(date.dayOfWeek.isoDayNumber - 1)

    private fun dayType(type: ReportDayType): String = when (type) {
        ReportDayType.Work -> "Arbeit"
        ReportDayType.Vacation -> "Urlaub"
        ReportDayType.Sick -> "Krank"
        ReportDayType.Holiday -> "Feiertag"
        ReportDayType.DayOff -> "Frei"
        ReportDayType.Missing -> "Kein Eintrag"
    }

    private fun formatDate(date: LocalDate): String =
        date.day.toString().padStart(2, '0') + "." +
            (date.month.ordinal + 1).toString().padStart(2, '0') + "." + date.year

    private fun clock(minuteOfDay: Int): String {
        val normalized = ((minuteOfDay % MinutesPerDay) + MinutesPerDay) % MinutesPerDay
        return (normalized / 60).toString().padStart(2, '0') + ":" +
            (normalized % 60).toString().padStart(2, '0')
    }

    private fun duration(minutes: Int): String =
        (abs(minutes) / 60).toString() + ":" + (abs(minutes) % 60).toString().padStart(2, '0')

    private fun signedDuration(minutes: Int): String = when {
        minutes > 0 -> "+" + duration(minutes)
        minutes < 0 -> "-" + duration(minutes)
        else -> "0:00"
    }

    private fun pdfText(value: String): String = buildString {
        append('<')
        value.forEach { character ->
            append(winAnsiByte(character).toString(16).uppercase().padStart(2, '0'))
        }
        append('>')
    }

    private fun winAnsiByte(character: Char): Int = when (character) {
        'Ä' -> 0xC4
        'Ö' -> 0xD6
        'Ü' -> 0xDC
        'ä' -> 0xE4
        'ö' -> 0xF6
        'ü' -> 0xFC
        'ß' -> 0xDF
        else -> character.code.takeIf { it in 32..126 } ?: '?'.code
    }

    private companion object {
        const val PageWidth = 595
        const val PageHeight = 842
        const val TableTop = 625
        const val HeaderHeight = 20
        const val RowHeight = 16
        const val MinutesPerDay = 24 * 60
    }
}
