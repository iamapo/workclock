package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

object WatchCompanionCard {
    @Composable
    operator fun invoke(
        state: String,
        remaining: String,
        caption: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.PanelRaised,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Row(
                modifier = Modifier.padding(AppDimensions.size16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.size16)
            ) {
                Surface(
                    modifier = Modifier
                        .width(AppDimensions.size54)
                        .height(AppDimensions.size62),
                    color = androidx.compose.ui.graphics.Color.Black,
                    border = BorderStroke(AppDimensions.size1, AppColors.LineStrong.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(AppDimensions.size15)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = AppDimensions.size4, vertical = AppDimensions.size8),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.uppercase(),
                            color = stateColor(state),
                            fontSize = AppFontSizes.size6,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = AppFontSizes.size0_5
                        )
                        Text(
                            text = remaining,
                            color = AppColors.Ink,
                            fontSize = AppFontSizes.size13,
                            lineHeight = AppFontSizes.size15,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = AppDimensions.size3)
                        )
                        Text(
                            text = stringResource(Res.string.remaining),
                            color = AppColors.Subtle,
                            fontSize = AppFontSizes.size5,
                            lineHeight = AppFontSizes.size6
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.size4)
                ) {
                    Text(
                        text = stringResource(Res.string.watch_companion),
                        color = AppColors.Ink,
                        fontSize = AppFontSizes.size12,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = caption,
                        color = AppColors.Muted,
                        fontSize = AppFontSizes.size11,
                        lineHeight = AppFontSizes.size14
                    )
                    Row(
                        modifier = Modifier.padding(top = AppDimensions.size4),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.size6)
                    ) {
                        WatchPill("watchOS", stateColor(state), stateColor(state).copy(alpha = 0.12f))
                        WatchPill(stringResource(Res.string.complication), AppColors.Subtle, AppColors.SoftMuted)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)) {
                    WatchActionButton(if (state == stringResource(Res.string.state_break)) "▶" else "II")
                    WatchActionButton("■")
                }
            }
        }
    }

    @Composable
    private fun WatchPill(label: String, color: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color) {
        Surface(
            color = background,
            shape = RoundedCornerShape(AppDimensions.size99),
            border = BorderStroke(AppDimensions.size1, color.copy(alpha = 0.18f))
        ) {
            Text(
                text = label,
                color = color,
                fontSize = AppFontSizes.size10,
                lineHeight = AppFontSizes.size12,
                modifier = Modifier.padding(horizontal = AppDimensions.size8, vertical = AppDimensions.size3)
            )
        }
    }

    @Composable
    private fun stateColor(state: String): androidx.compose.ui.graphics.Color = when (state) {
        stringResource(Res.string.state_break) -> AppColors.Amber
        stringResource(Res.string.state_ready) -> AppColors.Subtle
        stringResource(Res.string.state_finished) -> AppColors.Blue
        else -> AppColors.Green
    }
}

@Preview
@Composable
private fun WatchCompanionCardPreview() {
    TimeTrackerTheme {
        WatchCompanionCard(
            state = "Aktiv",
            remaining = "2:48",
            caption = "noch bis 17:21"
        )
    }
}
