package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkDayConfig
import com.iamapo.timetracker.domain.WorkEvent
import com.iamapo.timetracker.domain.WorkEventKind
import kotlinx.datetime.LocalDate

object WorkHistorySerializer {
    private const val Version = "1"
    private const val HistoryVersion = "1"
    private const val NullValue = "-"

    fun encodeHistory(history: WorkHistory): String = buildString {
        appendLine("historyVersion=$HistoryVersion")
        appendLine("defaultDailyTargetMinutes=${history.defaultConfig.dailyTargetMinutes}")
        appendLine("defaultRequiredBreakMinutes=${history.defaultConfig.requiredBreakMinutes}")
        appendLine("defaultWeeklyTargetMinutes=${history.defaultConfig.weeklyTargetMinutes}")
        appendLine("lockScreenStatusEnabled=${history.lockScreenStatusEnabled}")
        history.days.entries
            .sortedBy { (date, _) -> date.toString() }
            .forEach { (date, day) ->
                appendLine("day=$date|${escape(encode(day))}")
            }
    }

    fun decodeHistory(raw: String): WorkHistory? = runCatching {
        var hasSupportedVersion = false
        val values = mutableMapOf<String, String>()
        val days = mutableMapOf<LocalDate, WorkDay>()

        raw.lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                val separator = line.indexOf('=')
                require(separator > 0)

                val key = line.substring(0, separator)
                val value = line.substring(separator + 1)

                when (key) {
                    "historyVersion" -> hasSupportedVersion = value == HistoryVersion
                    "day" -> {
                        val daySeparator = value.indexOf('|')
                        require(daySeparator > 0)

                        val date = LocalDate.parse(value.substring(0, daySeparator))
                        val day = decode(unescape(value.substring(daySeparator + 1)))
                        require(day != null)

                        days[date] = day
                    }
                    else -> values[key] = value
                }
            }

        require(hasSupportedVersion)
        WorkHistory(
            defaultConfig = WorkDayConfig(
                dailyTargetMinutes = values["defaultDailyTargetMinutes"]?.toInt() ?: WorkDayConfig().dailyTargetMinutes,
                requiredBreakMinutes = values["defaultRequiredBreakMinutes"]?.toInt() ?: WorkDayConfig().requiredBreakMinutes,
                weeklyTargetMinutes = values["defaultWeeklyTargetMinutes"]?.toInt() ?: WorkDayConfig().weeklyTargetMinutes
            ),
            lockScreenStatusEnabled = values["lockScreenStatusEnabled"]?.toBooleanStrictOrNull() ?: false,
            days = days
        )
    }.getOrNull()

    fun encode(day: WorkDay): String = buildString {
        appendLine("version=$Version")
        appendLine("kind=${day.kind.name}")
        appendLine("status=${day.status.name}")
        appendLine("startMinute=${day.startMinute.encodeNullableInt()}")
        appendLine("activeSessionStartMinute=${day.activeSessionStartMinute.encodeNullableInt()}")
        appendLine("pauseStartedMinute=${day.pauseStartedMinute.encodeNullableInt()}")
        appendLine("workedMinutes=${day.workedMinutes}")
        appendLine("breakMinutes=${day.breakMinutes}")
        appendLine("lastBreakMinutes=${day.lastBreakMinutes.encodeNullableInt()}")
        appendLine("weeklyWorkedBeforeTodayMinutes=${day.weeklyWorkedBeforeTodayMinutes}")
        appendLine("weeklyBalanceCarryMinutes=${day.weeklyBalanceCarryMinutes}")
        appendLine("dailyTargetMinutes=${day.config.dailyTargetMinutes}")
        appendLine("requiredBreakMinutes=${day.config.requiredBreakMinutes}")
        appendLine("weeklyTargetMinutes=${day.config.weeklyTargetMinutes}")
        day.events.forEach { event ->
            appendLine("event=${event.minuteOfDay}|${event.kind.name}|${escape(event.title)}")
        }
    }

    fun decode(raw: String): WorkDay? = runCatching {
        val values = mutableMapOf<String, String>()
        val events = mutableListOf<WorkEvent>()

        raw.lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                val separator = line.indexOf('=')
                require(separator > 0)

                val key = line.substring(0, separator)
                val value = line.substring(separator + 1)

                if (key == "event") {
                    events += decodeEvent(value)
                } else {
                    values[key] = value
                }
            }

        require(values["version"] == Version)

        WorkDay(
            kind = values["kind"]?.let { enumValueOf(it) } ?: WorkDay().kind,
            status = enumValueOf(values.getValue("status")),
            startMinute = values.getValue("startMinute").decodeNullableInt(),
            activeSessionStartMinute = values.getValue("activeSessionStartMinute").decodeNullableInt(),
            pauseStartedMinute = values.getValue("pauseStartedMinute").decodeNullableInt(),
            workedMinutes = values.getValue("workedMinutes").toInt(),
            breakMinutes = values.getValue("breakMinutes").toInt(),
            lastBreakMinutes = values.getValue("lastBreakMinutes").decodeNullableInt(),
            weeklyWorkedBeforeTodayMinutes = values.getValue("weeklyWorkedBeforeTodayMinutes").toInt(),
            weeklyBalanceCarryMinutes = values["weeklyBalanceCarryMinutes"]?.toInt() ?: 0,
            events = events,
            config = WorkDayConfig(
                dailyTargetMinutes = values.getValue("dailyTargetMinutes").toInt(),
                requiredBreakMinutes = values.getValue("requiredBreakMinutes").toInt(),
                weeklyTargetMinutes = values.getValue("weeklyTargetMinutes").toInt()
            )
        )
    }.getOrNull()

    private fun decodeEvent(value: String): WorkEvent {
        val parts = value.split('|', limit = 3)
        require(parts.size == 3)

        return WorkEvent(
            minuteOfDay = parts[0].toInt(),
            title = unescape(parts[2]),
            kind = enumValueOf<WorkEventKind>(parts[1])
        )
    }

    private fun Int?.encodeNullableInt(): String = this?.toString() ?: NullValue

    private fun String.decodeNullableInt(): Int? = if (this == NullValue) null else toInt()

    private fun escape(value: String): String = buildString {
        value.forEach { char ->
            when (char) {
                '%' -> append("%25")
                '|' -> append("%7C")
                '\n' -> append("%0A")
                '\r' -> append("%0D")
                else -> append(char)
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            if (value[index] == '%' && index + 2 < value.length) {
                val hex = value.substring(index + 1, index + 3)
                val decoded = hex.toIntOrNull(16)?.toChar()
                if (decoded != null) {
                    append(decoded)
                    index += 3
                } else {
                    append(value[index])
                    index += 1
                }
            } else {
                append(value[index])
                index += 1
            }
        }
    }
}
