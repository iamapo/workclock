package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*

object TimelineRow {
    @Composable
    operator fun invoke(item: TimelineItemUiModel, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = item.time,
                color = AppColors.Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(50.dp)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(colorFor(item.kind).copy(alpha = if (item.kind == TimelineKind.Target) 0.16f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.kind == TimelineKind.Target) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(colorFor(item.kind))
                    )
                }
            }
            Text(
                text = localizedEventTitle(item.title),
                color = if (item.kind == TimelineKind.Target) AppColors.Subtle else AppColors.Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun localizedEventTitle(title: String): String = when (title) {
        "Arbeitsbeginn" -> stringResource(Res.string.event_work_started)
        "Pause gestartet" -> stringResource(Res.string.event_break_started)
        "Weitergearbeitet" -> stringResource(Res.string.event_work_resumed)
        "Arbeitstag beendet" -> stringResource(Res.string.event_workday_finished)
        "Manueller Eintrag" -> stringResource(Res.string.event_manual_entry)
        "Geplanter Feierabend" -> stringResource(Res.string.event_planned_end)
        else -> title
    }

    private fun colorFor(kind: TimelineKind): Color = when (kind) {
        TimelineKind.Work -> AppColors.Green
        TimelineKind.Break -> AppColors.Amber
        TimelineKind.Target -> AppColors.Subtle
    }
}

@Preview
@Composable
private fun TimelineRowPreview() {
    TimeTrackerTheme {
        TimelineRow(
            TimelineItemUiModel(
                time = "12:26",
                title = "Weitergearbeitet",
                kind = TimelineKind.Work
            )
        )
    }
}
