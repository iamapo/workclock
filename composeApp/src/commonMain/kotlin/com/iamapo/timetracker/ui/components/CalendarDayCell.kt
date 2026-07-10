package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.Res
import workclock.composeapp.generated.resources.until_time

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
                .heightIn(min = 58.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            color = backgroundFor(day.style),
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AppColors.Ink.copy(alpha = 0.42f) else borderFor(day.style)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(7.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.day,
                    color = if (selected) AppColors.Ink else textFor(day.style),
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (day.isToday || selected) FontWeight.Black else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (day.note.isNotBlank()) {
                    Text(
                        text = day.note.removePrefix(stringResource(Res.string.until_time, "")),
                        color = noteFor(day.style),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }

    private fun backgroundFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.20f)
        CalendarDayStyle.Today -> AppColors.Blue
        CalendarDayStyle.Planned -> AppColors.Lemon.copy(alpha = 0.38f)
        CalendarDayStyle.Vacation -> AppColors.Purple.copy(alpha = 0.16f)
        CalendarDayStyle.Sick -> AppColors.Rose.copy(alpha = 0.16f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Soft.copy(alpha = 0.48f)
    }

    private fun borderFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.44f)
        CalendarDayStyle.Today -> AppColors.Blue
        CalendarDayStyle.Planned -> AppColors.Lemon.copy(alpha = 0.70f)
        CalendarDayStyle.Vacation -> AppColors.Purple.copy(alpha = 0.34f)
        CalendarDayStyle.Sick -> AppColors.Rose.copy(alpha = 0.34f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Line
    }

    private fun textFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Ink
        CalendarDayStyle.Today -> AppColors.Paper
        CalendarDayStyle.Planned -> AppColors.Subtle
        CalendarDayStyle.Vacation -> AppColors.Purple
        CalendarDayStyle.Sick -> AppColors.Rose
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Subtle
    }

    private fun noteFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Today -> AppColors.Paper.copy(alpha = 0.92f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Subtle.copy(alpha = 0.62f)
        else -> AppColors.Muted
    }
}

@Preview
@Composable
private fun CalendarDayCellPreview() {
    TimeTrackerTheme {
        CalendarDayCell(TimeTrackerPreviewData.uiState.calendarDays.first { it.isToday })
    }
}
