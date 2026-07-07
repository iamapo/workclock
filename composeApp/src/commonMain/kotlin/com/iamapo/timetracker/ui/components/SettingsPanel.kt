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
    operator fun invoke(settings: SettingsUiModel, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Schnelle Vorgaben",
                    color = AppColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                SettingsRow("Tägliches Ziel", settings.dailyTarget)
                SettingsRow("Pflichtpause", settings.requiredBreak)
                SettingsRow("Wochenziel", settings.weeklyTarget)
            }
        }
    }
}
