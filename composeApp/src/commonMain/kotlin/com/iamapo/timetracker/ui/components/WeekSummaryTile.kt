package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
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
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object WeekSummaryTile {
    @Composable
    operator fun invoke(label: String, value: String, modifier: Modifier = Modifier) {
        val accent = if (label.contains("erreicht", ignoreCase = true)) AppColors.Green else AppColors.Lemon
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = accent.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.36f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = label.uppercase(),
                    color = AppColors.Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = value,
                    color = AppColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun WeekSummaryTilePreview() {
    TimeTrackerTheme {
        WeekSummaryTile(
            label = "Aktuell erreicht",
            value = "21:40 h"
        )
    }
}
