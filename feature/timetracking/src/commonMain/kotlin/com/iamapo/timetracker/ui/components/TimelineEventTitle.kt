package com.iamapo.timetracker.ui.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

/** Timeline events carry their German title from the domain; this maps it back to the active locale. */
@Composable
internal fun localizedEventTitle(title: String): String = when (title) {
    "Arbeitsbeginn" -> stringResource(Res.string.event_work_started)
    "Pause gestartet" -> stringResource(Res.string.event_break_started)
    "Weitergearbeitet" -> stringResource(Res.string.event_work_resumed)
    "Arbeitstag beendet" -> stringResource(Res.string.event_workday_finished)
    "Manueller Eintrag" -> stringResource(Res.string.event_manual_entry)
    "Geplanter Feierabend" -> stringResource(Res.string.event_planned_end)
    else -> title
}
