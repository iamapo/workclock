package com.iamapo.timetracker.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object TimeInputFormatter {
    fun normalize(value: String): String {
        val digits = value.filter(Char::isDigit).take(MaxDigits)
        return when {
            digits.length <= HourDigits -> digits
            digits.length == 3 && digits.take(HourDigits).toInt() > LastHour ->
                "0${digits.first()}:${digits.drop(1)}"
            else -> digits.take(HourDigits) + ":" + digits.drop(HourDigits)
        }
    }

    /** Normalizes the text and keeps the cursor behind the same digit. */
    fun normalize(value: TextFieldValue): TextFieldValue {
        val text = normalize(value.text)
        if (text == value.text) return value

        val droppedDigits = (value.text.count(Char::isDigit) - MaxDigits).coerceAtLeast(0)
        val digitsAfterCursor = value.text.drop(value.selection.end).count(Char::isDigit) - droppedDigits
        val cursor = text.length - digitsAfterCursor.coerceAtLeast(0)
        // Directly behind the separator, backspace would only bring the separator back.
        val position = if (':' in text && cursor <= HourDigits + 1) cursor - 1 else cursor
        return TextFieldValue(text, TextRange(position))
    }

    private const val HourDigits = 2
    private const val MaxDigits = 4
    private const val LastHour = 23
}
