package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.WeekDayProgressUiModel
import com.iamapo.timetracker.presentation.state.WeekOverviewUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object WeekOverviewCard {
    @Composable
    operator fun invoke(
        week: WeekOverviewUiModel,
        modifier: Modifier = Modifier,
        showCarry: Boolean = false
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(18.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.padding(18.dp)) {
                val compact = maxWidth < 310.dp
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            WeekTotal(week)
                            BalancePill(week, showCarry)
                        }
                        WeekBars(week.days)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeekTotal(week)
                        BalancePill(week, showCarry)
                        WeekBars(week.days)
                    }
                }
            }
        }
    }

    @Composable
    private fun WeekTotal(week: WeekOverviewUiModel) {
        Column {
            Text(
                text = "DIESE WOCHE",
                color = AppColors.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.2.sp
            )
            Text(
                text = week.reached,
                color = AppColors.Ink,
                fontSize = 31.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }

    @Composable
    private fun BalancePill(week: WeekOverviewUiModel, showCarry: Boolean) {
        val displaysCarry = showCarry && week.carry != null
        val value = if (displaysCarry) week.carry.orEmpty() else week.balance
        val isPositive = if (displaysCarry) week.isPositiveCarry else week.isPositiveBalance
        val color = if (isPositive) AppColors.Lemon else AppColors.Coral
        Surface(
            color = color.copy(alpha = if (isPositive) 0.42f else 0.24f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.30f)),
            shape = RoundedCornerShape(99.dp)
        ) {
            if (displaysCarry) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        color = AppColors.Ink,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = value,
                    color = AppColors.Ink,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    @Composable
    private fun WeekBars(days: List<WeekDayProgressUiModel>) {
        Row(
            modifier = Modifier
                .width(82.dp)
                .height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(day.progress.coerceIn(0.08f, 1f))
                        .clip(
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 2.dp,
                                bottomEnd = 2.dp
                            )
                        )
                        .background(barColor(day))
                )
            }
        }
    }

    private fun barColor(day: WeekDayProgressUiModel): Color =
        if (day.isToday) {
            AppColors.Coral
        } else {
            AppColors.Ink.copy(alpha = 0.16f)
        }
}

@Preview
@Composable
private fun WeekOverviewCardPreview() {
    TimeTrackerTheme {
        WeekOverviewCard(TimeTrackerPreviewData.uiState.weekOverview)
    }
}

@Preview(name = "Wochenübertrag im Kalendereditor")
@Composable
private fun WeekOverviewCardWithCarryPreview() {
    TimeTrackerTheme {
        WeekOverviewCard(
            week = TimeTrackerPreviewData.uiState.weekOverview.copy(
                carry = "+30 min",
                isPositiveCarry = true
            ),
            showCarry = true
        )
    }
}
