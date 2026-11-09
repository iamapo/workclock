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
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.DayProgressSegmentUiModel
import com.iamapo.timetracker.presentation.state.DayScheduleUiKind
import com.iamapo.timetracker.presentation.state.TimelineKind
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.resources.*
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes
import com.iamapo.timetracker.ui.theme.LedgerMonospace
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
                modifier = Modifier.padding(horizontal = AppDimensions.size24, vertical = AppDimensions.size16)
            )
        }
    }

    @Composable
    private fun Hero(state: TimeTrackerUiState) {
        val tone = heroTone(state)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppDimensions.size24,
                    top = AppDimensions.size24,
                    end = AppDimensions.size24,
                    bottom = AppDimensions.size16
                )
        ) {
            Text(
                text = stringResource(Res.string.nav_today),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size40,
                lineHeight = AppFontSizes.size44,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = state.dateLabel,
                color = AppColors.Subtle,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size16,
                lineHeight = AppFontSizes.size20,
                modifier = Modifier.padding(top = AppDimensions.size4)
            )
            StatusPill(tone)
            Text(
                text = heroKicker(state),
                color = AppColors.Subtle,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size13,
                lineHeight = AppFontSizes.size16,
                modifier = Modifier.padding(top = AppDimensions.size16)
            )
            Text(
                text = heroValue(state),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size48,
                lineHeight = AppFontSizes.size52,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = heroSupportingText(state),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size16,
                lineHeight = AppFontSizes.size20
            )
        }
    }

    @Composable
    private fun StatusPill(tone: HeroTone) {
        Surface(
            color = tone.pillBackground,
            shape = RoundedCornerShape(AppDimensions.size99),
            modifier = Modifier.padding(top = AppDimensions.size8)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppDimensions.size12, vertical = AppDimensions.size6),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(AppDimensions.size8)
                        .background(tone.pillContent, CircleShape)
                )
                Text(
                    text = tone.label,
                    color = tone.pillContent,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = AppFontSizes.size12,
                    lineHeight = AppFontSizes.size14,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun DayProgress(state: TimeTrackerUiState) {
        val finished = state.primaryCommand == TimeTrackingCommand.StartNewDay
        Column(
            modifier = Modifier.padding(horizontal = AppDimensions.size24, vertical = AppDimensions.size12),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.startTime,
                    color = AppColors.Ink,
                    fontFamily = LedgerMonospace,
                    fontSize = AppFontSizes.size13,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(
                        if (finished) Res.string.finished_at else Res.string.now_time,
                        state.currentTime
                    ),
                    color = AppColors.Ink,
                    fontFamily = LedgerMonospace,
                    fontSize = AppFontSizes.size14,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.35f)
                )
                Text(
                    text = state.endTime.removeSuffix(" Uhr"),
                    color = AppColors.Ink,
                    fontFamily = LedgerMonospace,
                    fontSize = AppFontSizes.size13,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            SegmentedProgress(
                segments = state.dayProgressSegments,
                modifier = Modifier.padding(top = AppDimensions.size8)
            )
            // Three equal columns with a fixed gap. SpaceBetween let the long German
            // labels run into the next entry's dot once they no longer fit.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimensions.size8),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
            ) {
                Legend(stringResource(Res.string.worked), state.workedTime, AppColors.Green, Modifier.weight(1f))
                Legend(
                    stringResource(Res.string.break_label),
                    state.metrics.getOrNull(1)?.value.orEmpty(),
                    AppColors.Sand,
                    Modifier.weight(1f)
                )
                Legend(stringResource(Res.string.remaining), state.remainingTime, AppColors.CalendarLine, Modifier.weight(1f))
            }
            Text(
                text = "${(state.progress.coerceIn(0f, 1f) * 100).roundToInt()} %",
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size24,
                lineHeight = AppFontSizes.size28,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = AppDimensions.size8)
            )
            Text(
                text = stringResource(Res.string.progress_target),
                color = AppColors.Subtle,
                fontSize = AppFontSizes.size14,
                lineHeight = AppFontSizes.size18
            )
        }
    }

    @Composable
    private fun SegmentedProgress(segments: List<DayProgressSegmentUiModel>, modifier: Modifier = Modifier) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(AppDimensions.size32)
                .clip(RoundedCornerShape(AppDimensions.size99))
        ) {
            drawRoundRect(AppColors.SoftMuted, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
            val separator = AppDimensions.size2.toPx()
            val minimumBreakWidth = AppDimensions.size4.toPx()
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
                    AppColors.Ink,
                    topLeft = Offset((size.width * progress - AppDimensions.size1.toPx()).coerceAtLeast(0f), 0f),
                    size = androidx.compose.ui.geometry.Size(AppDimensions.size2.toPx(), size.height)
                )
            }
        }
    }

    @Composable
    private fun Legend(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size4),
            verticalAlignment = Alignment.Top
        ) {
            // Centred on the first line, so the dot keeps its place when the label wraps.
            Box(
                Modifier
                    .padding(top = AppDimensions.size3)
                    .size(AppDimensions.size8)
                    .background(color, CircleShape)
            )
            Text(
                text = "$label $value",
                color = AppColors.Muted,
                fontSize = AppFontSizes.size11,
                lineHeight = AppFontSizes.size14
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
            pillContent = AppColors.Ink
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
            pillContent = AppColors.Ink
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
