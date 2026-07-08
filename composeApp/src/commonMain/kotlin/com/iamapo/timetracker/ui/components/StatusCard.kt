package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object StatusCard {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val tone = statusTone(state)
        Column(modifier = modifier.fillMaxWidth()) {
            Surface(
                color = tone.background,
                border = BorderStroke(1.dp, tone.color.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(99.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(tone.color)
                        )
                    }
                    Text(
                        text = tone.label,
                        color = tone.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = state.workedTime,
                        color = AppColors.Ink.copy(alpha = if (state.progress == 0f) 0.28f else 1f),
                        fontSize = if (state.progress == 0f) 42.sp else 44.sp,
                        lineHeight = 46.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.6).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "GEARBEITET",
                        color = AppColors.Subtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    HeroMetric(
                        label = "RESTZEIT",
                        value = state.remainingTime,
                        accent = if (state.remainingTime == "0 min") AppColors.Green else AppColors.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 42.dp)
                            .background(AppColors.Line)
                    )
                    HeroMetric(
                        label = "FEIERABEND",
                        value = state.endTime.removeSuffix(" Uhr"),
                        modifier = Modifier.weight(1f)
                    )
                }

                TimeProgressBar(
                    progress = state.progress,
                    color = tone.progressColor
                )
                PrimaryActionsRow(
                    primaryLabel = state.primaryActionLabel,
                    secondaryLabel = state.secondaryActionLabel,
                    onPrimaryAction = onPrimaryAction,
                    onSecondaryAction = onSecondaryAction,
                    primaryColor = tone.actionColor,
                    primaryContainerColor = tone.actionColor.copy(alpha = 0.10f)
                )
            }
        }
    }

    @Composable
    private fun HeroMetric(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        accent: Color = AppColors.Ink
    ) {
        Column(modifier = modifier) {
            Text(
                text = label,
                color = AppColors.Subtle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = accent,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }

    private data class StatusTone(
        val label: String,
        val color: Color,
        val background: Color,
        val progressColor: Color = color,
        val actionColor: Color = color
    )

    private fun statusTone(state: TimeTrackerUiState): StatusTone = when {
        state.primaryActionLabel.contains("Pause", ignoreCase = true) -> StatusTone(
            label = "AKTIV",
            color = AppColors.Green,
            background = AppColors.Green.copy(alpha = 0.10f),
            actionColor = AppColors.Amber,
            progressColor = AppColors.Green
        )
        state.primaryActionLabel.contains("Weiter", ignoreCase = true) -> StatusTone(
            label = "PAUSE",
            color = AppColors.Amber,
            background = AppColors.Amber.copy(alpha = 0.09f)
        )
        state.primaryActionLabel.contains("Neu", ignoreCase = true) -> StatusTone(
            label = "BEENDET",
            color = AppColors.Blue,
            background = AppColors.Blue.copy(alpha = 0.10f)
        )
        else -> StatusTone(
            label = "NICHT GESTARTET",
            color = AppColors.Subtle,
            background = AppColors.Subtle.copy(alpha = 0.15f),
            progressColor = AppColors.Subtle,
            actionColor = AppColors.Green
        )
    }
}

@Preview
@Composable
private fun StatusCardPreview() {
    TimeTrackerTheme {
        StatusCard(
            state = TimeTrackerPreviewData.uiState,
            onPrimaryAction = {},
            onSecondaryAction = {}
        )
    }
}
