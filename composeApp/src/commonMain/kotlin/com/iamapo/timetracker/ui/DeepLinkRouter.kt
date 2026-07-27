package com.iamapo.timetracker.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lets platform code (e.g. tapping the Live Activity) request that the app
 * switch to the TimeTracker tab, regardless of which tab was last shown.
 */
object DeepLinkRouter {
    private val _requestedTabEvent = MutableStateFlow(0L)
    val requestedTabEvent: StateFlow<Long> = _requestedTabEvent.asStateFlow()

    fun requestTimeTrackerTab() {
        _requestedTabEvent.value += 1
    }
}
