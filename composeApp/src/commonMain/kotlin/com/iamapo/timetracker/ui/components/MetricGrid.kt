package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.PreviewFrame
import com.iamapo.timetracker.ui.state.MetricUiModel
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object MetricGrid {
    @Composable
    operator fun invoke(metrics: List<MetricUiModel>, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = if (metrics.size <= 3) listOf(metrics) else metrics.chunked(2)
            rows.forEach { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Preview
@Composable
private fun MetricGridPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            MetricGrid(TimeTrackerPreviewData.uiState.metrics)
        }
    }
}
