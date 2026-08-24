package com.iamapo.timetracker.watch

internal class DeferredWatchEventHandler<T> {
    private data class Pending<T>(
        val event: T,
        val onAccepted: (T) -> Unit
    )

    private val pending = mutableListOf<Pending<T>>()
    private var handler: ((T) -> Boolean)? = null

    fun submit(event: T, onAccepted: (T) -> Unit) {
        if (pending.none { it.event == event }) {
            pending += Pending(event, onAccepted)
        }
        drain()
    }

    fun attach(handler: (T) -> Boolean) {
        this.handler = handler
        drain()
    }

    private fun drain() {
        val activeHandler = handler ?: return
        var index = 0
        while (index < pending.size) {
            val entry = pending[index]
            if (activeHandler(entry.event)) {
                pending.removeAt(index)
                entry.onAccepted(entry.event)
            } else {
                index += 1
            }
        }
    }
}
