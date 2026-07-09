package com.iamapo.timetracker.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.SettingsPanel
import com.iamapo.timetracker.ui.components.TopBarSection
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

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
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TopBarSection(
                    dateLabel = "Vorgaben & Daten",
                    title = "Einstellungen"
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
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DATEN",
                    color = AppColors.Subtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = if (confirmDelete) {
                        "Alle gespeicherten Zeiteinträge wirklich löschen?"
                    } else {
                        "Gespeicherte Zeiteinträge löschen"
                    },
                    color = AppColors.Ink,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (confirmDelete) {
                        "Die schnellen Vorgaben bleiben erhalten. Kalender- und Arbeitszeitdaten werden zurückgesetzt."
                    } else {
                        "Setzt Historie, Kalender-Einträge und den aktuellen Arbeitstag zurück."
                    },
                    color = AppColors.Muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (confirmDelete) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DestructiveButton(
                            label = "Abbrechen",
                            color = AppColors.Muted,
                            background = AppColors.PanelRaised,
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        )
                        DestructiveButton(
                            label = "Löschen",
                            color = AppColors.Rose,
                            background = AppColors.Rose.copy(alpha = 0.10f),
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    DestructiveButton(
                        label = "Alle Einträge löschen",
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
        Surface(
            modifier = modifier.clickable(onClick = onClick),
            color = background,
            border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
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
