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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.LedgerMonospace
import com.iamapo.timetracker.ui.theme.LedgerShapes
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.ui.ComposePreviewContext
import androidx.compose.ui.graphics.graphicsLayer
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
        val shape = LedgerShapes.Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(color = AppColors.Panel, shape = shape)
                .border(BorderStroke(AppDimensions.size1, AppColors.Line), shape)
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
            color = Color.Transparent,
            border = BorderStroke(AppDimensions.size2, tone.color),
            shape = LedgerShapes.CardSmall,
            modifier = Modifier.graphicsLayer(rotationZ = -3f)
        ) {
            Text(
                text = tone.label.uppercase(),
                color = tone.color,
                fontSize = AppFontSizes.size12,
                lineHeight = AppFontSizes.size14,
                fontWeight = FontWeight.Black,
                fontFamily = LedgerMonospace,
                letterSpacing = AppFontSizes.size0_2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = AppDimensions.size10, vertical = AppDimensions.size6)
            )
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
        val progressColor: Color = color,
        val actionColor: Color = color
    )

    @Composable
    private fun statusTone(state: TimeTrackerUiState): StatusTone = when (state.primaryCommand) {
        TimeTrackingCommand.StartBreak -> StatusTone(
            label = stringResource(Res.string.status_working),
            color = AppColors.Purple,
            actionColor = AppColors.Night,
            progressColor = AppColors.Purple
        )
        TimeTrackingCommand.ResumeWork -> StatusTone(
            label = stringResource(Res.string.state_break),
            color = AppColors.Amber,
            actionColor = AppColors.Night,
            progressColor = AppColors.Amber
        )
        TimeTrackingCommand.StartNewDay -> StatusTone(
            label = stringResource(Res.string.state_finished),
            color = AppColors.Blue,
            actionColor = AppColors.Night
        )
        else -> StatusTone(
            label = stringResource(Res.string.state_ready),
            color = AppColors.Green,
            progressColor = AppColors.Green,
            actionColor = AppColors.Night
        )
    }
}

@Preview
@Composable
private fun StatusCardPreview() {
    ComposePreviewContext()
    TimeTrackerTheme {
        StatusCard(
            state = TimeTrackerPreviewData.uiState(),
            onPrimaryAction = {},
            onSecondaryAction = {}
        )
    }
}
