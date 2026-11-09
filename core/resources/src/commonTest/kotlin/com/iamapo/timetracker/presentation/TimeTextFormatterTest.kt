package com.iamapo.timetracker.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTextFormatterTest {
    @Test
    fun signedClockDurationMarksPositiveNegativeAndZero() {
        assertEquals("+5:40 h", TimeTextFormatter.signedClockDuration(340))
        assertEquals("−46:33 h", TimeTextFormatter.signedClockDuration(-2793))
        assertEquals("0:00 h", TimeTextFormatter.signedClockDuration(0))
    }

    @Test
    fun signedClockDurationPadsMinutesUnderTen() {
        assertEquals("+1:05 h", TimeTextFormatter.signedClockDuration(65))
        assertEquals("−0:07 h", TimeTextFormatter.signedClockDuration(-7))
    }
}
