package com.iamapo.timetracker.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeInputFormatterTest {
    @Test
    fun numericClockInputRestoresTheSeparator() {
        assertEquals("08:30", TimeInputFormatter.normalize("0830"))
        assertEquals("08:30", TimeInputFormatter.normalize("08:30"))
    }

    @Test
    fun threeDigitsAreUnderstoodAsSingleDigitHour() {
        assertEquals("08:30", TimeInputFormatter.normalize("830"))
    }
}
