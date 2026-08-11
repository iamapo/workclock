package com.iamapo.timetracker.data.sync

import com.iamapo.timetracker.data.WorkHistorySerializer
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.sync.CloudReplica
import com.iamapo.timetracker.domain.sync.DayRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

interface DaySyncStore {
    fun loadSyncState(): String?
    fun saveSyncState(state: String)
}

/**
 * Keeps the work days of all devices of one iCloud account equal.
 *
 * Every repository change passes through here, so edits, imports and deletions need no extra
 * code. If two devices change the same day, the later change wins for that whole day.
 */
class WorkDaySync(
    private val repository: WorkHistoryRepository,
    private val store: DaySyncStore,
    private val replica: CloudReplica,
    private val timeProvider: TimeProvider
) {
    private val mutex = Mutex()
    private val syncedDayMap: MutableMap<String, SyncedDay> =
        SyncedDay.decodeMap(store.loadSyncState()).toMutableMap()

    /** Syncs on start and after every local change until cancelled. */
    suspend fun run() {
        repository.history.collect {
            sync()
        }
    }

    suspend fun sync(): Boolean = mutex.withLock {
        try {
            stampLocalChanges()
            val records = replica.fetchAll()
            // Changes made during the download must beat older remote data.
            stampLocalChanges()
            applyRemoteRecords(records)
            uploadPendingDays()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }

    private fun stampLocalChanges() {
        val now = timeProvider.now().epochMillis
        val localPayloadMap = repository.history.value.days.entries.associate { (date, day) ->
            date.toString() to WorkHistorySerializer.encode(day)
        }

        var changed = false
        (localPayloadMap.keys + syncedDayMap.keys).forEach { date ->
            val localPayload = localPayloadMap[date]
            val synced = syncedDayMap[date]
            if (synced == null || synced.payload != localPayload) {
                // Staying above the old stamp keeps edits alive on a device with a late clock.
                val updatedAt = maxOf(now, (synced?.updatedAtEpochMillis ?: 0L) + 1)
                syncedDayMap[date] = SyncedDay(localPayload, updatedAt, pending = true)
                changed = true
            }
        }
        if (changed) saveState()
    }

    private fun applyRemoteRecords(records: List<DayRecord>) {
        val newerRecords = records.filter { record ->
            record.updatedAtEpochMillis > (syncedDayMap[record.date]?.updatedAtEpochMillis ?: -1L)
        }
        val remoteDayMap = newerRecords.mapNotNull(::decodeRecord).toMap()
        if (remoteDayMap.isEmpty()) return

        repository.update { history ->
            remoteDayMap.entries.fold(history) { updated, (date, remoteDay) ->
                if (remoteDay.day == null) {
                    updated.withoutDay(date)
                } else {
                    updated.withDay(date, remoteDay.day)
                }
            }
        }
        remoteDayMap.forEach { (date, remoteDay) ->
            syncedDayMap[date.toString()] = SyncedDay(
                payload = remoteDay.day?.let(WorkHistorySerializer::encode),
                updatedAtEpochMillis = remoteDay.updatedAtEpochMillis,
                pending = false
            )
        }
        saveState()
    }

    /** Skips records this app version cannot read, so they can never damage local data. */
    private fun decodeRecord(record: DayRecord): Pair<LocalDate, RemoteDay>? {
        val date = runCatching {
            LocalDate.parse(record.date)
        }.getOrNull() ?: return null
        val payload = record.payload ?: return date to RemoteDay(null, record.updatedAtEpochMillis)
        val day = WorkHistorySerializer.decode(payload) ?: return null
        return date to RemoteDay(day, record.updatedAtEpochMillis)
    }

    private suspend fun uploadPendingDays(): Boolean {
        val pendingRecords = syncedDayMap
            .filterValues(SyncedDay::pending)
            .map { (date, synced) ->
                DayRecord(date, synced.payload, synced.updatedAtEpochMillis)
            }
        if (pendingRecords.isEmpty()) return true

        val uploadedDates = replica.upload(pendingRecords)
        uploadedDates.forEach { date ->
            syncedDayMap[date]?.let { synced ->
                syncedDayMap[date] = synced.copy(pending = false)
            }
        }
        saveState()
        return uploadedDates.size == pendingRecords.size
    }

    private fun saveState() {
        store.saveSyncState(SyncedDay.encodeMap(syncedDayMap))
    }

    private data class RemoteDay(
        val day: WorkDay?,
        val updatedAtEpochMillis: Long
    )
}

/** The last state of a day known to be in sync, or waiting for upload if [pending]. */
private data class SyncedDay(
    val payload: String?,
    val updatedAtEpochMillis: Long,
    val pending: Boolean
) {
    companion object {
        private const val DELETED = "-"
        private const val FIELD_COUNT = 4

        fun encodeMap(syncedDayMap: Map<String, SyncedDay>): String =
            syncedDayMap.entries.joinToString("\n") { (date, synced) ->
                val payload = synced.payload?.let(WorkHistorySerializer::escape) ?: DELETED
                "$date|${synced.updatedAtEpochMillis}|${synced.pending}|$payload"
            }

        fun decodeMap(raw: String?): Map<String, SyncedDay> =
            raw.orEmpty().lineSequence()
                .map { line ->
                    line.split('|', limit = FIELD_COUNT)
                }
                .filter { fields ->
                    fields.size == FIELD_COUNT
                }
                .associate { (date, updatedAt, pending, payload) ->
                    date to SyncedDay(
                        payload = payload
                            .takeUnless { it == DELETED }
                            ?.let(WorkHistorySerializer::unescape),
                        updatedAtEpochMillis = updatedAt.toLong(),
                        pending = pending.toBoolean()
                    )
                }
    }
}
