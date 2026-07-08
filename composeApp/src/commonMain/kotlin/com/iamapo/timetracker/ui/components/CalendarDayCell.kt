package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object CalendarDayCell {
    @Composable
    operator fun invoke(
        day: CalendarDayUiModel,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        onClick: (() -> Unit)? = null
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            color = backgroundFor(day.style),
            border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) AppColors.Ink.copy(alpha = 0.25f) else borderFor(day.style)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.day,
                    color = if (selected) AppColors.Ink else textFor(day.style),
                    fontSize = 13.sp,
                    fontWeight = if (day.isToday || selected) FontWeight.Bold else FontWeight.Normal
                )
                if (showDot(day.style)) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(4.dp),
                            color = dotFor(day.style),
                            shape = CircleShape,
                            content = {}
                        )
                    }
                }
            }
        }
    }

    private fun backgroundFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.10f)
        CalendarDayStyle.Today -> AppColors.Blue.copy(alpha = 0.15f)
        CalendarDayStyle.Planned -> Color.Transparent
        CalendarDayStyle.Vacation -> AppColors.Purple.copy(alpha = 0.09f)
        CalendarDayStyle.Sick -> AppColors.Rose.copy(alpha = 0.09f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> Color.Transparent
    }

    private fun borderFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.20f)
        CalendarDayStyle.Today -> AppColors.Blue.copy(alpha = 0.35f)
        CalendarDayStyle.Planned -> Color.Transparent
        CalendarDayStyle.Vacation -> AppColors.Purple.copy(alpha = 0.20f)
        CalendarDayStyle.Sick -> AppColors.Rose.copy(alpha = 0.20f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> Color.Transparent
    }

    private fun textFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green
        CalendarDayStyle.Today -> AppColors.Ink
        CalendarDayStyle.Planned -> AppColors.Subtle
        CalendarDayStyle.Vacation -> AppColors.Purple
        CalendarDayStyle.Sick -> AppColors.Rose
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Subtle
    }

    private fun showDot(style: CalendarDayStyle): Boolean = when (style) {
        CalendarDayStyle.Done,
        CalendarDayStyle.Today,
        CalendarDayStyle.Vacation,
        CalendarDayStyle.Sick -> true
        CalendarDayStyle.Planned,
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> false
    }

    private fun dotFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green
        CalendarDayStyle.Today -> AppColors.Blue
        CalendarDayStyle.Vacation -> AppColors.Purple
        CalendarDayStyle.Sick -> AppColors.Rose
        else -> AppColors.Subtle
    }
}

@Preview
@Composable
private fun CalendarDayCellPreview() {
    TimeTrackerTheme {
        CalendarDayCell(TimeTrackerPreviewData.uiState.calendarDays.first { it.isToday })
    }
}
