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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.booked_today
import com.iamapo.timetracker.resources.week
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(25.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.size(25.dp)) {
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
                        color = AppColors.Navy,
                        fontSize = 16.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "${stringResource(Res.string.week)} $weeklyBalance",
                    color = AppColors.Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = AppColors.CalendarLine, thickness = 1.dp)
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
                        modifier = Modifier.padding(start = 24.dp),
                        color = AppColors.CalendarLine,
                        thickness = 1.dp
                    )
                }
            }
        }
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
