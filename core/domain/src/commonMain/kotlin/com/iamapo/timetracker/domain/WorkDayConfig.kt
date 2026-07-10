package com.iamapo.timetracker.domain

data class WorkDayConfig(
    val dailyTargetMinutes: Int = 8 * 60,
    val requiredBreakMinutes: Int = 30,
    val weeklyTargetMinutes: Int = 40 * 60
)
