package com.iamapo.timetracker.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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

    @Test
    fun typingKeepsTheCursorBehindTheNewDigit() {
        assertEquals(TextFieldValue("08:3", TextRange(4)), type("083", cursor = 3))
        assertEquals(TextFieldValue("08:30", TextRange(5)), type("830", cursor = 3))
    }

    @Test
    fun editingTheMiddleKeepsTheCursorBehindTheSameDigit() {
        assertEquals(TextFieldValue("08:53", TextRange(4)), type("08:530", cursor = 4))
        assertEquals(TextFieldValue("05:83", TextRange(2)), type("058:30", cursor = 2))
    }

    @Test
    fun deletingBehindTheSeparatorMovesTheCursorInFrontOfIt() {
        assertEquals(TextFieldValue("08:30", TextRange(2)), type("0830", cursor = 2))
    }

    @Test
    fun movingTheCursorDoesNotChangeTheSelection() {
        val selected = TextFieldValue("08:30", TextRange(3, 5))

        assertEquals(selected, TimeInputFormatter.normalize(selected))
    }

    private fun type(text: String, cursor: Int): TextFieldValue =
        TimeInputFormatter.normalize(TextFieldValue(text, TextRange(cursor)))
}
