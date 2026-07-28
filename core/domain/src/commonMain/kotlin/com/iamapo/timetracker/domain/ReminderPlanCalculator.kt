package com.iamapo.timetracker.domain

data class ReminderPlan(
    val breakReminderDelayMinutes: Int?,
    val dailyTargetReminderDelayMinutes: Int?,
    val weeklyTargetReminderDelayMinutes: Int?
)

/**
 * Computes when (in minutes from now) each reminder should fire, or null to cancel it.
 * Only future thresholds are scheduled so reopening the app after a reminder fired does not
 * enqueue the same reminder again.
 */
class ReminderPlanCalculator(
    private val summaryCalculator: WorkDaySummaryCalculator = WorkDaySummaryCalculator(),
    private val weeklyBalanceCalculator: WeeklyBalanceCalculator = WeeklyBalanceCalculator()
) {
    fun calculate(day: WorkDay, snapshot: TimeSnapshot): ReminderPlan {
        val summary = summaryCalculator.calculate(day, snapshot)

        val breakDelay = summary.missingBreakMinutes.takeIf { delay ->
            day.status == WorkStatus.Paused && delay > 0
        }

        val dailyDelay = summary.remainingWorkMinutes.takeIf { delay ->
            day.status == WorkStatus.Working && delay > 0
        }

        val weeklyDelay = if (day.status == WorkStatus.Working) {
            val weeklyBalance = weeklyBalanceCalculator.calculate(day, snapshot.date, summary.workedMinutes)
            (day.config.weeklyTargetMinutes - weeklyBalance.workedMinutes).takeIf { delay -> delay > 0 }
        } else {
            null
        }

        return ReminderPlan(
            breakReminderDelayMinutes = breakDelay,
            dailyTargetReminderDelayMinutes = dailyDelay,
            weeklyTargetReminderDelayMinutes = weeklyDelay
        )
    }
}
