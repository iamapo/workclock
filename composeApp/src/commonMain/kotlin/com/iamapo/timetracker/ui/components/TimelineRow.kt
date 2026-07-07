package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.state.TimelineItemUiModel
import com.iamapo.timetracker.ui.state.TimelineKind
import com.iamapo.timetracker.ui.theme.AppColors

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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(54.dp)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorFor(item.kind))
            )
            Text(
                text = item.title,
                color = AppColors.Ink,
                fontWeight = FontWeight.Medium
            )
        }
    }

    private fun colorFor(kind: TimelineKind): Color = when (kind) {
        TimelineKind.Work -> AppColors.Blue
        TimelineKind.Break -> AppColors.Rose
        TimelineKind.Target -> AppColors.Cyan
    }
}
