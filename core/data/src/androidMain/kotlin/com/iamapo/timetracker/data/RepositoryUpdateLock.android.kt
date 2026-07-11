package com.iamapo.timetracker.data

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal actual class RepositoryUpdateLock {
    private val lock = ReentrantLock()

    actual fun <T> withLock(block: () -> T): T = lock.withLock(block)
}
