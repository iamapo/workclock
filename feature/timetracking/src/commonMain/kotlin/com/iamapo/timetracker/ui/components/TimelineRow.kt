package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.iamapo.timetracker.presentation.state.TimelineEditUiModel
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.LedgerMonospace
import com.iamapo.timetracker.ui.theme.LedgerShapes
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

object TimelineRow {
    @Composable
    operator fun invoke(
        item: TimelineItemUiModel,
        modifier: Modifier = Modifier,
        onEdit: (() -> Unit)? = null
    ) {
        val editable = item.edit != null && onEdit != null
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(LedgerShapes.CardSmall)
                .then(
                    if (editable) {
                        Modifier.clickable { onEdit?.invoke() }
                    } else {
                        Modifier
                    }
                )
                .heightIn(min = AppDimensions.size28),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size12)
        ) {
            Text(
                text = item.time,
                color = AppColors.Muted,
                fontSize = AppFontSizes.size13,
                fontWeight = FontWeight.Black,
                fontFamily = LedgerMonospace,
                modifier = Modifier.width(AppDimensions.size50)
            )
            Box(
                modifier = Modifier
                    .size(AppDimensions.size16)
                    .clip(CircleShape)
                    .background(colorFor(item.kind).copy(alpha = if (item.kind == TimelineKind.Target) 0.16f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.kind == TimelineKind.Target) {
                    Box(
                        modifier = Modifier
                            .size(AppDimensions.size8)
                            .clip(RoundedCornerShape(AppDimensions.size99))
                            .background(colorFor(item.kind))
                    )
                }
            }
            Text(
                text = localizedEventTitle(item.title),
                color = if (item.kind == TimelineKind.Target) AppColors.Subtle else AppColors.Ink,
                fontSize = AppFontSizes.size14,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (editable) {
                Text(
                    text = stringResource(Res.string.edit_time_action),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size11,
                    fontWeight = FontWeight.Black,
                    fontFamily = LedgerMonospace,
                    letterSpacing = AppFontSizes.size0_4,
                    modifier = Modifier.padding(start = AppDimensions.size4)
                )
            }
        }
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
                kind = TimelineKind.Work,
                edit = TimelineEditUiModel(
                    eventIndex = 2,
                    minuteOfDay = 12 * 60 + 26,
                    earliestMinute = 12 * 60 + 2,
                    latestMinute = 14 * 60 + 33
                )
            ),
            onEdit = {}
        )
    }
}
