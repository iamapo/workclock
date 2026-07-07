package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.CalendarDayStyle
import com.iamapo.timetracker.ui.state.CalendarDayUiModel
import com.iamapo.timetracker.ui.theme.AppColors

object CalendarDayCell {
    @Composable
    operator fun invoke(day: CalendarDayUiModel, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),
            color = backgroundFor(day.style),
            border = BorderStroke(1.dp, borderFor(day.style)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(7.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.day,
                    color = textFor(day.style),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = day.note,
                    color = noteFor(day.style),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    private fun backgroundFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.10f)
        CalendarDayStyle.Today -> AppColors.Blue.copy(alpha = 0.22f)
        CalendarDayStyle.Planned -> AppColors.Amber.copy(alpha = 0.10f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Soft.copy(alpha = 0.42f)
    }

    private fun borderFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Done -> AppColors.Green.copy(alpha = 0.42f)
        CalendarDayStyle.Today -> AppColors.Blue.copy(alpha = 0.72f)
        CalendarDayStyle.Planned -> AppColors.Amber.copy(alpha = 0.40f)
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Line
    }

    private fun textFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Muted,
        CalendarDayStyle.Weekend -> AppColors.Muted.copy(alpha = 0.55f)
        else -> AppColors.Ink
    }

    private fun noteFor(style: CalendarDayStyle): Color = when (style) {
        CalendarDayStyle.Today -> AppColors.Ink
        else -> AppColors.Muted
    }
}
