package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.state.TargetItemUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TargetSummaryStrip {
    @Composable
    operator fun invoke(items: List<TargetItemUiModel>, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size14)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.size0)) {
                items.forEachIndexed { index, item ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.size16, vertical = AppDimensions.size11),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            color = AppColors.Muted,
                            fontSize = AppFontSizes.size14,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.value,
                            color = AppColors.Ink,
                            fontSize = AppFontSizes.size15,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(color = AppColors.Line, thickness = AppDimensions.size1)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TargetSummaryStripPreview() {
    TimeTrackerTheme {
        TargetSummaryStrip(
            listOf(
                TargetItemUiModel("Arbeiten bis", "17:21"),
                TargetItemUiModel("Soll heute", "8 h"),
                TargetItemUiModel("Pause", "30 min")
            )
        )
    }
}
