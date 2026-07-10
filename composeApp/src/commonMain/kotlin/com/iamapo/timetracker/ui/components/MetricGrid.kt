package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.MetricUiModel
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object MetricGrid {
    @Composable
    operator fun invoke(metrics: List<MetricUiModel>, modifier: Modifier = Modifier) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val useSingleRow = metrics.size <= 3 && maxWidth > AppDimensions.size330
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size8)
            ) {
                val rows = if (useSingleRow) listOf(metrics) else metrics.chunked(2)
                rows.forEach { rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
                    ) {
                        rowMetrics.forEach { metric ->
                            MetricCard(metric, modifier = Modifier.weight(1f))
                        }
                        if (rowMetrics.size == 1) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MetricGridPreview() {
    TimeTrackerTheme {
        MetricGrid(TimeTrackerPreviewData.uiState.metrics)
    }
}
