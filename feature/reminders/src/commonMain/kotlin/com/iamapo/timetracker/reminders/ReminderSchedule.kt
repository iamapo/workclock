package com.iamapo.timetracker.reminders

data class Reminder(
    val title: String,
    val body: String,
    val triggerAtEpochMillis: Long
)

data class ReminderSchedule(
    val breakReminder: Reminder?,
    val dailyTargetReminder: Reminder?,
    val weeklyTargetReminder: Reminder?
) {
    companion object {
        val None = ReminderSchedule(null, null, null)
    }
}

interface ReminderScheduler {
    fun apply(schedule: ReminderSchedule)
    fun requestAuthorization(onResult: (Boolean) -> Unit)
}

object NoOpReminderScheduler : ReminderScheduler {
    override fun apply(schedule: ReminderSchedule) = Unit
    override fun requestAuthorization(onResult: (Boolean) -> Unit) = onResult(true)
}
