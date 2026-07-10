package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object SettingsPanel {
    @Composable
    operator fun invoke(
        settings: SettingsUiModel,
        onDecreaseDailyTarget: () -> Unit,
        onIncreaseDailyTarget: () -> Unit,
        onDecreaseRequiredBreak: () -> Unit,
        onIncreaseRequiredBreak: () -> Unit,
        onDecreaseWeeklyTarget: () -> Unit,
        onIncreaseWeeklyTarget: () -> Unit,
        onLockScreenStatusChanged: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SCHNELLE VORGABEN",
                    color = AppColors.Subtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.2.sp
                )
                SettingsRow(
                    label = "Tägliches Ziel",
                    value = settings.dailyTarget,
                    onDecrease = onDecreaseDailyTarget,
                    onIncrease = onIncreaseDailyTarget,
                    canDecrease = settings.canDecreaseDailyTarget,
                    canIncrease = settings.canIncreaseDailyTarget
                )
                SettingsRow(
                    label = "Pause",
                    value = settings.requiredBreak,
                    onDecrease = onDecreaseRequiredBreak,
                    onIncrease = onIncreaseRequiredBreak,
                    canDecrease = settings.canDecreaseRequiredBreak,
                    canIncrease = settings.canIncreaseRequiredBreak
                )
                SettingsRow(
                    label = "Wochenarbeitszeit",
                    value = settings.weeklyTarget,
                    onDecrease = onDecreaseWeeklyTarget,
                    onIncrease = onIncreaseWeeklyTarget,
                    canDecrease = settings.canDecreaseWeeklyTarget,
                    canIncrease = settings.canIncreaseWeeklyTarget
                )
                LockScreenStatusRow(
                    enabled = settings.lockScreenStatusEnabled,
                    onEnabledChange = onLockScreenStatusChanged
                )
            }
        }
    }

    @Composable
    private fun LockScreenStatusRow(
        enabled: Boolean,
        onEnabledChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sperrbildschirm-Status",
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Arbeitszeit und Pausen live anzeigen",
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.Paper,
                    checkedTrackColor = AppColors.Green,
                    uncheckedThumbColor = AppColors.Paper,
                    uncheckedTrackColor = AppColors.SoftMuted,
                    uncheckedBorderColor = AppColors.LineStrong
                )
            )
        }
    }
}

@Preview
@Composable
private fun SettingsPanelPreview() {
    TimeTrackerTheme {
        SettingsPanel(
            settings = SettingsUiModel(
                dailyTarget = "8:00 h",
                canDecreaseDailyTarget = true,
                canIncreaseDailyTarget = true,
                requiredBreak = "30 min",
                canDecreaseRequiredBreak = true,
                canIncreaseRequiredBreak = true,
                weeklyTarget = "40:00 h",
                canDecreaseWeeklyTarget = true,
                canIncreaseWeeklyTarget = true,
                lockScreenStatusEnabled = true
            ),
            onDecreaseDailyTarget = {},
            onIncreaseDailyTarget = {},
            onDecreaseRequiredBreak = {},
            onIncreaseRequiredBreak = {},
            onDecreaseWeeklyTarget = {},
            onIncreaseWeeklyTarget = {},
            onLockScreenStatusChanged = {}
        )
    }
}
