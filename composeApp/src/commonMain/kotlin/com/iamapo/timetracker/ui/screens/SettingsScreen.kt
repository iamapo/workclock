package com.iamapo.timetracker.ui.screens

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.SettingsPanel
import com.iamapo.timetracker.ui.components.TopBarSection
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*

object SettingsScreen {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onDecreaseDailyTarget: () -> Unit,
        onIncreaseDailyTarget: () -> Unit,
        onDecreaseRequiredBreak: () -> Unit,
        onIncreaseRequiredBreak: () -> Unit,
        onDecreaseWeeklyTarget: () -> Unit,
        onIncreaseWeeklyTarget: () -> Unit,
        onLockScreenStatusChanged: (Boolean) -> Unit,
        onDeleteAllEntries: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var confirmDelete by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentPadding = PaddingValues(start = AppDimensions.size20, top = AppDimensions.size18, end = AppDimensions.size20, bottom = AppDimensions.size28),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
        ) {
            item {
                TopBarSection(
                    dateLabel = stringResource(Res.string.settings_subtitle),
                    title = stringResource(Res.string.settings_title)
                )
            }
            item {
                SettingsPanel(
                    settings = state.settings,
                    onDecreaseDailyTarget = onDecreaseDailyTarget,
                    onIncreaseDailyTarget = onIncreaseDailyTarget,
                    onDecreaseRequiredBreak = onDecreaseRequiredBreak,
                    onIncreaseRequiredBreak = onIncreaseRequiredBreak,
                    onDecreaseWeeklyTarget = onDecreaseWeeklyTarget,
                    onIncreaseWeeklyTarget = onIncreaseWeeklyTarget,
                    onLockScreenStatusChanged = onLockScreenStatusChanged
                )
            }
            item {
                DeleteEntriesPanel(
                    confirmDelete = confirmDelete,
                    onRequestConfirm = { confirmDelete = true },
                    onCancel = { confirmDelete = false },
                    onConfirm = {
                        onDeleteAllEntries()
                        confirmDelete = false
                    }
                )
            }
        }
    }

    @Composable
    private fun DeleteEntriesPanel(
        confirmDelete: Boolean,
        onRequestConfirm: () -> Unit,
        onCancel: () -> Unit,
        onConfirm: () -> Unit
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
            ) {
                Text(
                    text = stringResource(Res.string.data),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Text(
                    text = if (confirmDelete) {
                        stringResource(Res.string.delete_confirm)
                    } else {
                        stringResource(Res.string.delete_entries)
                    },
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size18,
                    lineHeight = AppFontSizes.size22,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (confirmDelete) {
                        stringResource(Res.string.delete_description_confirm)
                    } else {
                        stringResource(Res.string.delete_description)
                    },
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size13,
                    lineHeight = AppFontSizes.size18
                )

                if (confirmDelete) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.size10)
                    ) {
                        DestructiveButton(
                            label = stringResource(Res.string.cancel),
                            color = AppColors.Muted,
                            background = AppColors.PanelRaised,
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        )
                        DestructiveButton(
                            label = stringResource(Res.string.delete),
                            color = AppColors.Rose,
                            background = AppColors.Rose.copy(alpha = 0.10f),
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    DestructiveButton(
                        label = stringResource(Res.string.delete_all_entries),
                        color = AppColors.Rose,
                        background = AppColors.Rose.copy(alpha = 0.10f),
                        onClick = onRequestConfirm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun DestructiveButton(
        label: String,
        color: androidx.compose.ui.graphics.Color,
        background: androidx.compose.ui.graphics.Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(AppDimensions.size14)
        Surface(
            modifier = modifier.clip(shape).clickable(onClick = onClick),
            color = background,
            border = BorderStroke(AppDimensions.size1, color.copy(alpha = 0.35f)),
            shape = shape
        ) {
            Text(
                text = label,
                color = color,
                fontSize = AppFontSizes.size15,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = AppDimensions.size16, vertical = AppDimensions.size16)
            )
        }
    }
}

@Preview(
    name = "Screen - Einstellungen",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun SettingsScreenPreview() {
    TimeTrackerTheme {
        SettingsScreen(
            state = TimeTrackerPreviewData.uiState,
            onDecreaseDailyTarget = {},
            onIncreaseDailyTarget = {},
            onDecreaseRequiredBreak = {},
            onIncreaseRequiredBreak = {},
            onDecreaseWeeklyTarget = {},
            onIncreaseWeeklyTarget = {},
            onLockScreenStatusChanged = {},
            onDeleteAllEntries = {}
        )
    }
}
