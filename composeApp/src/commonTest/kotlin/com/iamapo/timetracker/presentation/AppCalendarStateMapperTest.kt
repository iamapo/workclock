package com.iamapo.timetracker.presentation

import com.iamapo.timetracker.domain.GermanFederalState
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AppCalendarStateMapperTest {
    @Test
    fun showsRecordedWorkFromPreviousMonthInCurrentMonthGrid() {
        val previousMonthDate = LocalDate(2026, 7, 31)
        val state = AppCalendarStateMapper.map(
            history = WorkHistory(
                days = mapOf(previousMonthDate to WorkDay(workedMinutes = 8 * 60))
            ),
            snapshot = TimeSnapshot(LocalDate(2026, 8, 3), 9 * 60),
            displayedMonth = LocalDate(2026, 8, 1)
        )

        val previousMonthDay = state.days.first { day -> day.date == previousMonthDate }
        assertEquals(CalendarDayStyle.Muted, previousMonthDay.style)
        assertEquals("8:00", previousMonthDay.note)
    }

    @Test
    fun showsHolidayAndReducedWeeklyTarget() {
        val date = LocalDate(2026, 4, 3)
        val state = AppCalendarStateMapper.map(
            history = WorkHistory(
                automaticHolidaysEnabled = true,
                holidayFederalState = GermanFederalState.Berlin
            ),
            snapshot = TimeSnapshot(date, 9 * 60)
        )

        val holiday = state.days.first { day -> day.date == date }
        assertEquals(CalendarDayStyle.Holiday, holiday.style)
        assertEquals("Karfreitag", holiday.note)
        assertEquals("32:00 h", state.plannedWeek)
    }
}
