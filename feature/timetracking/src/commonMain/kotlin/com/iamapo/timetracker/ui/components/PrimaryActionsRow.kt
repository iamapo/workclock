package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppFontSizes
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object PrimaryActionsRow {
    @Composable
    operator fun invoke(
        primaryLabel: String,
        secondaryLabel: String?,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier,
        primaryColor: Color = Color.White,
        primaryContainerColor: Color = AppColors.Ink,
        secondaryColor: Color = AppColors.Coral
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.size8)
        ) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.size52),
                shape = RoundedCornerShape(AppDimensions.size10),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerColor,
                    contentColor = primaryColor
                ),
                contentPadding = PaddingValues(horizontal = AppDimensions.size16, vertical = AppDimensions.size12)
            ) {
                Text(primaryLabel, fontSize = AppFontSizes.size16, fontWeight = FontWeight.Medium)
            }
            if (secondaryLabel != null) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth().heightIn(min = AppDimensions.size52),
                    shape = RoundedCornerShape(AppDimensions.size10),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = secondaryColor),
                    border = BorderStroke(AppDimensions.size1, secondaryColor),
                    contentPadding = PaddingValues(horizontal = AppDimensions.size16, vertical = AppDimensions.size12)
                ) {
                    Text(secondaryLabel, fontSize = AppFontSizes.size16, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PrimaryActionsRowPreview() {
    TimeTrackerTheme {
        PrimaryActionsRow("Pause starten", "Tag beenden", {}, {})
    }
}
