package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*

object CalendarWeekdays {
    @Composable
    operator fun invoke(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Res.string.monday_short, Res.string.tuesday_short, Res.string.wednesday_short,
                Res.string.thursday_short, Res.string.friday_short, Res.string.saturday_short,
                Res.string.sunday_short
            ).forEach { weekday ->
                Text(
                    text = stringResource(weekday),
                    color = AppColors.Subtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalendarWeekdaysPreview() {
    TimeTrackerTheme {
        CalendarWeekdays()
    }
}
