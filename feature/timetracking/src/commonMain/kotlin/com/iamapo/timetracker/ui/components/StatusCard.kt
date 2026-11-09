package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.DayProgressSegmentUiModel
import com.iamapo.timetracker.presentation.state.DayScheduleUiKind
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.resources.*
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** The Dayflow summary: state hero, linear day progress and the two core actions. */
object StatusCard {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Hero(state)
            if (state.dayScheduleKind == DayScheduleUiKind.Workday) {
                DayProgress(state)
            }
            PrimaryActionsRow(
                primaryLabel = state.primaryActionLabel,
                secondaryLabel = state.secondaryActionLabel,
                onPrimaryAction = onPrimaryAction,
                onSecondaryAction = onSecondaryAction,
                secondaryColor = if (state.primaryCommand == TimeTrackingCommand.StartNewDay) {
                    AppColors.Success
                } else {
                    AppColors.Coral
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }

    @Composable
    private fun Hero(state: TimeTrackerUiState) {
        val tone = heroTone(state)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.nav_today),
                color = AppColors.Navy,
                fontFamily = FontFamily.SansSerif,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = state.dateLabel,
                color = AppColors.Navy.copy(alpha = 0.66f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            StatusPill(tone)
            Text(
                text = heroKicker(state),
                color = AppColors.Navy.copy(alpha = 0.66f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = heroValue(state),
                color = AppColors.Navy,
                fontFamily = FontFamily.SansSerif,
                fontSize = 56.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = heroSupportingText(state),
                color = AppColors.Navy,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                lineHeight = 20.sp
            )
        }
    }

    @Composable
    private fun StatusPill(tone: HeroTone) {
        Surface(
            color = tone.pillBackground,
            shape = RoundedCornerShape(99.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(tone.pillContent, CircleShape)
                )
                Text(
                    text = tone.label.uppercase(),
                    color = tone.pillContent,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }

    @Composable
    private fun DayProgress(state: TimeTrackerUiState) {
        val finished = state.primaryCommand == TimeTrackingCommand.StartNewDay
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.startTime,
                    color = AppColors.Navy,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(
                        if (finished) Res.string.finished_at else Res.string.now_time,
                        state.currentTime
                    ),
                    color = AppColors.Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.35f)
                )
                Text(
                    text = state.endTime.removeSuffix(" Uhr"),
                    color = AppColors.Navy,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            SegmentedProgress(
                segments = state.dayProgressSegments,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Legend(stringResource(Res.string.worked), state.workedTime, AppColors.Green)
                Legend(stringResource(Res.string.break_label), state.metrics.getOrNull(1)?.value.orEmpty(), AppColors.Sand)
                Legend(stringResource(Res.string.remaining), state.remainingTime, AppColors.CalendarLine)
            }
            Text(
                text = "${(state.progress.coerceIn(0f, 1f) * 100).roundToInt()} %",
                color = AppColors.Navy,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(Res.string.progress_target),
                color = AppColors.Navy.copy(alpha = 0.66f),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }

    @Composable
    private fun SegmentedProgress(segments: List<DayProgressSegmentUiModel>, modifier: Modifier = Modifier) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(99.dp))
        ) {
            drawRoundRect(AppColors.SoftMuted, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
            val separator = 2.dp.toPx()
            val minimumBreakWidth = 4.dp.toPx()
            fun drawSegments(kind: TimelineKind, color: Color, minimumWidth: Float) {
                var left = 0f
                segments.forEach { segment ->
                    val width = size.width * segment.fraction
                    if (segment.kind == kind) {
                        drawRect(
                            color,
                            topLeft = Offset(left, 0f),
                            size = androidx.compose.ui.geometry.Size(
                                maxOf(width - separator, minimumWidth),
                                size.height
                            )
                        )
                    }
                    left += width
                }
            }
            drawSegments(TimelineKind.Work, AppColors.Green, 0f)
            // Drawn last so that a short break stays visible on top of the work next to it.
            drawSegments(TimelineKind.Break, AppColors.Sand, minimumBreakWidth)
            val progress = segments.sumOf { it.fraction.toDouble() }.toFloat().coerceIn(0f, 1f)
            // An empty day has nothing to mark; drawing it anyway leaves a stray bar at the left edge.
            if (progress > 0f) {
                drawRect(
                    AppColors.Navy,
                    topLeft = Offset((size.width * progress - 1.dp.toPx()).coerceAtLeast(0f), 0f),
                    size = androidx.compose.ui.geometry.Size(2.dp.toPx(), size.height)
                )
            }
        }
    }

    @Composable
    private fun Legend(label: String, value: String, color: Color) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Text(
                text = "$label $value",
                color = AppColors.Navy.copy(alpha = 0.78f),
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }

    @Composable
    private fun heroKicker(state: TimeTrackerUiState): String = when (state.primaryCommand) {
        TimeTrackingCommand.StartDay -> if (state.dayScheduleKind == DayScheduleUiKind.DayOff) {
            stringResource(Res.string.today_day_off)
        } else {
            stringResource(Res.string.end_of_workday)
        }
        TimeTrackingCommand.ResumeWork -> state.statusLabel
        TimeTrackingCommand.StartNewDay -> stringResource(Res.string.worked)
        else -> stringResource(Res.string.end_of_workday)
    }

    @Composable
    private fun heroValue(state: TimeTrackerUiState): String = when (state.primaryCommand) {
        TimeTrackingCommand.StartDay -> if (state.dayScheduleKind == DayScheduleUiKind.DayOff) {
            stringResource(Res.string.day_off)
        } else {
            state.endTime.removeSuffix(" Uhr")
        }
        TimeTrackingCommand.StartNewDay -> state.workedTime
        else -> state.endTime.removeSuffix(" Uhr")
    }

    @Composable
    private fun heroSupportingText(state: TimeTrackerUiState): String = when (state.primaryCommand) {
        TimeTrackingCommand.StartDay -> if (state.dayScheduleKind == DayScheduleUiKind.DayOff) {
            stringResource(Res.string.no_workday_scheduled)
        } else {
            stringResource(Res.string.remaining_prefix, state.remainingTime)
        }
        TimeTrackingCommand.StartNewDay -> state.statusLabel
        TimeTrackingCommand.ResumeWork -> state.breakRequirementLabel
        else -> stringResource(Res.string.remaining_prefix, state.remainingTime)
    }

    @Composable
    private fun heroTone(state: TimeTrackerUiState): HeroTone = when {
        state.dayScheduleKind == DayScheduleUiKind.DayOff -> HeroTone(
            label = stringResource(Res.string.day_off),
            pillBackground = SuccessPillBackground,
            pillContent = AppColors.Success
        )
        state.primaryCommand == TimeTrackingCommand.ResumeWork -> HeroTone(
            label = stringResource(Res.string.state_break),
            pillBackground = AppColors.Sand,
            pillContent = AppColors.Navy
        )
        state.primaryCommand == TimeTrackingCommand.StartNewDay -> HeroTone(
            label = stringResource(Res.string.state_finished),
            pillBackground = SuccessPillBackground,
            pillContent = AppColors.Success
        )
        state.primaryCommand == TimeTrackingCommand.StartBreak -> HeroTone(
            label = stringResource(Res.string.status_working),
            pillBackground = SuccessPillBackground,
            pillContent = AppColors.Success
        )
        else -> HeroTone(
            label = stringResource(Res.string.state_ready),
            pillBackground = AppColors.SoftMuted,
            pillContent = AppColors.Navy
        )
    }

    private val SuccessPillBackground = Color(0xFFDFF6E9)

    private data class HeroTone(
        val label: String,
        val pillBackground: Color,
        val pillContent: Color
    )
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
