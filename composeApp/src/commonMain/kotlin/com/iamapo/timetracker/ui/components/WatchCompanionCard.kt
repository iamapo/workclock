package com.iamapo.timetracker.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors

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
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .width(54.dp)
                        .height(62.dp),
                    color = androidx.compose.ui.graphics.Color.Black,
                    border = BorderStroke(1.dp, AppColors.LineStrong.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.uppercase(),
                            color = stateColor(state),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = remaining,
                            color = AppColors.Ink,
                            fontSize = 13.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Text(
                            text = "verbleibend",
                            color = AppColors.Subtle,
                            fontSize = 5.sp,
                            lineHeight = 6.sp
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Apple Watch Companion",
                        color = AppColors.Ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = caption,
                        color = AppColors.Muted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WatchPill("watchOS", stateColor(state), stateColor(state).copy(alpha = 0.12f))
                        WatchPill("Komplikation", AppColors.Subtle, AppColors.SoftMuted)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatchActionButton(if (state.contains("pause", ignoreCase = true)) "▶" else "II")
                    WatchActionButton("■")
                }
            }
        }
    }

    @Composable
    private fun WatchPill(label: String, color: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color) {
        Surface(
            color = background,
            shape = RoundedCornerShape(99.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }

    private fun stateColor(state: String): androidx.compose.ui.graphics.Color = when {
        state.contains("pause", ignoreCase = true) -> AppColors.Amber
        state.contains("bereit", ignoreCase = true) -> AppColors.Subtle
        state.contains("beendet", ignoreCase = true) -> AppColors.Blue
        else -> AppColors.Green
    }
}
