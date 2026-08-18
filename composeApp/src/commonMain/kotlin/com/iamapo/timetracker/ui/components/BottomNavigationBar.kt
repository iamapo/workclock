package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.nav_calendar
import com.iamapo.timetracker.resources.nav_settings
import com.iamapo.timetracker.resources.nav_today
import com.iamapo.timetracker.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class MainTab {
    Today,
    Calendar,
    Settings
}

object BottomNavigationBar {
    @Composable
    operator fun invoke(
        selectedTab: MainTab,
        onSelectTab: (MainTab) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(AppColors.Background)
                .drawBehind {
                    drawLine(
                        color = AppColors.CalendarLine,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                BottomNavigationItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    private fun BottomNavigationItem(
        tab: MainTab,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(98.dp)
                        .height(32.dp)
                        .background(
                            color = if (selected) AppColors.Green.copy(alpha = 0.17f) else Color.Transparent,
                            shape = RoundedCornerShape(99.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationIcon(
                        tab = tab,
                        color = if (selected) AppColors.Coral else AppColors.Navy
                    )
                }
                Text(
                    text = stringResource(
                        when (tab) {
                            MainTab.Today -> Res.string.nav_today
                            MainTab.Calendar -> Res.string.nav_calendar
                            MainTab.Settings -> Res.string.nav_settings
                        }
                    ),
                    color = if (selected) AppColors.Coral else AppColors.Navy,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    @Composable
    private fun NavigationIcon(tab: MainTab, color: Color) {
        Canvas(Modifier.size(24.dp)) {
            val strokeWidth = 1.8.dp.toPx()
            when (tab) {
                MainTab.Today -> {
                    drawCircle(
                        color = color,
                        radius = size.minDimension * 0.39f,
                        center = center,
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = color,
                        start = center,
                        end = Offset(center.x, center.y - size.height * 0.22f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = center,
                        end = Offset(center.x + size.width * 0.18f, center.y + size.height * 0.12f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                MainTab.Calendar -> {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(size.width * 0.14f, size.height * 0.18f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.72f, size.height * 0.68f),
                        cornerRadius = CornerRadius(size.width * 0.08f),
                        style = Stroke(strokeWidth)
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.14f, size.height * 0.38f),
                        end = Offset(size.width * 0.86f, size.height * 0.38f),
                        strokeWidth = strokeWidth
                    )
                    listOf(0.34f, 0.66f).forEach { x ->
                        drawLine(
                            color = color,
                            start = Offset(size.width * x, size.height * 0.10f),
                            end = Offset(size.width * x, size.height * 0.27f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                MainTab.Settings -> {
                    val path = Path()
                    repeat(16) { index ->
                        val angle = -PI / 2 + index * PI / 8
                        val radius = if (index % 2 == 0) size.minDimension * 0.43f else size.minDimension * 0.34f
                        val point = Offset(
                            x = center.x + (cos(angle) * radius).toFloat(),
                            y = center.y + (sin(angle) * radius).toFloat()
                        )
                        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                    }
                    path.close()
                    drawPath(path, color, style = Stroke(strokeWidth, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    drawCircle(
                        color = color,
                        radius = size.minDimension * 0.13f,
                        center = center,
                        style = Stroke(strokeWidth)
                    )
                }
            }
        }
    }
}
