package com.iamapo.timetracker.data.sync

import com.iamapo.timetracker.data.NoOpWorkDayStore
import com.iamapo.timetracker.data.PersistedWorkHistoryRepository
import com.iamapo.timetracker.data.WorkHistorySerializer
import com.iamapo.timetracker.domain.TimeSnapshot
import com.iamapo.timetracker.domain.WorkDay
import com.iamapo.timetracker.domain.WorkStatus
import com.iamapo.timetracker.domain.sync.CloudReplica
import com.iamapo.timetracker.domain.sync.DayRecord
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkDaySyncTest {
    private val date = LocalDate(2026, 11, 5)
    private val workingDay = WorkDay(status = WorkStatus.Working, startMinute = 480)
    private val pausedDay = WorkDay(status = WorkStatus.Paused, startMinute = 480, workedMinutes = 60)

    @Test
    fun startOnOneDeviceAppearsOnTheOther() = runTest {
        val cloud = FakeCloud()
        val iphone = Device(cloud)
        val mac = Device(cloud)

        iphone.repository.update { it.withDay(date, workingDay) }
        assertTrue(iphone.sync.sync())
        assertTrue(mac.sync.sync())

        assertEquals(WorkStatus.Working, mac.repository.history.value.days[date]?.status)
    }

    @Test
    fun editsTravelInBothDirections() = runTest {
        val cloud = FakeCloud()
        val iphone = Device(cloud)
        val mac = Device(cloud)
        iphone.repository.update { it.withDay(date, workingDay) }
        iphone.sync.sync()
        mac.sync.sync()

        mac.clock.millis = 2_000
        mac.repository.update { it.withDay(date, pausedDay) }
        mac.sync.sync()
        iphone.sync.sync()

        assertEquals(pausedDay, iphone.repository.history.value.days[date])
    }

    @Test
    fun laterChangeWinsWhenBothDevicesEditTheSameDay() = runTest {
        val cloud = FakeCloud()
        val iphone = Device(cloud)
        val mac = Device(cloud)

        iphone.clock.millis = 1_000
        iphone.repository.update { it.withDay(date, workingDay) }
        mac.clock.millis = 2_000
        mac.repository.update { it.withDay(date, pausedDay) }
        mac.sync.sync()
        iphone.sync.sync()
        mac.sync.sync()

        assertEquals(WorkStatus.Paused, iphone.repository.history.value.days[date]?.status)
        assertEquals(WorkStatus.Paused, mac.repository.history.value.days[date]?.status)
    }

    @Test
    fun deletionReachesTheOtherDevice() = runTest {
        val cloud = FakeCloud()
        val iphone = Device(cloud)
        val mac = Device(cloud)
        iphone.repository.update { it.withDay(date, workingDay) }
        iphone.sync.sync()
        mac.sync.sync()

        iphone.clock.millis = 2_000
        iphone.repository.update { it.withoutDay(date) }
        iphone.sync.sync()
        mac.sync.sync()

        assertNull(mac.repository.history.value.days[date])
    }

    @Test
    fun appliedRemoteChangeIsNotUploadedAgain() = runTest {
        val cloud = FakeCloud()
        val iphone = Device(cloud)
        val mac = Device(cloud)
        iphone.repository.update { it.withDay(date, workingDay) }
        iphone.sync.sync()

        mac.sync.sync()
        mac.sync.sync()

        assertEquals(1, cloud.uploadCount)
    }

    @Test
    fun syncStateSurvivesRestart() = runTest {
        val cloud = FakeCloud()
        val device = Device(cloud)
        device.repository.update { it.withDay(date, workingDay) }
        device.sync.sync()

        val restarted = WorkDaySync(device.repository, device.store, cloud, device.clock)
        restarted.sync()

        assertEquals(1, cloud.uploadCount)
    }

    @Test
    fun offlineChangeIsUploadedLater() = runTest {
        val cloud = FakeCloud()
        val device = Device(cloud)
        device.repository.update { it.withDay(date, workingDay) }

        cloud.isOffline = true
        assertFalse(device.sync.sync())
        cloud.isOffline = false
        assertTrue(device.sync.sync())

        assertTrue(date.toString() in cloud.recordMap)
    }

    @Test
    fun unreadableRemoteRecordKeepsLocalDay() = runTest {
        val cloud = FakeCloud()
        val device = Device(cloud)
        device.repository.update { it.withDay(date, workingDay) }
        device.sync.sync()
        cloud.recordMap[date.toString()] = DayRecord(date.toString(), "broken", Long.MAX_VALUE)

        device.sync.sync()

        assertEquals(workingDay, device.repository.history.value.days[date])
    }

    @Test
    fun payloadWithSpecialCharactersSurvivesRestart() = runTest {
        val cloud = FakeCloud()
        val device = Device(cloud)
        val payload = WorkHistorySerializer.encode(workingDay)
        cloud.recordMap[date.toString()] = DayRecord(date.toString(), payload, 1_000)
        device.sync.sync()

        WorkDaySync(device.repository, device.store, cloud, device.clock).sync()

        assertEquals(0, cloud.uploadCount)
    }

    private class Device(cloud: FakeCloud) {
        val clock = FakeClock()
        val store = MemorySyncStore()
        val repository = PersistedWorkHistoryRepository(NoOpWorkDayStore, LocalDate(2026, 11, 5))
        val sync = WorkDaySync(repository, store, cloud, clock)
    }

    private class FakeClock : com.iamapo.timetracker.domain.TimeProvider {
        var millis = 1_000L

        override fun now() = TimeSnapshot(LocalDate(2026, 11, 5), 600, millis)
    }

    private class MemorySyncStore : DaySyncStore {
        private var state: String? = null

        override fun loadSyncState(): String? = state

        override fun saveSyncState(state: String) {
            this.state = state
        }
    }

    private class FakeCloud : CloudReplica {
        val recordMap = mutableMapOf<String, DayRecord>()
        var isOffline = false
        var uploadCount = 0

        override suspend fun upload(records: List<DayRecord>): Set<String> {
            check(!isOffline)
            uploadCount += records.size
            records.forEach { record ->
                recordMap[record.date] = record
            }
            return records.map(DayRecord::date).toSet()
        }

        override suspend fun fetchAll(): List<DayRecord> {
            check(!isOffline)
            return recordMap.values.toList()
        }
    }
}
