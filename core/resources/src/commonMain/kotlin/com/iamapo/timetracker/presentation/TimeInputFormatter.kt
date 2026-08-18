package com.iamapo.timetracker.presentation

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

    private const val HourDigits = 2
    private const val MaxDigits = 4
    private const val LastHour = 23
}
