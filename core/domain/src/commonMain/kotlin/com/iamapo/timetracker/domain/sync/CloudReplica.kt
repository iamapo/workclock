package com.iamapo.timetracker.domain.sync

/** One work day as stored in the cloud. A null [payload] marks the day as deleted. */
data class DayRecord(
    val date: String,
    val payload: String?,
    val updatedAtEpochMillis: Long
)

interface CloudReplica {
    /** Returns the dates that were stored successfully. */
    suspend fun upload(records: List<DayRecord>): Set<String>

    suspend fun fetchAll(): List<DayRecord>
}
