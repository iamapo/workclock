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
        updateDay(date, WorkDayKind.Vacation, "Urlaub")
    }

    fun setSick(date: LocalDate) {
        updateDay(date, WorkDayKind.Sick, "Krank")
    }

    fun setForgottenWorkDay(date: LocalDate) {
        repository.update { history ->
            val current = history.dayFor(date)
            val targetMinutes = history.defaultConfig.dailyTargetMinutes.coerceAtLeast(0)
            val hasBreak = targetMinutes > WorkBeforeBreakMinutes
            val endMinute = if (hasBreak) {
                WorkDayBreakEndMinute + targetMinutes - WorkBeforeBreakMinutes
            } else {
                WorkDayStartMinute + targetMinutes
            }
            val breakMinutes = if (hasBreak) DefaultBreakMinutes else 0
            val events = if (hasBreak) {
                listOf(
                    WorkEvent(WorkDayStartMinute, "Arbeitsbeginn", WorkEventKind.Work),
                    WorkEvent(WorkDayBreakStartMinute, "Pause gestartet", WorkEventKind.Break),
                    WorkEvent(WorkDayBreakEndMinute, "Weitergearbeitet", WorkEventKind.Work),
                    WorkEvent(endMinute, "Arbeitstag beendet", WorkEventKind.Target)
                )
            } else {
                listOf(
                    WorkEvent(WorkDayStartMinute, "Arbeitsbeginn", WorkEventKind.Work),
                    WorkEvent(endMinute, "Arbeitstag beendet", WorkEventKind.Target)
                )
            }
            val updated = current.copy(
                kind = WorkDayKind.Work,
                status = WorkStatus.Finished,
                startMinute = WorkDayStartMinute,
                activeSessionStartMinute = null,
                pauseStartedMinute = null,
                workedMinutes = targetMinutes,
                breakMinutes = breakMinutes,
                lastBreakMinutes = breakMinutes.takeIf { it > 0 },
                weeklyWorkedBeforeTodayMinutes = 0,
                events = events,
                config = history.defaultConfig
            )

            history.withDay(date, updated)
        }
    }

    fun clearDay(date: LocalDate) {
        repository.update { history -> history.withoutDay(date) }
    }

    fun setWorkTimes(date: LocalDate, startMinute: Int, breakMinutes: Int, endMinute: Int) {
        require(startMinute in 0 until MinutesPerDay)
        require(endMinute in 0 until MinutesPerDay)
        require(breakMinutes >= 0)

        val elapsedMinutes = if (endMinute >= startMinute) {
            endMinute - startMinute
        } else {
            MinutesPerDay - startMinute + endMinute
        }
        require(breakMinutes <= elapsedMinutes)

        repository.update { history ->
            val current = history.dayFor(date)
            val workedMinutes = elapsedMinutes - breakMinutes
            val breakStart = (startMinute + workedMinutes / 2) % MinutesPerDay
            val breakEnd = (breakStart + breakMinutes) % MinutesPerDay
            val events = buildList {
                add(WorkEvent(startMinute, "Arbeitsbeginn", WorkEventKind.Work))
                if (breakMinutes > 0) {
                    add(WorkEvent(breakStart, "Pause gestartet", WorkEventKind.Break))
                    add(WorkEvent(breakEnd, "Weitergearbeitet", WorkEventKind.Work))
                }
                add(WorkEvent(endMinute, "Arbeitstag beendet", WorkEventKind.Target))
            }
            history.withDay(date, current.copy(
                kind = WorkDayKind.Work,
                status = WorkStatus.Finished,
                startMinute = startMinute,
                activeSessionStartMinute = null,
                pauseStartedMinute = null,
                workedMinutes = workedMinutes,
                breakMinutes = breakMinutes,
                lastBreakMinutes = breakMinutes.takeIf { it > 0 },
                weeklyWorkedBeforeTodayMinutes = 0,
                events = events,
                config = history.defaultConfig
            ))
        }
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
        title: String
    ) {
        repository.update { history ->
            history.updateDay(date, kind, history.defaultConfig.dailyTargetMinutes, title)
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
            config = defaultConfig,
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
        const val DefaultBreakMinutes = 30
        const val WorkDayStartMinute = 8 * 60 + 30
        const val WorkDayBreakStartMinute = 12 * 60
        const val WorkDayBreakEndMinute = 12 * 60 + 30
        const val WorkBeforeBreakMinutes = WorkDayBreakStartMinute - WorkDayStartMinute
        const val MaxManualDayMinutes = 24 * 60
        const val MinutesPerDay = 24 * 60
    }
}
