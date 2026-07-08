package com.iamapo.timetracker.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.TargetItemUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TargetSummaryStrip {
    @Composable
    operator fun invoke(items: List<TargetItemUiModel>, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items.forEachIndexed { index, item ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            color = AppColors.Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = item.value,
                            color = AppColors.Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
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
                TargetItemUiModel("Pflichtpause", "30 min")
            )
        )
    }
}
