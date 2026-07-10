package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.Res
import workclock.composeapp.generated.resources.working_until

object EndTimeCard {
    @Composable
    operator fun invoke(endTime: String, breakRequirementLabel: String, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Blue.copy(alpha = 0.10f),
            border = BorderStroke(AppDimensions.size1, AppColors.Blue.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(AppDimensions.size8)
        ) {
            Row(
                modifier = Modifier.padding(AppDimensions.size14),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.working_until),
                        color = AppColors.Muted,
                        fontSize = AppFontSizes.size12,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = endTime,
                        color = AppColors.Ink,
                        fontSize = AppFontSizes.size28,
                        lineHeight = AppFontSizes.size30,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = breakRequirementLabel,
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size12,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun EndTimeCardPreview() {
    TimeTrackerTheme {
        EndTimeCard("17:21 Uhr", "inkl. 30 min Pause")
    }
}
