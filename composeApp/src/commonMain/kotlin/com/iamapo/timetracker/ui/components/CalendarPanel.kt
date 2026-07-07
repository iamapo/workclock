package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors

object CalendarPanel {
    @Composable
    operator fun invoke(
        monthTitle: String,
        days: List<CalendarDayUiModel>,
        plannedWeek: String,
        reachedWeek: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kalender",
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
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
