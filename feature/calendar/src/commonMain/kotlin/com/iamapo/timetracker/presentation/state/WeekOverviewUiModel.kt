package com.iamapo.timetracker.presentation.state

data class WeekOverviewUiModel(
    val reached: String,
    val balance: String,
    val balanceMinutes: Int,
    val isPositiveBalance: Boolean,
    val carry: String?,
    val carryMinutes: Int,
    val isPositiveCarry: Boolean,
    val weekNumber: Int,
    val days: List<WeekDayProgressUiModel>
)
