package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import kotlin.math.max
import kotlin.math.min

internal data class WorkDaySummary(
    val workedMinutes: Int,
    val breakMinutes: Int,
    val remainingWorkMinutes: Int,
    val missingBreakMinutes: Int,
    val endMinute: Int,
    val progress: Float
)

internal class WorkDaySummaryCalculator {
    fun calculate(day: WorkDay, snapshot: TimeSnapshot): WorkDaySummary {
        val workedMinutes = day.workedMinutes + activeWorkMinutes(day, snapshot.minuteOfDay)
        val breakMinutes = day.breakMinutes + activeBreakMinutes(day, snapshot.minuteOfDay)
        val remainingWorkMinutes = max(day.config.dailyTargetMinutes - workedMinutes, 0)
        val missingBreakMinutes = max(day.config.requiredBreakMinutes - breakMinutes, 0)
        val endMinute = when (day.status) {
            WorkStatus.NotStarted -> snapshot.minuteOfDay + day.config.dailyTargetMinutes + day.config.requiredBreakMinutes
            WorkStatus.Working,
            WorkStatus.Paused -> snapshot.minuteOfDay + remainingWorkMinutes + missingBreakMinutes
            WorkStatus.Finished -> finishedMinute(day, snapshot.minuteOfDay)
        }
        val progress = min(workedMinutes.toFloat() / day.config.dailyTargetMinutes.toFloat(), 1f)

        return WorkDaySummary(
            workedMinutes = workedMinutes,
            breakMinutes = breakMinutes,
            remainingWorkMinutes = remainingWorkMinutes,
            missingBreakMinutes = missingBreakMinutes,
            endMinute = endMinute,
            progress = progress
        )
    }

    private fun activeWorkMinutes(day: WorkDay, nowMinute: Int): Int {
        val sessionStart = day.activeSessionStartMinute
        return if (day.status == WorkStatus.Working && sessionStart != null) {
            elapsedMinutes(sessionStart, nowMinute)
        } else 0
    }

    private fun activeBreakMinutes(day: WorkDay, nowMinute: Int): Int {
        val pauseStart = day.pauseStartedMinute
        return if (day.status == WorkStatus.Paused && pauseStart != null) {
            elapsedMinutes(pauseStart, nowMinute)
        } else 0
    }

    private fun elapsedMinutes(startMinute: Int, endMinute: Int): Int =
        if (endMinute >= startMinute) endMinute - startMinute else MinutesPerDay - startMinute + endMinute

    private fun finishedMinute(day: WorkDay, fallbackMinute: Int): Int =
        day.events.lastOrNull { it.title == FinishedEventTitle }?.minuteOfDay ?: fallbackMinute

    private companion object {
        const val FinishedEventTitle = "Arbeitstag beendet"
        const val MinutesPerDay = 24 * 60
    }
}
