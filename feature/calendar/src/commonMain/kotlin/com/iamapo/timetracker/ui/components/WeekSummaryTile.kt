package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

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
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object WeekSummaryTile {
    @Composable
    operator fun invoke(label: String, value: String, modifier: Modifier = Modifier) {
        val accent = if (label.contains("erreicht", ignoreCase = true)) AppColors.Green else AppColors.Lemon
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = accent.copy(alpha = 0.22f),
            border = BorderStroke(AppDimensions.size1, accent.copy(alpha = 0.36f)),
            shape = RoundedCornerShape(AppDimensions.size12)
        ) {
            Column(modifier = Modifier.padding(AppDimensions.size12)) {
                Text(
                    text = label.uppercase(),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Text(
                    text = value,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size18,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = AppDimensions.size6)
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
