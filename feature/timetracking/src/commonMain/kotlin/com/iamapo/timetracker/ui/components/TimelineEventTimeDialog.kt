package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.iamapo.timetracker.presentation.state.TimelineEditUiModel
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes
import com.iamapo.timetracker.ui.theme.LedgerMonospace
import com.iamapo.timetracker.ui.theme.LedgerShapes
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

/**
 * Corrects the time of a single recorded event, for example a break that was
 * started or ended later than it actually happened.
 */
object TimelineEventTimeDialog {
    @Composable
    operator fun invoke(
        item: TimelineItemUiModel,
        edit: TimelineEditUiModel,
        onDismiss: () -> Unit,
        onSave: (Int) -> Unit
    ) {
        var value by remember(edit.eventIndex) { mutableStateOf(formatClock(edit.minuteOfDay)) }
        val minute = parseClock(value)
        val valid = minute != null && minute in edit.earliestMinute..edit.latestMinute

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                color = AppColors.Panel,
                border = BorderStroke(AppDimensions.size1, AppColors.Line),
                shape = LedgerShapes.Card
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.size20),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
                ) {
                    Text(
                        text = stringResource(Res.string.edit_time),
                        color = AppColors.Ink,
                        fontSize = AppFontSizes.size24,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = localizedEventTitle(item.title),
                        color = AppColors.Muted,
                        fontSize = AppFontSizes.size13,
                        fontFamily = LedgerMonospace
                    )
                    Text(
                        text = stringResource(
                            Res.string.edit_time_range,
                            formatClock(edit.earliestMinute),
                            formatClock(edit.latestMinute)
                        ),
                        color = AppColors.Subtle,
                        fontSize = AppFontSizes.size12,
                        fontFamily = LedgerMonospace
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = normalizeTimeInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(localizedEventTitle(item.title)) },
                        singleLine = true,
                        isError = !valid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = LedgerShapes.CardSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Blue,
                            focusedLabelColor = AppColors.Blue,
                            cursorColor = AppColors.Blue
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(Res.string.cancel),
                                color = AppColors.Blue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(AppDimensions.size10))
                        Button(
                            onClick = { onSave(minute!!) },
                            enabled = valid,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue),
                            shape = LedgerShapes.CardSmall
                        ) {
                            Text(stringResource(Res.string.save), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    private fun formatClock(minutes: Int): String =
        "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"

    private fun parseClock(value: String): Int? {
        val parts = value.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return if (hour in 0..23 && minute in 0..59) hour * 60 + minute else null
    }

    private fun normalizeTimeInput(value: String): String =
        value.filter { it.isDigit() || it == ':' }.take(5)
}

@Preview
@Composable
private fun TimelineEventTimeDialogPreview() {
    ComposePreviewContext()
    TimeTrackerTheme {
        TimelineEventTimeDialog(
            item = TimelineItemUiModel(
                time = "12:02",
                title = "Pause gestartet",
                kind = TimelineKind.Break
            ),
            edit = TimelineEditUiModel(
                eventIndex = 1,
                minuteOfDay = 12 * 60 + 2,
                earliestMinute = 8 * 60 + 42,
                latestMinute = 14 * 60 + 33
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}
