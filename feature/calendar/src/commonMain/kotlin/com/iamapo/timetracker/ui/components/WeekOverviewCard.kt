package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

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
import com.iamapo.timetracker.presentation.state.WeekDayProgressUiModel
import com.iamapo.timetracker.presentation.state.WeekOverviewUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.this_week

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
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            BoxWithConstraints(modifier = Modifier.padding(AppDimensions.size18)) {
                val compact = maxWidth < AppDimensions.size310
                if (compact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
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
                text = stringResource(Res.string.this_week),
                color = AppColors.Muted,
                fontSize = AppFontSizes.size11,
                fontWeight = FontWeight.Black,
                letterSpacing = AppFontSizes.size0_2
            )
            Text(
                text = week.reached,
                color = AppColors.Ink,
                fontSize = AppFontSizes.size31,
                lineHeight = AppFontSizes.size34,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = AppDimensions.size5)
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
            border = BorderStroke(AppDimensions.size1, color.copy(alpha = 0.30f)),
            shape = RoundedCornerShape(AppDimensions.size99)
        ) {
            if (displaysCarry) {
                Column(
                    modifier = Modifier.padding(horizontal = AppDimensions.size12, vertical = AppDimensions.size7),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        color = AppColors.Ink,
                        fontSize = AppFontSizes.size13,
                        lineHeight = AppFontSizes.size15,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = value,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size13,
                    lineHeight = AppFontSizes.size15,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = AppDimensions.size12, vertical = AppDimensions.size8)
                )
            }
        }
    }

    @Composable
    private fun WeekBars(days: List<WeekDayProgressUiModel>) {
        Row(
            modifier = Modifier
                .width(AppDimensions.size82)
                .height(AppDimensions.size72),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size5),
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(day.progress.coerceIn(0.08f, 1f))
                        .clip(
                            RoundedCornerShape(
                                topStart = AppDimensions.size8,
                                topEnd = AppDimensions.size8,
                                bottomStart = AppDimensions.size2,
                                bottomEnd = AppDimensions.size2
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
