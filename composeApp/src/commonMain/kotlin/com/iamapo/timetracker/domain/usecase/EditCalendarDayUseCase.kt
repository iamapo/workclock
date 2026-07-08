package com.iamapo.timetracker.domain.usecase

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import kotlinx.datetime.LocalDate

class EditCalendarDayUseCase(
    private val repository: WorkHistoryRepository
) {
    fun increaseDay(date: LocalDate) {
        adjustDay(date, ManualEditStepMinutes)
    }

    fun decreaseDay(date: LocalDate) {
        adjustDay(date, -ManualEditStepMinutes)
    }

    fun setVacation(date: LocalDate) {
        updateDay(date, WorkDayKind.Vacation, FullAbsenceDayMinutes, "Urlaub")
    }

    fun setSick(date: LocalDate) {
        updateDay(date, WorkDayKind.Sick, FullAbsenceDayMinutes, "Krank")
    }

    fun clearDay(date: LocalDate) {
        repository.update { history -> history.withoutDay(date) }
    }

    private fun adjustDay(date: LocalDate, deltaMinutes: Int) {
        repository.update { history ->
            val current = history.dayFor(date)
            val updatedMinutes = (current.workedMinutes + deltaMinutes).coerceIn(0, MaxManualDayMinutes)
            history.updateDay(date, WorkDayKind.Work, updatedMinutes, "Manueller Eintrag")
        }
    }

    private fun updateDay(
        date: LocalDate,
        kind: WorkDayKind,
        workedMinutes: Int,
        title: String
    ) {
        repository.update { history ->
            history.updateDay(date, kind, workedMinutes, title)
        }
    }

    private fun WorkHistory.updateDay(
        date: LocalDate,
        kind: WorkDayKind,
        workedMinutes: Int,
        title: String
    ): WorkHistory {
        val current = dayFor(date)
        val updated = current.copy(
            kind = kind,
            status = if (workedMinutes > 0) WorkStatus.Finished else WorkStatus.NotStarted,
            startMinute = null,
            activeSessionStartMinute = null,
            pauseStartedMinute = null,
            workedMinutes = workedMinutes,
            breakMinutes = 0,
            lastBreakMinutes = null,
            weeklyWorkedBeforeTodayMinutes = 0,
            events = if (workedMinutes > 0) {
                listOf(WorkEvent(0, title, WorkEventKind.Target))
            } else {
                emptyList()
            }
        )

        return withDay(date, updated)
    }

    private companion object {
        const val ManualEditStepMinutes = 15
        const val FullAbsenceDayMinutes = 8 * 60
        const val MaxManualDayMinutes = 24 * 60
    }
}
