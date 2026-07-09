package com.iamapo.timetracker.lockscreen

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.TimeTextFormatter
import com.iamapo.timetracker.presentation.WorkDaySummaryCalculator

object LockScreenStatusMapper {
    private val summaryCalculator = WorkDaySummaryCalculator()

    fun map(
        day: WorkDay,
        snapshot: TimeSnapshot,
        enabled: Boolean
    ): LockScreenStatus {
        if (!enabled) return LockScreenStatus.Hidden

        val sessionStartMinute = when (day.status) {
            WorkStatus.Working -> day.activeSessionStartMinute
            WorkStatus.Paused -> day.pauseStartedMinute
            WorkStatus.NotStarted,
            WorkStatus.Finished -> null
        } ?: return LockScreenStatus.Hidden

        val summary = summaryCalculator.calculate(day, snapshot)
        val elapsedMinutes = when (day.status) {
            WorkStatus.Working -> summary.workedMinutes
            WorkStatus.Paused -> elapsedMinutes(sessionStartMinute, snapshot.minuteOfDay)
            WorkStatus.NotStarted,
            WorkStatus.Finished -> 0
        }
        val startedAtEpochMillis = if (snapshot.epochMillis > 0L) {
            snapshot.epochMillis - elapsedMinutes * MillisPerMinute
        } else {
            0L
        }

        return when (day.status) {
            WorkStatus.Working -> LockScreenStatus(
                visible = true,
                phase = LockScreenStatus.PhaseWorking,
                title = "WorkClock",
                phaseLabel = "Arbeitszeit heute",
                subtitle = "Pause: ${TimeTextFormatter.duration(summary.breakMinutes)}",
                startedAtEpochMillis = startedAtEpochMillis,
                elapsedMinutes = elapsedMinutes,
                workedMinutes = summary.workedMinutes,
                breakMinutes = summary.breakMinutes
            )
            WorkStatus.Paused -> LockScreenStatus(
                visible = true,
                phase = LockScreenStatus.PhasePaused,
                title = "WorkClock",
                phaseLabel = "Pause seit",
                subtitle = "Gearbeitet: ${TimeTextFormatter.duration(summary.workedMinutes)}",
                startedAtEpochMillis = startedAtEpochMillis,
                elapsedMinutes = elapsedMinutes,
                workedMinutes = summary.workedMinutes,
                breakMinutes = summary.breakMinutes
            )
            WorkStatus.NotStarted,
            WorkStatus.Finished -> LockScreenStatus.Hidden
        }
    }

    private fun elapsedMinutes(startMinute: Int, endMinute: Int): Int =
        if (endMinute >= startMinute) endMinute - startMinute else MinutesPerDay - startMinute + endMinute

    private const val MinutesPerDay = 24 * 60
    private const val MillisPerMinute = 60_000L
}
