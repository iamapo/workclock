package com.iamapo.timetracker.watch

import kotlin.test.Test
import kotlin.test.assertEquals

class DeferredWatchEventHandlerTest {
    @Test
    fun eventReceivedBeforeAppHandlerIsReadyIsDeliveredAfterAttach() {
        val accepted = mutableListOf<String>()
        val acknowledged = mutableListOf<String>()
        val handler = DeferredWatchEventHandler<String>()

        handler.submit("startDay") { acknowledged += it }
        assertEquals(emptyList(), accepted)
        assertEquals(emptyList(), acknowledged)

        handler.attach { event ->
            accepted += event
            true
        }

        assertEquals(listOf("startDay"), accepted)
        assertEquals(listOf("startDay"), acknowledged)
    }

    @Test
    fun rejectedEventIsNotAcknowledgedAndCanBeRetried() {
        val acknowledged = mutableListOf<String>()
        val handler = DeferredWatchEventHandler<String>()
        handler.attach { false }

        handler.submit("startDay") { acknowledged += it }
        assertEquals(emptyList(), acknowledged)

        handler.attach { true }
        assertEquals(listOf("startDay"), acknowledged)
    }
}
