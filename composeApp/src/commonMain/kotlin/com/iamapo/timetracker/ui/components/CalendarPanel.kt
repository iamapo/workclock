package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

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
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.Res
import workclock.composeapp.generated.resources.calendar

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
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size12)
            ) {
                Text(
                    text = stringResource(Res.string.calendar),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size24,
                    fontWeight = FontWeight.Black
                )
                CalendarWeekdays()
                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.size6)
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
        CalendarPanel(
            monthTitle = TimeTrackerPreviewData.uiState.monthTitle,
            days = TimeTrackerPreviewData.uiState.calendarPreviewDays,
            plannedWeek = TimeTrackerPreviewData.uiState.plannedWeek,
            reachedWeek = TimeTrackerPreviewData.uiState.reachedWeek,
            onOpenCalendar = {}
        )
    }
}
