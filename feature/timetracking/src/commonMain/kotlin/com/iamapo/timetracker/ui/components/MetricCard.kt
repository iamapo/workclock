package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.state.MetricUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

object MetricCard {
    @Composable
    operator fun invoke(metric: MetricUiModel, modifier: Modifier = Modifier) {
        val tone = toneFor(metric)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(AppDimensions.size112),
            color = tone.background,
            border = BorderStroke(AppDimensions.size1, tone.border),
            shape = RoundedCornerShape(AppDimensions.size14)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AppDimensions.size14, vertical = AppDimensions.size12),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = metric.label.uppercase(),
                    color = tone.content.copy(alpha = 0.70f),
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_2
                )
                Text(
                    text = metric.value,
                    color = tone.content,
                    fontSize = AppFontSizes.size22,
                    lineHeight = AppFontSizes.size24,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = AppDimensions.size8)
                )
                Text(
                    text = metric.hint,
                    color = tone.content.copy(alpha = 0.72f),
                    fontSize = AppFontSizes.size11,
                    lineHeight = AppFontSizes.size13,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AppDimensions.size5)
                )
            }
        }
    }

    private data class MetricTone(
        val background: Color,
        val border: Color,
        val content: Color = AppColors.Ink
    )

    @Composable
    private fun toneFor(metric: MetricUiModel): MetricTone = when (metric.label) {
        stringResource(Res.string.worked) -> MetricTone(
            background = AppColors.Green,
            border = AppColors.Green.copy(alpha = 0.24f)
        )
        stringResource(Res.string.break_label) -> MetricTone(
            background = AppColors.Lemon,
            border = AppColors.Lemon.copy(alpha = 0.32f)
        )
        stringResource(Res.string.week) -> MetricTone(
            background = AppColors.Purple,
            border = AppColors.Purple.copy(alpha = 0.24f),
            content = AppColors.Paper
        )
        else -> if (metric.emphasized) MetricTone(
            background = AppColors.Blue,
            border = AppColors.Blue.copy(alpha = 0.24f)
        )
        else MetricTone(
            background = AppColors.Panel,
            border = AppColors.Line
        )
    }
}

@Preview
@Composable
private fun MetricCardPreview() {
    TimeTrackerTheme {
        MetricCard(
            MetricUiModel(
                label = "Rest heute",
                value = "2 h 48 min",
                hint = "bis 17:21",
                emphasized = true
            )
        )
    }
}
