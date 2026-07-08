package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.PreviewFrame
import com.iamapo.timetracker.ui.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object CalendarPanel {
    @Composable
    operator fun invoke(
        monthTitle: String,
        days: List<CalendarDayUiModel>,
        plannedWeek: String,
        reachedWeek: String,
        modifier: Modifier = Modifier,
        onOpenCalendar: (() -> Unit)? = null
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onOpenCalendar != null) Modifier.clickable(onClick = onOpenCalendar) else Modifier),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "KALENDER",
                    color = AppColors.Subtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                CalendarWeekdays()
                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { day ->
                            CalendarDayCell(day, modifier = Modifier.weight(1f))
                        }
                    }
                }
                WeekSummaryRow(plannedWeek, reachedWeek)
            }
        }
    }
}

@Preview
@Composable
private fun CalendarPanelPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            CalendarPanel(
                monthTitle = TimeTrackerPreviewData.uiState.monthTitle,
                days = TimeTrackerPreviewData.uiState.calendarDays.take(14),
                plannedWeek = TimeTrackerPreviewData.uiState.plannedWeek,
                reachedWeek = TimeTrackerPreviewData.uiState.reachedWeek,
                onOpenCalendar = {}
            )
        }
    }
}
