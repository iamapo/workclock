package com.iamapo.timetracker.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GermanHolidayCalendarTest {
    @Test
    fun calculatesFixedAndMovableHolidays() {
        val berlin = GermanFederalState.Berlin

        assertEquals(
            GermanHolidayKind.GoodFriday,
            GermanHolidayCalendar.holiday(LocalDate(2026, 4, 3), berlin)?.kind
        )
        assertEquals(
            GermanHolidayKind.AscensionDay,
            GermanHolidayCalendar.holiday(LocalDate(2026, 5, 14), berlin)?.kind
        )
        assertEquals(
            GermanHolidayKind.GermanUnityDay,
            GermanHolidayCalendar.holiday(LocalDate(2026, 10, 3), berlin)?.kind
        )
    }

    @Test
    fun appliesFederalStateRulesAndTheirIntroductionYear() {
        assertNotNull(
            GermanHolidayCalendar.holiday(
                LocalDate(2026, 1, 6),
                GermanFederalState.Bavaria
            )
        )
        assertNull(
            GermanHolidayCalendar.holiday(
                LocalDate(2026, 1, 6),
                GermanFederalState.Berlin
            )
        )
        assertNull(
            GermanHolidayCalendar.holiday(
                LocalDate(2018, 3, 8),
                GermanFederalState.Berlin
            )
        )
        assertNotNull(
            GermanHolidayCalendar.holiday(
                LocalDate(2019, 3, 8),
                GermanFederalState.Berlin
            )
        )
    }

    @Test
    fun publicHolidayReducesWeeklyTargetWithoutCreatingWorkedTime() {
        val history = WorkHistory(
            automaticHolidaysEnabled = true,
            holidayFederalState = GermanFederalState.Berlin
        )

        assertEquals(0, history.targetMinutes(LocalDate(2026, 4, 3)))
        assertEquals(32 * 60, history.targetMinutesForWeek(LocalDate(2026, 4, 3)))
        assertEquals(0, history.dayFor(LocalDate(2026, 4, 3)).workedMinutes)
    }

    @Test
    fun recordedWorkdayKeepsPublicHolidayTargetAtZero() {
        val holiday = LocalDate(2026, 4, 3)
        val history = WorkHistory(
            automaticHolidaysEnabled = true,
            holidayFederalState = GermanFederalState.Berlin,
            days = mapOf(holiday to WorkDay(kind = WorkDayKind.Work))
        )

        assertEquals(0, history.targetMinutes(holiday))
    }
}
