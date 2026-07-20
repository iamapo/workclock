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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

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
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
            ) {
                Text(
                    text = stringResource(Res.string.quick_targets),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                SettingsRow(
                    label = stringResource(Res.string.daily),
                    value = settings.dailyTarget,
                    onDecrease = onDecreaseDailyTarget,
                    onIncrease = onIncreaseDailyTarget,
                    canDecrease = settings.canDecreaseDailyTarget,
                    canIncrease = settings.canIncreaseDailyTarget
                )
                SettingsRow(
                    label = stringResource(Res.string.break_label),
                    value = settings.requiredBreak,
                    onDecrease = onDecreaseRequiredBreak,
                    onIncrease = onIncreaseRequiredBreak,
                    canDecrease = settings.canDecreaseRequiredBreak,
                    canIncrease = settings.canIncreaseRequiredBreak
                )
                SettingsRow(
                    label = stringResource(Res.string.week),
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
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size14),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size4)
            ) {
                Text(
                    text = stringResource(Res.string.lock_screen_status),
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.lock_screen_description),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size12,
                    lineHeight = AppFontSizes.size16
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
