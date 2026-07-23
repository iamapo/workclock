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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.backup.BackupUiStatus
import com.iamapo.timetracker.backup.PendingBackupImport
import com.iamapo.timetracker.domain.GermanFederalState
import com.iamapo.timetracker.ui.components.SettingsRow
import com.iamapo.timetracker.ui.components.SettingsPanel
import com.iamapo.timetracker.ui.components.TopBarSection
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

object SettingsScreen {
    @Composable
    operator fun invoke(
        state: SettingsUiModel,
        onDecreaseRequiredBreak: () -> Unit,
        onIncreaseRequiredBreak: () -> Unit,
        onLockScreenStatusChanged: (Boolean) -> Unit,
        onDecreaseWeekdayTarget: (Int) -> Unit,
        onIncreaseWeekdayTarget: (Int) -> Unit,
        onAutomaticHolidaysChanged: (Boolean) -> Unit,
        onHolidayFederalStateChanged: (GermanFederalState) -> Unit,
        backupStatus: BackupUiStatus,
        pendingBackupImport: PendingBackupImport?,
        canUndoImport: Boolean,
        onExportBackup: () -> Unit,
        onImportBackup: () -> Unit,
        onCancelImport: () -> Unit,
        onConfirmImport: () -> Unit,
        onUndoImport: () -> Unit,
        onDeleteAllEntries: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var confirmDelete by remember { mutableStateOf(false) }
        var showFederalStateDialog by remember { mutableStateOf(false) }

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
                    settings = state,
                    onDecreaseRequiredBreak = onDecreaseRequiredBreak,
                    onIncreaseRequiredBreak = onIncreaseRequiredBreak,
                    onLockScreenStatusChanged = onLockScreenStatusChanged
                )
            }
            item {
                WorkSchedulePanel(
                    state = state,
                    onDecrease = onDecreaseWeekdayTarget,
                    onIncrease = onIncreaseWeekdayTarget
                )
            }
            item {
                HolidayPanel(
                    state = state,
                    onEnabledChanged = onAutomaticHolidaysChanged,
                    onChooseFederalState = { showFederalStateDialog = true }
                )
            }
            item {
                BackupPanel(
                    status = backupStatus,
                    canUndoImport = canUndoImport,
                    onExportBackup = onExportBackup,
                    onImportBackup = onImportBackup,
                    onUndoImport = onUndoImport
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

        pendingBackupImport?.let { pendingImport ->
            ImportConfirmationDialog(
                pendingImport = pendingImport,
                onCancel = onCancelImport,
                onConfirm = onConfirmImport
            )
        }
        if (showFederalStateDialog) {
            FederalStateDialog(
                selected = state.holidayFederalState,
                onDismiss = { showFederalStateDialog = false },
                onSelect = { federalState ->
                    onHolidayFederalStateChanged(federalState)
                    showFederalStateDialog = false
                }
            )
        }
    }

    @Composable
    private fun WorkSchedulePanel(
        state: SettingsUiModel,
        onDecrease: (Int) -> Unit,
        onIncrease: (Int) -> Unit
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
                    text = stringResource(Res.string.work_schedule),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                state.workdays.forEach { workday ->
                    SettingsRow(
                        label = weekdayName(workday.isoDayNumber),
                        value = workday.target,
                        onDecrease = { onDecrease(workday.isoDayNumber) },
                        onIncrease = { onIncrease(workday.isoDayNumber) },
                        canDecrease = workday.canDecrease,
                        canIncrease = workday.canIncrease
                    )
                }
                Text(
                    text = stringResource(Res.string.work_schedule_total, state.weeklyTarget),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size13,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun HolidayPanel(
        state: SettingsUiModel,
        onEnabledChanged: (Boolean) -> Unit,
        onChooseFederalState: () -> Unit
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
                    text = stringResource(Res.string.holidays),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size14),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.automatic_holidays),
                            color = AppColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.automatic_holidays_description),
                            color = AppColors.Muted,
                            fontSize = AppFontSizes.size12
                        )
                    }
                    Switch(
                        checked = state.automaticHolidaysEnabled,
                        onCheckedChange = onEnabledChanged,
                        enabled = state.holidayFederalState != null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Paper,
                            checkedTrackColor = AppColors.Green,
                            uncheckedThumbColor = AppColors.Paper,
                            uncheckedTrackColor = AppColors.SoftMuted,
                            uncheckedBorderColor = AppColors.LineStrong
                        )
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDimensions.size12))
                        .clickable(onClick = onChooseFederalState),
                    color = AppColors.PanelRaised,
                    shape = RoundedCornerShape(AppDimensions.size12)
                ) {
                    Column(Modifier.padding(AppDimensions.size14)) {
                        Text(
                            text = stringResource(Res.string.workplace_federal_state),
                            color = AppColors.Muted,
                            fontSize = AppFontSizes.size12
                        )
                        Text(
                            text = state.holidayFederalState?.let { federalState -> federalStateName(federalState) }
                                ?: stringResource(Res.string.select_federal_state),
                            color = AppColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.holiday_schedule_hint),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size12,
                    lineHeight = AppFontSizes.size16
                )
            }
        }
    }

    @Composable
    private fun FederalStateDialog(
        selected: GermanFederalState?,
        onDismiss: () -> Unit,
        onSelect: (GermanFederalState) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.workplace_federal_state)) },
            text = {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    items(GermanFederalState.entries.size) { index ->
                        val federalState = GermanFederalState.entries[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(federalState) }
                                .padding(vertical = AppDimensions.size6),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = federalState == selected,
                                onClick = { onSelect(federalState) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AppColors.Green,
                                    unselectedColor = AppColors.LineStrong,
                                    disabledSelectedColor = AppColors.Green.copy(alpha = 0.38f),
                                    disabledUnselectedColor = AppColors.LineStrong.copy(alpha = 0.38f)
                                )
                            )
                            Text(federalStateName(federalState), color = AppColors.Ink)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    @Composable
    private fun weekdayName(isoDayNumber: Int): String = stringResource(when (isoDayNumber) {
        1 -> Res.string.monday
        2 -> Res.string.tuesday
        3 -> Res.string.wednesday
        4 -> Res.string.thursday
        5 -> Res.string.friday
        6 -> Res.string.saturday
        else -> Res.string.sunday
    })

    @Composable
    private fun federalStateName(federalState: GermanFederalState): String = stringResource(when (federalState) {
        GermanFederalState.BadenWuerttemberg -> Res.string.state_baden_wuerttemberg
        GermanFederalState.Bavaria -> Res.string.state_bavaria
        GermanFederalState.Berlin -> Res.string.state_berlin
        GermanFederalState.Brandenburg -> Res.string.state_brandenburg
        GermanFederalState.Bremen -> Res.string.state_bremen
        GermanFederalState.Hamburg -> Res.string.state_hamburg
        GermanFederalState.Hesse -> Res.string.state_hesse
        GermanFederalState.MecklenburgWesternPomerania -> Res.string.state_mecklenburg_western_pomerania
        GermanFederalState.LowerSaxony -> Res.string.state_lower_saxony
        GermanFederalState.NorthRhineWestphalia -> Res.string.state_north_rhine_westphalia
        GermanFederalState.RhinelandPalatinate -> Res.string.state_rhineland_palatinate
        GermanFederalState.Saarland -> Res.string.state_saarland
        GermanFederalState.Saxony -> Res.string.state_saxony
        GermanFederalState.SaxonyAnhalt -> Res.string.state_saxony_anhalt
        GermanFederalState.SchleswigHolstein -> Res.string.state_schleswig_holstein
        GermanFederalState.Thuringia -> Res.string.state_thuringia
    })

    @Composable
    private fun BackupPanel(
        status: BackupUiStatus,
        canUndoImport: Boolean,
        onExportBackup: () -> Unit,
        onImportBackup: () -> Unit,
        onUndoImport: () -> Unit
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
                    text = stringResource(Res.string.backup_label),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Text(
                    text = stringResource(Res.string.backup_title),
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size18,
                    lineHeight = AppFontSizes.size22,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.backup_description),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size13,
                    lineHeight = AppFontSizes.size18
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size10)
                ) {
                    BackupButton(
                        label = stringResource(Res.string.export_backup),
                        color = AppColors.Blue,
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f)
                    )
                    BackupButton(
                        label = stringResource(Res.string.import_backup),
                        color = AppColors.Purple,
                        onClick = onImportBackup,
                        modifier = Modifier.weight(1f)
                    )
                }

                status.messageResource()?.let { messageResource ->
                    Text(
                        text = stringResource(messageResource),
                        color = if (status == BackupUiStatus.Failure || status == BackupUiStatus.InvalidFile) {
                            AppColors.Rose
                        } else {
                            AppColors.Muted
                        },
                        fontSize = AppFontSizes.size13,
                        lineHeight = AppFontSizes.size18
                    )
                }

                if (canUndoImport) {
                    BackupButton(
                        label = stringResource(Res.string.undo_last_import),
                        color = AppColors.Muted,
                        onClick = onUndoImport,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun BackupButton(
        label: String,
        color: androidx.compose.ui.graphics.Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(AppDimensions.size14)
        Surface(
            modifier = modifier.clip(shape).clickable(onClick = onClick),
            color = color.copy(alpha = 0.10f),
            border = BorderStroke(AppDimensions.size1, color.copy(alpha = 0.35f)),
            shape = shape
        ) {
            Text(
                text = label,
                color = color,
                fontSize = AppFontSizes.size15,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = AppDimensions.size12, vertical = AppDimensions.size16)
            )
        }
    }

    @Composable
    private fun ImportConfirmationDialog(
        pendingImport: PendingBackupImport,
        onCancel: () -> Unit,
        onConfirm: () -> Unit
    ) {
        val period: String = when {
            pendingImport.firstDate == null -> stringResource(Res.string.backup_no_days)
            pendingImport.firstDate == pendingImport.lastDate -> pendingImport.firstDate.orEmpty()
            else -> stringResource(
                Res.string.backup_period,
                pendingImport.firstDate.orEmpty(),
                pendingImport.lastDate.orEmpty()
            )
        }

        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(stringResource(Res.string.import_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.import_confirm_description,
                        pendingImport.dayCount,
                        period
                    )
                )
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(Res.string.import_backup))
                }
            }
        )
    }

    private fun BackupUiStatus.messageResource() = when (this) {
        BackupUiStatus.None -> null
        BackupUiStatus.Exported -> Res.string.backup_export_success
        BackupUiStatus.Imported -> Res.string.backup_import_success
        BackupUiStatus.ImportUndone -> Res.string.backup_undo_success
        BackupUiStatus.InvalidFile -> Res.string.backup_invalid_file
        BackupUiStatus.Failure -> Res.string.backup_failure
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
