package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlin.math.roundToInt

object StatusCard {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val tone = statusTone(state)
        val shape = RoundedCornerShape(18.dp)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            tone.color.copy(alpha = 0.18f),
                            AppColors.Panel,
                            AppColors.Blue.copy(alpha = 0.16f)
                        )
                    ),
                    shape = shape
                )
                .border(BorderStroke(1.dp, tone.color.copy(alpha = 0.20f)), shape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(tone)
                    Text(
                        text = (state.progress.coerceIn(0f, 1f) * 100).roundToInt().toString() + "%",
                        color = AppColors.Ink,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < 300.dp
                    if (compact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            ProgressRing(
                                progress = state.progress,
                                value = state.watchRemaining,
                                tone = tone,
                                size = 136
                            )
                            FinishBlock(state)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                progress = state.progress,
                                value = state.watchRemaining,
                                tone = tone,
                                modifier = Modifier.weight(0.95f),
                                size = 150
                            )
                            FinishBlock(
                                state = state,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                PrimaryActionsRow(
                    primaryLabel = state.primaryActionLabel,
                    secondaryLabel = state.secondaryActionLabel,
                    onPrimaryAction = onPrimaryAction,
                    onSecondaryAction = onSecondaryAction,
                    primaryColor = AppColors.Paper,
                    primaryContainerColor = tone.actionColor
                )
            }
        }
    }

    @Composable
    private fun StatusChip(tone: StatusTone) {
        Surface(
            color = tone.background,
            border = BorderStroke(1.dp, tone.color.copy(alpha = 0.16f)),
            shape = RoundedCornerShape(99.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tone.color)
                )
                Text(
                    text = tone.label,
                    color = tone.foreground,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun ProgressRing(
        progress: Float,
        value: String,
        tone: StatusTone,
        modifier: Modifier = Modifier,
        size: Int
    ) {
        Box(
            modifier = modifier.size(size.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size.dp)) {
                val strokeWidth = 16.dp.toPx()
                val diameter = this.size.minDimension - strokeWidth
                val topLeft = (this.size.minDimension - diameter) / 2f
                drawArc(
                    color = AppColors.SoftMuted,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(topLeft, topLeft),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = tone.progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(topLeft, topLeft),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    color = AppColors.Ink,
                    fontSize = 38.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Restzeit",
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun FinishBlock(state: TimeTrackerUiState, modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Text(
                text = "FEIERABEND",
                color = AppColors.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.2.sp
            )
            Text(
                text = state.endTime.removeSuffix(" Uhr"),
                color = AppColors.Ink,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = state.breakRequirementLabel,
                color = AppColors.Muted,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.workedTime + " gearbeitet",
                color = AppColors.Subtle,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }

    private data class StatusTone(
        val label: String,
        val color: Color,
        val foreground: Color,
        val background: Color,
        val progressColor: Color = color,
        val actionColor: Color = color
    )

    private fun statusTone(state: TimeTrackerUiState): StatusTone = when {
        state.primaryActionLabel.contains("Pause", ignoreCase = true) -> StatusTone(
            label = "Am Arbeiten",
            color = AppColors.Green,
            foreground = Color(0xFF096443),
            background = AppColors.Green.copy(alpha = 0.18f),
            actionColor = AppColors.Night,
            progressColor = AppColors.Green
        )
        state.primaryActionLabel.contains("Weiter", ignoreCase = true) -> StatusTone(
            label = "Pause",
            color = AppColors.Amber,
            foreground = Color(0xFF6A4B00),
            background = AppColors.Amber.copy(alpha = 0.32f),
            actionColor = AppColors.Night,
            progressColor = AppColors.Amber
        )
        state.primaryActionLabel.contains("Neu", ignoreCase = true) -> StatusTone(
            label = "Fertig",
            color = AppColors.Blue,
            foreground = Color(0xFF145EA7),
            background = AppColors.Blue.copy(alpha = 0.16f),
            actionColor = AppColors.Night
        )
        else -> StatusTone(
            label = "Bereit",
            color = AppColors.Coral,
            foreground = Color(0xFF8D2C25),
            background = AppColors.Coral.copy(alpha = 0.14f),
            progressColor = AppColors.Coral,
            actionColor = AppColors.Night
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
