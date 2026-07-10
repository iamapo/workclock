package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object WeekSummaryRow {
    @Composable
    operator fun invoke(plannedWeek: String, reachedWeek: String, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WeekSummaryTile(stringResource(Res.string.planned_this_week), plannedWeek, Modifier.weight(1f))
            WeekSummaryTile(stringResource(Res.string.currently_reached), reachedWeek, Modifier.weight(1f))
        }
    }
}

@Preview
@Composable
private fun WeekSummaryRowPreview() {
    TimeTrackerTheme {
        WeekSummaryRow(
            plannedWeek = "40:00 h",
            reachedWeek = "21:40 h"
        )
    }
}
