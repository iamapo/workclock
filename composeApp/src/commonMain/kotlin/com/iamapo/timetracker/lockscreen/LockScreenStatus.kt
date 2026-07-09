package com.iamapo.timetracker.lockscreen

data class LockScreenStatus(
    val visible: Boolean,
    val phase: String,
    val title: String,
    val phaseLabel: String,
    val subtitle: String,
    val startedAtEpochMillis: Long,
    val elapsedMinutes: Int,
    val workedMinutes: Int,
    val breakMinutes: Int
) {
    companion object {
        const val PhaseHidden = "hidden"
        const val PhaseWorking = "working"
        const val PhasePaused = "paused"

        val Hidden = LockScreenStatus(
            visible = false,
            phase = PhaseHidden,
            title = "WorkClock",
            phaseLabel = "",
            subtitle = "",
            startedAtEpochMillis = 0L,
            elapsedMinutes = 0,
            workedMinutes = 0,
            breakMinutes = 0
        )
    }
}

interface LockScreenStatusController {
    fun publish(status: LockScreenStatus)
}

object NoOpLockScreenStatusController : LockScreenStatusController {
    override fun publish(status: LockScreenStatus) = Unit
}
