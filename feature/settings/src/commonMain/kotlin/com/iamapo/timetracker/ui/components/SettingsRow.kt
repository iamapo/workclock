package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8),
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
                    modifier = Modifier.width(AppDimensions.size88),
                    color = AppColors.Lemon.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(AppDimensions.size10)
                ) {
                    Text(
                        text = value,
                        color = AppColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.size8, vertical = AppDimensions.size8)
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
                .heightIn(min = AppDimensions.size36)
                .widthIn(min = AppDimensions.size40),
            shape = RoundedCornerShape(AppDimensions.size8),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.PanelRaised,
                contentColor = AppColors.Ink,
                disabledContainerColor = AppColors.Panel,
                disabledContentColor = AppColors.Subtle
            ),
            contentPadding = PaddingValues(horizontal = AppDimensions.size10, vertical = AppDimensions.size0)
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
            label = "Pause",
            value = "30 min",
            onDecrease = {},
            onIncrease = {}
        )
    }
}
