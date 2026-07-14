package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkDayKind
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTrackerUiStateMapperTest {
    @Test
    fun calendarPreviewShowsPreviousAndCurrentWeek() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(),
            snapshot = TimeSnapshot(LocalDate(2026, 7, 13), 9 * 60)
        )

        assertEquals(LocalDate(2026, 7, 6), state.calendarPreviewDays.first().date)
        assertEquals(LocalDate(2026, 7, 19), state.calendarPreviewDays.last().date)
        assertEquals(14, state.calendarPreviewDays.size)
    }

    @Test
    fun previewDayCalculatesRemainingTimeEndTimeAndWeekTotal() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay.preview(),
            snapshot = TimeTrackerPreviewData.snapshot
        )

        assertEquals("2 h 48 min", state.remainingTime)
        assertEquals("17:21 Uhr", state.endTime)
        assertEquals("21:40 h", state.reachedWeek)
        assertEquals("Woche", state.metrics[2].label)
        assertEquals("21h 40m", state.metrics[2].value)
        assertEquals("+5 h 40 min Saldo", state.metrics[2].hint)
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
        assertEquals("", vacationDay.note)
        assertEquals(CalendarDayStyle.Sick, sickDay.style)
        assertEquals("", sickDay.note)
    }

    @Test
    fun weekOverviewBalancesAgainstExpectedWorkUntilToday() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 8 * 60 + 10
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 6),
                minuteOfDay = 18 * 60
            )
        )

        assertEquals("8:10 h", state.weekOverview.reached)
        assertEquals("+10 min", state.weekOverview.balance)
        assertEquals(true, state.weekOverview.isPositiveBalance)
    }

    @Test
    fun weekOverviewIncludesBalanceCarriedFromPreviousWeek() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 7 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 13),
                minuteOfDay = 18 * 60
            )
        )

        assertEquals("7:30 h", state.weekOverview.reached)
        assertEquals("0 min", state.weekOverview.balance)
        assertEquals(true, state.weekOverview.isPositiveBalance)
        assertEquals("0 min", state.weekOverview.carry)
        assertEquals(true, state.weekOverview.isPositiveCarry)
        assertEquals("40:00 h", state.plannedWeek)
    }

    @Test
    fun carryIncludesOvertimeFromCompletedMonday() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 8 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 13),
                minuteOfDay = 18 * 60
            )
        )

        assertEquals("+1 h", state.weekOverview.carry)
        assertEquals(true, state.weekOverview.isPositiveCarry)
    }

    @Test
    fun carryDoesNotIncludeAnOngoingDay() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Working,
                workedMinutes = 4 * 60,
                weeklyWorkedBeforeTodayMinutes = 8 * 60 + 30,
                weeklyBalanceCarryMinutes = 30
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 14),
                minuteOfDay = 12 * 60
            )
        )

        assertEquals("+1 h", state.weekOverview.carry)
        assertEquals(true, state.weekOverview.isPositiveCarry)
    }

    @Test
    fun negativeBalanceCarryKeepsPlannedWeekAndAppearsInCarry() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(weeklyBalanceCarryMinutes = -30),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 13),
                minuteOfDay = 8 * 60
            )
        )

        assertEquals("-30 min", state.weekOverview.carry)
        assertEquals(false, state.weekOverview.isPositiveCarry)
        assertEquals("40:00 h", state.plannedWeek)
    }

    @Test
    fun fridayBalanceBecomesCarryForFollowingWeek() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 8 * 60 + 30,
                weeklyWorkedBeforeTodayMinutes = 32 * 60
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 10),
                minuteOfDay = 18 * 60
            )
        )

        assertEquals("+30 min", state.weekOverview.carry)
        assertEquals("40:00 h", state.plannedWeek)
    }

    @Test
    fun weekOverviewUsesConfiguredWeeklyTarget() {
        val state = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                workedMinutes = 7 * 60 + 30,
                weeklyWorkedBeforeTodayMinutes = 30 * 60,
                config = WorkDayConfig(weeklyTargetMinutes = 37 * 60 + 30)
            ),
            snapshot = TimeSnapshot(
                date = LocalDate(2026, 7, 10),
                minuteOfDay = 18 * 60
            )
        )

        assertEquals("37:30 h", state.weekOverview.reached)
        assertEquals("0 min", state.weekOverview.balance)
    }

    @Test
    fun breakRequirementShowsConfiguredOrLongerActualBreak() {
        val configuredState = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                breakMinutes = 20,
                config = WorkDayConfig(requiredBreakMinutes = 35)
            ),
            snapshot = TimeTrackerPreviewData.snapshot
        )
        val longerBreakState = TimeTrackerUiStateMapper.map(
            day = WorkDay(
                status = WorkStatus.Finished,
                breakMinutes = 45,
                config = WorkDayConfig(requiredBreakMinutes = 30)
            ),
            snapshot = TimeTrackerPreviewData.snapshot
        )

        assertEquals("inkl. 35 min Pause", configuredState.breakRequirementLabel)
        assertEquals("inkl. 45 min Pause", longerBreakState.breakRequirementLabel)
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
