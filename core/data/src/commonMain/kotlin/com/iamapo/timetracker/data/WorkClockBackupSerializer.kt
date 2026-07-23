package com.iamapo.timetracker.data

import com.iamapo.timetracker.domain.WorkHistory

data class WorkClockBackup(
    val history: WorkHistory,
    val createdAtEpochMillis: Long
)

object WorkClockBackupSerializer {
    const val FileExtension = "workclock"
    const val MaxBackupBytes = 5 * 1024 * 1024

    private const val Magic = "WORKCLOCK_BACKUP"
    private const val BackupVersion = "1"
    private const val ChecksumPrefix = "fnv1a64:"
    private const val HeaderSeparator = "\n\n"

    fun encode(
        history: WorkHistory,
        createdAtEpochMillis: Long
    ): String {
        val payload = WorkHistorySerializer.encodeHistory(history)
        val payloadBytes = payload.encodeToByteArray()

        return buildString {
            appendLine(Magic)
            appendLine("backupVersion=$BackupVersion")
            appendLine("createdAtEpochMillis=$createdAtEpochMillis")
            appendLine("payloadBytes=${payloadBytes.size}")
            appendLine("checksum=$ChecksumPrefix${checksum(payloadBytes)}")
            appendLine()
            append(payload)
        }
    }

    fun decode(raw: String): WorkClockBackup? = runCatching {
        val backupBytes = raw.encodeToByteArray()
        require(backupBytes.size <= MaxBackupBytes)

        val headerEnd = raw.indexOf(HeaderSeparator)
        require(headerEnd > 0)

        val headerLines = raw.substring(0, headerEnd).lineSequence().toList()
        require(headerLines.firstOrNull() == Magic)

        val header = headerLines.drop(1).associate { line ->
            val separator = line.indexOf('=')
            require(separator > 0)
            line.substring(0, separator) to line.substring(separator + 1)
        }

        require(header["backupVersion"] == BackupVersion)
        val createdAtEpochMillis = header.getValue("createdAtEpochMillis").toLong()
        require(createdAtEpochMillis >= 0)

        val payload = raw.substring(headerEnd + HeaderSeparator.length)
        val payloadBytes = payload.encodeToByteArray()
        require(payloadBytes.size == header.getValue("payloadBytes").toInt())
        require(header.getValue("checksum") == ChecksumPrefix + checksum(payloadBytes))

        val history = requireNotNull(WorkHistorySerializer.decodeHistory(payload))
        require(history.isValidBackupContent())

        WorkClockBackup(
            history = history,
            createdAtEpochMillis = createdAtEpochMillis
        )
    }.getOrNull()

    private fun WorkHistory.isValidBackupContent(): Boolean {
        if (days.size > MaxDays) return false
        if (!defaultConfig.isValid()) return false
        if (!workSchedule.isValid()) return false

        return days.values.all { day ->
            day.config.isValid() &&
                day.startMinute.isValidMinute() &&
                day.activeSessionStartMinute.isValidMinute() &&
                day.pauseStartedMinute.isValidMinute() &&
                day.lastBreakMinutes.isValidDuration() &&
                day.workedMinutes in 0..MinutesPerDay &&
                day.breakMinutes in 0..MinutesPerDay &&
                day.events.size <= MaxEventsPerDay &&
                day.events.all { event ->
                    event.minuteOfDay in 0 until MinutesPerDay &&
                        event.title.length <= MaxEventTitleLength
                }
        }
    }

    private fun com.iamapo.timetracker.domain.WorkDayConfig.isValid(): Boolean =
        dailyTargetMinutes in 0..MinutesPerDay &&
            requiredBreakMinutes in 0..MinutesPerDay &&
            weeklyTargetMinutes in 0..MinutesPerWeek

    private fun com.iamapo.timetracker.domain.WorkSchedule.isValid(): Boolean =
        listOf(
            mondayMinutes,
            tuesdayMinutes,
            wednesdayMinutes,
            thursdayMinutes,
            fridayMinutes,
            saturdayMinutes,
            sundayMinutes
        ).all { minutes -> minutes in 0..MinutesPerDay }

    private fun Int?.isValidMinute(): Boolean = this == null || this in 0 until MinutesPerDay

    private fun Int?.isValidDuration(): Boolean = this == null || this in 0..MinutesPerDay

    private fun checksum(bytes: ByteArray): String {
        var hash = FnvOffsetBasis
        bytes.forEach { byte ->
            hash = hash xor byte.toUByte().toULong()
            hash *= FnvPrime
        }
        return hash.toString(radix = 16).padStart(16, '0')
    }

    private const val MinutesPerDay = 24 * 60
    private const val MinutesPerWeek = 7 * MinutesPerDay
    private const val MaxDays = 366 * 100
    private const val MaxEventsPerDay = 1_000
    private const val MaxEventTitleLength = 1_000
    private val FnvOffsetBasis = 14_695_981_039_346_656_037uL
    private val FnvPrime = 1_099_511_628_211uL
}
