package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.domain.TimeTrackingCommand
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.resources.*
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** The Dayflow summary: expressive state hero, linear day progress and the two core actions. */
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
            DayProgress(state)
            PrimaryActionsRow(
                primaryLabel = state.primaryActionLabel,
                secondaryLabel = state.secondaryActionLabel,
                onPrimaryAction = onPrimaryAction,
                onSecondaryAction = onSecondaryAction,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }

    @Composable
    private fun Hero(state: TimeTrackerUiState) {
        val tone = heroTone(state.primaryCommand)
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(304.dp + statusBarHeight)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - 42.dp.toPx())
                    cubicTo(
                        size.width * 0.72f,
                        size.height - 18.dp.toPx(),
                        size.width * 0.34f,
                        size.height - 2.dp.toPx(),
                        0f,
                        size.height
                    )
                    close()
                }
                drawPath(path, tone.background)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(start = 26.dp, top = 22.dp, end = 26.dp, bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(Res.string.nav_today),
                    color = AppColors.Navy,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = state.dateLabel,
                    color = AppColors.Navy.copy(alpha = 0.66f),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                StatusPill(tone)
                Text(
                    text = heroKicker(state).uppercase(),
                    color = tone.heroContent,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.9.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = heroValue(state),
                    color = tone.heroContent,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 64.sp,
                    lineHeight = 66.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = heroSupportingText(state),
                    color = tone.heroContent,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun StatusPill(tone: HeroTone) {
        Surface(
            color = tone.pillBackground,
            shape = RoundedCornerShape(99.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
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
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.7.sp
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
                    fontWeight = FontWeight.Black,
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
                worked = state.workdayProgress,
                paused = state.breakProgress,
                modifier = Modifier.padding(top = 9.dp)
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
                color = AppColors.Success,
                fontSize = 27.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = stringResource(Res.string.progress_target),
                color = AppColors.Success,
                fontSize = 14.sp,
                lineHeight = 17.sp
            )
        }
    }

    @Composable
    private fun SegmentedProgress(worked: Float, paused: Float, modifier: Modifier = Modifier) {
        val workedWidth = worked.coerceIn(0f, 1f)
        val pauseWidth = paused.coerceIn(0f, 1f - workedWidth)
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(99.dp))
        ) {
            drawRoundRect(AppColors.SoftMuted, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
            drawRect(AppColors.Green, size = androidx.compose.ui.geometry.Size(size.width * workedWidth, size.height))
            drawRect(
                AppColors.Sand,
                topLeft = Offset(size.width * workedWidth, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * pauseWidth, size.height)
            )
            val markerX = size.width * (workedWidth + pauseWidth).coerceIn(0f, 1f)
            drawRect(
                AppColors.Navy,
                topLeft = Offset((markerX - 1.dp.toPx()).coerceAtLeast(0f), 0f),
                size = androidx.compose.ui.geometry.Size(2.dp.toPx(), size.height)
            )
        }
    }

    @Composable
    private fun Legend(label: String, value: String, color: Color) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(9.dp).background(color, CircleShape))
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
        TimeTrackingCommand.ResumeWork -> state.statusLabel
        TimeTrackingCommand.StartNewDay -> stringResource(Res.string.worked)
        else -> stringResource(Res.string.end_of_workday)
    }

    @Composable
    private fun heroValue(state: TimeTrackerUiState): String = when (state.primaryCommand) {
        TimeTrackingCommand.StartNewDay -> state.workedTime
        else -> state.endTime.removeSuffix(" Uhr")
    }

    @Composable
    private fun heroSupportingText(state: TimeTrackerUiState): String = when (state.primaryCommand) {
        TimeTrackingCommand.StartNewDay -> state.statusLabel
        TimeTrackingCommand.ResumeWork -> state.breakRequirementLabel
        else -> stringResource(Res.string.remaining_prefix, state.remainingTime)
    }

    @Composable
    private fun heroTone(command: TimeTrackingCommand): HeroTone = when (command) {
        TimeTrackingCommand.ResumeWork -> HeroTone(
            label = stringResource(Res.string.state_break),
            background = AppColors.Sand,
            heroContent = AppColors.Navy,
            pillBackground = AppColors.Background.copy(alpha = 0.78f),
            pillContent = AppColors.Navy
        )
        TimeTrackingCommand.StartNewDay -> HeroTone(
            label = stringResource(Res.string.state_finished),
            background = AppColors.Green,
            heroContent = AppColors.Navy,
            pillBackground = AppColors.Background.copy(alpha = 0.78f),
            pillContent = AppColors.Success
        )
        TimeTrackingCommand.StartBreak -> HeroTone(
            label = stringResource(Res.string.status_working),
            background = AppColors.Coral,
            pillBackground = Color(0xFFDFF6E9),
            pillContent = AppColors.Success
        )
        else -> HeroTone(
            label = stringResource(Res.string.state_ready),
            background = AppColors.Coral,
            pillBackground = AppColors.Background.copy(alpha = 0.84f),
            pillContent = AppColors.Navy
        )
    }

    private data class HeroTone(
        val label: String,
        val background: Color,
        val heroContent: Color = Color.White,
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
