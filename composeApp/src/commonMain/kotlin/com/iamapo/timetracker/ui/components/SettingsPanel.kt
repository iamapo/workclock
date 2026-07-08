package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.SettingsUiModel
import com.iamapo.timetracker.ui.theme.AppColors

object SettingsPanel {
    @Composable
    operator fun invoke(
        settings: SettingsUiModel,
        onDecreaseRequiredBreak: () -> Unit,
        onIncreaseRequiredBreak: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SCHNELLE VORGABEN",
                    color = AppColors.Subtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                SettingsRow("Tägliches Ziel", settings.dailyTarget)
                SettingsRow(
                    label = "Pflichtpause",
                    value = settings.requiredBreak,
                    onDecrease = onDecreaseRequiredBreak,
                    onIncrease = onIncreaseRequiredBreak,
                    canDecrease = settings.canDecreaseRequiredBreak,
                    canIncrease = settings.canIncreaseRequiredBreak
                )
                SettingsRow("Wochenziel", settings.weeklyTarget)
            }
        }
    }
}
