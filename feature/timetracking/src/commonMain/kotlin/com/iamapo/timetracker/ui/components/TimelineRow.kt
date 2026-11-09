package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.state.TimelineEditUiModel
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.edit_time_action
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.LedgerMonospace
import com.iamapo.timetracker.ui.theme.AppFontSizes
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource

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
                .then(if (editable) Modifier.clickable { onEdit?.invoke() } else Modifier)
                .padding(vertical = AppDimensions.size8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
        ) {
            Box(Modifier.size(AppDimensions.size8).background(colorFor(item.kind), CircleShape))
            Text(
                text = item.time,
                color = AppColors.Ink,
                fontFamily = LedgerMonospace,
                fontSize = AppFontSizes.size13,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(AppDimensions.size56)
            )
            Text(
                text = localizedEventTitle(item.title),
                color = if (item.kind == TimelineKind.Target) AppColors.Subtle else AppColors.Ink,
                fontSize = AppFontSizes.size13,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (editable) {
                Text(
                    text = stringResource(Res.string.edit_time_action),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size11,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    private fun colorFor(kind: TimelineKind): Color = when (kind) {
        TimelineKind.Work -> AppColors.Green
        TimelineKind.Break -> AppColors.Sand
        TimelineKind.Target -> AppColors.Coral
    }
}

@Preview
@Composable
private fun TimelineRowPreview() {
    TimeTrackerTheme {
        TimelineRow(
            item = TimelineItemUiModel(
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
