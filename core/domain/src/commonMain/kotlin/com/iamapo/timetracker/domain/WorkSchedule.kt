package com.iamapo.timetracker.domain

import kotlinx.datetime.DayOfWeek

data class WorkSchedule(
    val mondayMinutes: Int = 8 * 60,
    val tuesdayMinutes: Int = 8 * 60,
    val wednesdayMinutes: Int = 8 * 60,
    val thursdayMinutes: Int = 8 * 60,
    val fridayMinutes: Int = 8 * 60,
    val saturdayMinutes: Int = 0,
    val sundayMinutes: Int = 0
) {
    val weeklyTargetMinutes: Int
        get() = mondayMinutes + tuesdayMinutes + wednesdayMinutes + thursdayMinutes +
            fridayMinutes + saturdayMinutes + sundayMinutes

    fun targetMinutes(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
        DayOfWeek.MONDAY -> mondayMinutes
        DayOfWeek.TUESDAY -> tuesdayMinutes
        DayOfWeek.WEDNESDAY -> wednesdayMinutes
        DayOfWeek.THURSDAY -> thursdayMinutes
        DayOfWeek.FRIDAY -> fridayMinutes
        DayOfWeek.SATURDAY -> saturdayMinutes
        DayOfWeek.SUNDAY -> sundayMinutes
    }

    fun withTarget(dayOfWeek: DayOfWeek, minutes: Int): WorkSchedule = when (dayOfWeek) {
        DayOfWeek.MONDAY -> copy(mondayMinutes = minutes)
        DayOfWeek.TUESDAY -> copy(tuesdayMinutes = minutes)
        DayOfWeek.WEDNESDAY -> copy(wednesdayMinutes = minutes)
        DayOfWeek.THURSDAY -> copy(thursdayMinutes = minutes)
        DayOfWeek.FRIDAY -> copy(fridayMinutes = minutes)
        DayOfWeek.SATURDAY -> copy(saturdayMinutes = minutes)
        DayOfWeek.SUNDAY -> copy(sundayMinutes = minutes)
    }

    companion object {
        fun fromConfig(config: WorkDayConfig): WorkSchedule {
            var remainingDelta = config.weeklyTargetMinutes -
                config.dailyTargetMinutes * WorkdaysPerWeek
            val targets = MutableList(WorkdaysPerWeek) { config.dailyTargetMinutes }
            for (index in targets.lastIndex downTo 0) {
                if (remainingDelta == 0) break
                val availableDelta = if (remainingDelta > 0) {
                    MaxTargetMinutesPerDay - targets[index]
                } else {
                    -targets[index]
                }
                val appliedDelta = remainingDelta.coerceIn(
                    availableDelta.coerceAtMost(0),
                    availableDelta.coerceAtLeast(0)
                )
                targets[index] += appliedDelta
                remainingDelta -= appliedDelta
            }
            return WorkSchedule(
                mondayMinutes = targets[0],
                tuesdayMinutes = targets[1],
                wednesdayMinutes = targets[2],
                thursdayMinutes = targets[3],
                fridayMinutes = targets[4]
            )
        }

        fun weekdays(dailyTargetMinutes: Int): WorkSchedule = WorkSchedule(
            mondayMinutes = dailyTargetMinutes,
            tuesdayMinutes = dailyTargetMinutes,
            wednesdayMinutes = dailyTargetMinutes,
            thursdayMinutes = dailyTargetMinutes,
            fridayMinutes = dailyTargetMinutes
        )

        private const val WorkdaysPerWeek = 5
        private const val MaxTargetMinutesPerDay = 16 * 60
    }
}
