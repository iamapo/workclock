package com.iamapo.timetracker.reminders

import com.iamapo.timetracker.domain.ReminderPlanCalculator
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.presentation.localized
import com.iamapo.timetracker.resources.*

object ReminderScheduleMapper {
    private val planCalculator = ReminderPlanCalculator()

    fun map(day: WorkDay, snapshot: TimeSnapshot, enabled: Boolean): ReminderSchedule {
        if (!enabled) return ReminderSchedule.None

        val plan = planCalculator.calculate(day, snapshot)
        val title = localized(Res.string.app_name)

        return ReminderSchedule(
            breakReminder = plan.breakReminderDelayMinutes?.let { delay ->
                reminder(title, localized(Res.string.reminder_break_body), snapshot, delay)
            },
            dailyTargetReminder = plan.dailyTargetReminderDelayMinutes?.let { delay ->
                reminder(title, localized(Res.string.reminder_daily_target_body), snapshot, delay)
            },
            weeklyTargetReminder = plan.weeklyTargetReminderDelayMinutes?.let { delay ->
                reminder(title, localized(Res.string.reminder_weekly_target_body), snapshot, delay)
            }
        )
    }

    private fun reminder(
        title: String,
        body: String,
        snapshot: TimeSnapshot,
        delayMinutes: Int
    ): Reminder = Reminder(
        title = title,
        body = body,
        triggerAtEpochMillis = snapshot.epochMillis + delayMinutes * MillisPerMinute
    )

    private const val MillisPerMinute = 60_000L
}
