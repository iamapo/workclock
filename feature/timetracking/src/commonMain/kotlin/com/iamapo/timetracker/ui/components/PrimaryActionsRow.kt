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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors
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
        primaryContainerColor: Color = AppColors.Navy
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerColor,
                    contentColor = primaryColor
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(primaryLabel, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            if (secondaryLabel != null) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Coral),
                    border = BorderStroke(1.5.dp, AppColors.Coral),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    Text(secondaryLabel, fontSize = 16.sp, fontWeight = FontWeight.Black)
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
