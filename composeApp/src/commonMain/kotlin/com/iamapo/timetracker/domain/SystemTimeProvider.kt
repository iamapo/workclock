package com.iamapo.timetracker.domain

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class SystemTimeProvider : TimeProvider {
    override fun now(): TimeSnapshot {
        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return TimeSnapshot(
            date = localDateTime.date,
            minuteOfDay = localDateTime.hour * 60 + localDateTime.minute
        )
    }
}
