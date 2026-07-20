package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*
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
        val shape = RoundedCornerShape(AppDimensions.size18)
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
                .border(BorderStroke(AppDimensions.size1, tone.color.copy(alpha = 0.20f)), shape)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size16)
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
                        fontSize = AppFontSizes.size22,
                        fontWeight = FontWeight.Black
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compact = maxWidth < AppDimensions.size300
                    if (compact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppDimensions.size18)
                        ) {
                            ProgressRing(
                                progress = state.progress,
                                value = state.watchRemaining,
                                tone = tone,
                                size = AppDimensions.size136
                            )
                            FinishBlock(state)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size18),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                progress = state.progress,
                                value = state.watchRemaining,
                                tone = tone,
                                modifier = Modifier.weight(0.95f),
                                size = AppDimensions.size150
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
            border = BorderStroke(AppDimensions.size1, tone.color.copy(alpha = 0.16f)),
            shape = RoundedCornerShape(AppDimensions.size99)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppDimensions.size12, vertical = AppDimensions.size7),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.size8)
                        .clip(CircleShape)
                        .background(tone.color)
                )
                Text(
                    text = tone.label,
                    color = tone.foreground,
                    fontSize = AppFontSizes.size13,
                    lineHeight = AppFontSizes.size15,
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
        size: Dp
    ) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = AppDimensions.size16.toPx()
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
                    fontSize = AppFontSizes.size38,
                    lineHeight = AppFontSizes.size40,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.remaining_time),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size12,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun FinishBlock(state: TimeTrackerUiState, modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Text(
                text = stringResource(Res.string.end_of_workday),
                color = AppColors.Muted,
                fontSize = AppFontSizes.size11,
                fontWeight = FontWeight.Black,
                letterSpacing = AppFontSizes.size0_2
            )
            Text(
                text = state.endTime.removeSuffix(" Uhr"),
                color = AppColors.Ink,
                fontSize = AppFontSizes.size42,
                lineHeight = AppFontSizes.size44,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = state.breakRequirementLabel,
                color = AppColors.Muted,
                fontSize = AppFontSizes.size13,
                lineHeight = AppFontSizes.size16,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(Res.string.worked_suffix, state.workedTime),
                color = AppColors.Subtle,
                fontSize = AppFontSizes.size12,
                lineHeight = AppFontSizes.size15,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = AppDimensions.size7)
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

    @Composable
    private fun statusTone(state: TimeTrackerUiState): StatusTone = when (state.watchState) {
        stringResource(Res.string.state_active) -> StatusTone(
            label = stringResource(Res.string.status_working),
            color = AppColors.Green,
            foreground = Color(0xFF096443),
            background = AppColors.Green.copy(alpha = 0.18f),
            actionColor = AppColors.Night,
            progressColor = AppColors.Green
        )
        stringResource(Res.string.state_break) -> StatusTone(
            label = stringResource(Res.string.state_break),
            color = AppColors.Amber,
            foreground = Color(0xFF6A4B00),
            background = AppColors.Amber.copy(alpha = 0.32f),
            actionColor = AppColors.Night,
            progressColor = AppColors.Amber
        )
        stringResource(Res.string.state_finished) -> StatusTone(
            label = stringResource(Res.string.state_finished),
            color = AppColors.Blue,
            foreground = Color(0xFF145EA7),
            background = AppColors.Blue.copy(alpha = 0.16f),
            actionColor = AppColors.Night
        )
        else -> StatusTone(
            label = stringResource(Res.string.state_ready),
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
