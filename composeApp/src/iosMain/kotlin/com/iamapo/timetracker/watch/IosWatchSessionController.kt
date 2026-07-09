package com.iamapo.timetracker.watch

import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.datetime.LocalDate
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationState
import platform.WatchConnectivity.WCSessionDelegateProtocol
import platform.darwin.NSObject

private const val KeyMessageType = "messageType"
private const val KeyState = "state"
private const val KeyRemaining = "remaining"
private const val KeyCaption = "caption"
private const val KeyPrimaryAction = "primaryAction"
private const val KeySecondaryAction = "secondaryAction"
private const val KeyCommand = "command"
private const val KeyEventId = "eventId"
private const val KeyOccurredAtDate = "occurredAtDate"
private const val KeyOccurredAtMinuteOfDay = "occurredAtMinuteOfDay"
private const val KeyAcknowledgedEventId = "acknowledgedEventId"

private const val MessageTypeWatchEvent = "watchEvent"
private const val MessageTypeWatchEventAck = "watchEventAck"
private const val WatchCommandPrimary = "primary"
private const val WatchCommandStartDay = "startDay"
private const val WatchCommandStartBreak = "startBreak"
private const val WatchCommandResumeWork = "resumeWork"
private const val WatchCommandStartNewDay = "startNewDay"

@OptIn(ExperimentalForeignApi::class)
class IosWatchSessionController(
    private val onCommand: (String) -> Unit,
    private val onEvent: (String, LocalDate, Int) -> Boolean
) : NSObject(), WCSessionDelegateProtocol {
    private val session: WCSession? =
        if (WCSession.isSupported()) WCSession.defaultSession else null
    private val processedEvents = ProcessedWatchEventStore()

    fun activate() {
        session?.delegate = this
        session?.activateSession()
    }

    fun publish(state: TimeTrackerUiState) {
        val payload: Map<Any?, Any> = mapOf(
            KeyState to state.watchState,
            KeyRemaining to state.watchRemaining,
            KeyCaption to state.watchCaption,
            KeyPrimaryAction to state.primaryActionLabel,
            KeySecondaryAction to (state.secondaryActionLabel ?: ""),
            KeyCommand to commandForPrimaryAction(state.primaryActionLabel)
        )
        session?.sendMessage(payload, replyHandler = null, errorHandler = null)
        runCatching {
            session?.updateApplicationContext(payload, error = null)
        }
    }

    @ObjCSignatureOverride
    override fun session(
        session: WCSession,
        didReceiveMessage: Map<Any?, *>
    ) {
        handleIncoming(didReceiveMessage)?.let(::sendEventAck)
    }

    override fun session(
        session: WCSession,
        didReceiveMessage: Map<Any?, *>,
        replyHandler: (Map<Any?, *>?) -> Unit
    ) {
        val eventId = handleIncoming(didReceiveMessage)
        replyHandler(eventId?.let(::ackPayload) ?: emptyMap<Any?, Any>())
    }

    @ObjCSignatureOverride
    override fun session(
        session: WCSession,
        didReceiveUserInfo: Map<Any?, *>
    ) {
        handleIncoming(didReceiveUserInfo)?.let(::sendEventAck)
    }

    override fun session(
        session: WCSession,
        activationDidCompleteWithState: WCSessionActivationState,
        error: NSError?
    ) {
    }

    override fun sessionDidBecomeInactive(session: WCSession) {
    }

    override fun sessionDidDeactivate(session: WCSession) {
        session.activateSession()
    }

    private fun handleIncoming(payload: Map<Any?, *>): String? {
        parseWatchEvent(payload)?.let { event ->
            if (!processedEvents.contains(event.id)) {
                onEvent(event.command, event.date, event.minuteOfDay)
                processedEvents.add(event.id)
            }
            return event.id
        }

        val command = payload[KeyCommand] as? String ?: return null
        onCommand(command)
        return null
    }

    private fun parseWatchEvent(payload: Map<Any?, *>): WatchEvent? {
        if (payload[KeyMessageType] != MessageTypeWatchEvent && payload[KeyEventId] == null) {
            return null
        }

        val id = payload[KeyEventId] as? String ?: return null
        val command = payload[KeyCommand] as? String ?: return null
        val date = (payload[KeyOccurredAtDate] as? String)
            ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
            ?: return null
        val minuteOfDay = payload[KeyOccurredAtMinuteOfDay].asInt() ?: return null

        if (minuteOfDay !in 0 until MinutesPerDay) return null

        return WatchEvent(
            id = id,
            command = command,
            date = date,
            minuteOfDay = minuteOfDay
        )
    }

    private fun sendEventAck(eventId: String) {
        val session = session ?: return
        val payload = ackPayload(eventId)
        session.sendMessage(
            payload,
            replyHandler = null,
            errorHandler = {
                runCatching { session.transferUserInfo(payload) }
            }
        )
        runCatching { session.transferUserInfo(payload) }
    }

    private fun ackPayload(eventId: String): Map<Any?, Any> = mapOf(
        KeyMessageType to MessageTypeWatchEventAck,
        KeyAcknowledgedEventId to eventId
    )

    private fun commandForPrimaryAction(label: String): String = when (label) {
        "Tag starten" -> WatchCommandStartDay
        "Pause starten" -> WatchCommandStartBreak
        "Weiterarbeiten" -> WatchCommandResumeWork
        "Neuen Tag starten" -> WatchCommandStartNewDay
        else -> WatchCommandPrimary
    }

    private data class WatchEvent(
        val id: String,
        val command: String,
        val date: LocalDate,
        val minuteOfDay: Int
    )

    private class ProcessedWatchEventStore(
        private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    ) {
        fun contains(id: String): Boolean = ids().contains(id)

        fun add(id: String) {
            val updated = (ids().filterNot { it == id } + id).takeLast(MaxProcessedEventIds)
            defaults.setObject(
                updated.joinToString(separator = "\n"),
                forKey = ProcessedWatchEventIdsKey
            )
        }

        private fun ids(): List<String> =
            defaults.stringForKey(ProcessedWatchEventIdsKey)
                ?.lineSequence()
                ?.filter { it.isNotBlank() }
                ?.toList()
                ?: emptyList()
    }
}

private fun Any?.asInt(): Int? = when (this) {
    is Int -> this
    is Long -> this.toInt()
    is Short -> this.toInt()
    is NSNumber -> intValue
    is String -> toIntOrNull()
    else -> null
}

private const val MinutesPerDay = 24 * 60
private const val MaxProcessedEventIds = 200
private const val ProcessedWatchEventIdsKey = "processed_watch_event_ids"
