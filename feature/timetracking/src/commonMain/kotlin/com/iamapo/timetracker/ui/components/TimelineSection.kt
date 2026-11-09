package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.booked_today
import com.iamapo.timetracker.resources.week
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppFontSizes
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource

object TimelineSection {
    @Composable
    operator fun invoke(
        items: List<TimelineItemUiModel>,
        weeklyBalance: String,
        modifier: Modifier = Modifier,
        onEditItem: ((TimelineItemUiModel) -> Unit)? = null
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.size8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(AppDimensions.size24),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.size(AppDimensions.size24)) {
                            drawCircle(AppColors.Green.copy(alpha = 0.18f))
                            repeat(3) { index ->
                                val y = size.height * (0.32f + index * 0.18f)
                                drawCircle(AppColors.Success, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.29f, y))
                                drawLine(
                                    AppColors.Success,
                                    Offset(size.width * 0.42f, y),
                                    Offset(size.width * 0.72f, y),
                                    strokeWidth = 1.2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(Res.string.booked_today),
                        color = AppColors.Ink,
                        fontSize = AppFontSizes.size16,
                        lineHeight = AppFontSizes.size20,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${stringResource(Res.string.week)} $weeklyBalance",
                    color = balanceColor(weeklyBalance),
                    fontSize = AppFontSizes.size13,
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalDivider(color = AppColors.CalendarLine, thickness = AppDimensions.size1)
            items.forEachIndexed { index, item ->
                TimelineRow(
                    item = item,
                    onEdit = if (item.edit != null && onEditItem != null) {
                        { onEditItem(item) }
                    } else {
                        null
                    }
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = AppDimensions.size24),
                        color = AppColors.CalendarLine,
                        thickness = AppDimensions.size1
                    )
                }
            }
        }
    }

    /** Reads the sign off the label TimeTextFormatter.signedClockDuration produced. */
    private fun balanceColor(balance: String): Color = when {
        balance.startsWith('−') -> AppColors.Coral
        balance.startsWith('+') -> AppColors.Success
        else -> AppColors.Subtle
    }
}

@Preview
@Composable
private fun TimelineSectionPreview() {
    ComposePreviewContext()
    TimeTrackerTheme {
        TimelineSection(TimeTrackerPreviewData.uiState().timeline, "+1:12 h")
    }
}
