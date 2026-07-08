package com.iamapo.timetracker.domain

data class WorkDay(
    val kind: WorkDayKind = WorkDayKind.Work,
    val status: WorkStatus = WorkStatus.NotStarted,
    val startMinute: Int? = null,
    val activeSessionStartMinute: Int? = null,
    val pauseStartedMinute: Int? = null,
    val workedMinutes: Int = 0,
    val breakMinutes: Int = 0,
    val lastBreakMinutes: Int? = null,
    val weeklyWorkedBeforeTodayMinutes: Int = 0,
    val events: List<WorkEvent> = emptyList(),
    val config: WorkDayConfig = WorkDayConfig()
) {
    companion object {
        fun preview(): WorkDay = WorkDay(
            kind = WorkDayKind.Work,
            status = WorkStatus.Working,
            startMinute = 8 * 60 + 42,
            activeSessionStartMinute = 12 * 60 + 26,
            workedMinutes = 3 * 60 + 5,
            breakMinutes = 32,
            lastBreakMinutes = 24,
            weeklyWorkedBeforeTodayMinutes = 16 * 60 + 28,
            events = listOf(
                WorkEvent(8 * 60 + 42, "Arbeitsbeginn", WorkEventKind.Work),
                WorkEvent(12 * 60 + 2, "Pause gestartet", WorkEventKind.Break),
                WorkEvent(12 * 60 + 26, "Weitergearbeitet", WorkEventKind.Work)
            )
        )
    }
}
