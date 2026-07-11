package com.iamapo.timetracker.data

/** Serializes repository updates, including their persistent write. */
internal expect class RepositoryUpdateLock() {
    fun <T> withLock(block: () -> T): T
}
