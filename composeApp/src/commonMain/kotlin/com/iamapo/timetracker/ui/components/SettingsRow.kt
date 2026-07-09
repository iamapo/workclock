package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object SettingsRow {
    @Composable
    operator fun invoke(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        onDecrease: (() -> Unit)? = null,
        onIncrease: (() -> Unit)? = null,
        canDecrease: Boolean = true,
        canIncrease: Boolean = true
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = AppColors.Muted, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDecrease != null) {
                    StepButton(
                        label = "-",
                        onClick = onDecrease,
                        enabled = canDecrease
                    )
                }
                Surface(
                    color = AppColors.Lemon.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = value,
                        color = AppColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                if (onIncrease != null) {
                    StepButton(
                        label = "+",
                        onClick = onIncrease,
                        enabled = canIncrease
                    )
                }
            }
        }
    }

    @Composable
    private fun StepButton(
        label: String,
        onClick: () -> Unit,
        enabled: Boolean
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .heightIn(min = 36.dp)
                .widthIn(min = 40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.PanelRaised,
                contentColor = AppColors.Ink,
                disabledContainerColor = AppColors.Panel,
                disabledContentColor = AppColors.Subtle
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) {
            Text(label, fontWeight = FontWeight.Black)
        }
    }
}

@Preview
@Composable
private fun SettingsRowPreview() {
    TimeTrackerTheme {
        SettingsRow(
            label = "Pflichtpause",
            value = "30 min",
            onDecrease = {},
            onIncrease = {}
        )
    }
}
