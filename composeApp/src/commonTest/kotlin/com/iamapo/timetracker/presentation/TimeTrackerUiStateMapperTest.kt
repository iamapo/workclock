package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTrackerUiStateMapperTest {
    @Test
    fun previewDayCalculatesRemainingTimeEndTimeAndWeekTotal() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay.preview(),
            snapshot = TimeTrackerPreviewData.snapshot
        )

        assertEquals("2 h 48 min", state.remainingTime)
        assertEquals("17:21 Uhr", state.endTime)
        assertEquals("21:40 h", state.reachedWeek)
    }

    @Test
    fun calendarShowsVacationAndSickDays() {
        val vacationDate = LocalDate(2026, 7, 6)
        val sickDate = LocalDate(2026, 7, 8)
        val history = WorkHistory(
            days = mapOf(
                vacationDate to WorkDay(
                    kind = WorkDayKind.Vacation,
                    status = WorkStatus.Finished,
                    workedMinutes = 8 * 60
                ),
                sickDate to WorkDay(
                    kind = WorkDayKind.Sick,
                    status = WorkStatus.Finished,
                    workedMinutes = 8 * 60
                )
            )
        )

        val state = TimeTrackerUiStateMapper.map(
            day = history.dayWithWeeklySummary(TimeTrackerPreviewData.snapshot.date),
            snapshot = TimeTrackerPreviewData.snapshot,
            history = history.days
        )

        val vacationDay = state.calendarDays.first { it.date == vacationDate }
        val sickDay = state.calendarDays.first { it.date == sickDate }
        assertEquals(CalendarDayStyle.Vacation, vacationDay.style)
        assertEquals("Urlaub 8:00", vacationDay.note)
        assertEquals(CalendarDayStyle.Sick, sickDay.style)
        assertEquals("Krank 8:00", sickDay.note)
    }

    @Test
    fun settingsAndPlannedCalendarUseConfiguredTargets() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                config = WorkDayConfig(
                    dailyTargetMinutes = 7 * 60 + 30,
                    weeklyTargetMinutes = 37 * 60 + 30
                )
            ),
            snapshot = TimeTrackerPreviewData.snapshot
        )

        val plannedDay = state.calendarDays.first { it.date == LocalDate(2026, 7, 8) }
        assertEquals("7:30 h", state.settings.dailyTarget)
        assertEquals("37:30 h", state.settings.weeklyTarget)
        assertEquals("7:30", plannedDay.note)
    }

    @Test
    fun finishedDayShowsActualEndTimeWithoutPlannedTimelineEntry() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 8 * 60,
                breakMinutes = 30,
                events = listOf(
                    WorkEvent(8 * 60 + 30, "Arbeitsbeginn", WorkEventKind.Work),
                    WorkEvent(12 * 60, "Pause gestartet", WorkEventKind.Break),
                    WorkEvent(12 * 60 + 30, "Weitergearbeitet", WorkEventKind.Work),
                    WorkEvent(17 * 60, "Arbeitstag beendet", WorkEventKind.Target)
                )
            ),
            snapshot = TimeTrackerPreviewData.snapshot.copy(minuteOfDay = 20 * 60)
        )

        assertEquals("17:00 Uhr", state.endTime)
        assertEquals("Arbeitstag beendet", state.timeline.last().title)
        assertEquals("17:00", state.timeline.last().time)
        assertEquals(false, state.timeline.any { it.title == "Geplanter Feierabend" })
    }
}
