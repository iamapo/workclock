package com.iamapo.timetracker.watch

import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationState
import platform.WatchConnectivity.WCSessionDelegateProtocol
import platform.darwin.NSObject

private const val KeyState = "state"
private const val KeyRemaining = "remaining"
private const val KeyCaption = "caption"
private const val KeyPrimaryAction = "primaryAction"
private const val KeySecondaryAction = "secondaryAction"
private const val KeyCommand = "command"

private const val WatchCommandPrimary = "primary"
private const val WatchCommandStartDay = "startDay"
private const val WatchCommandStartBreak = "startBreak"
private const val WatchCommandResumeWork = "resumeWork"

@OptIn(ExperimentalForeignApi::class)
class IosWatchSessionController(
    private val onCommand: (String) -> Unit
) : NSObject(), WCSessionDelegateProtocol {
    private val session: WCSession? =
        if (WCSession.isSupported()) WCSession.defaultSession else null

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

    override fun session(
        session: WCSession,
        didReceiveMessage: Map<Any?, *>
    ) {
        val command = didReceiveMessage[KeyCommand] as? String ?: return
        onCommand(command)
    }

    override fun session(
        session: WCSession,
        didReceiveMessage: Map<Any?, *>,
        replyHandler: (Map<Any?, *>?) -> Unit
    ) {
        val command = didReceiveMessage[KeyCommand] as? String
        if (command != null) onCommand(command)
        replyHandler(emptyMap<Any?, Any>())
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

    private fun commandForPrimaryAction(label: String): String = when (label) {
        "Tag starten" -> WatchCommandStartDay
        "Pause starten" -> WatchCommandStartBreak
        "Weiterarbeiten" -> WatchCommandResumeWork
        else -> WatchCommandPrimary
    }
}
