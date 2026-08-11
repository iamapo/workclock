package com.iamapo.timetracker.data

import platform.Foundation.NSRecursiveLock

internal actual class RepositoryUpdateLock {
    private val lock = NSRecursiveLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        return try {
            block()
        } finally {
            lock.unlock()
        }
    }
}
