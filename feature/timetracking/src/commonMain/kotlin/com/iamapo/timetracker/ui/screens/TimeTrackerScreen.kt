package com.iamapo.timetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.components.MetricGrid
import com.iamapo.timetracker.ui.components.StatusCard
import com.iamapo.timetracker.ui.components.TimelineSection
import com.iamapo.timetracker.ui.components.TopBarSection
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.ledgerRuledPaper
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TimeTrackerScreen {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        calendarContent: @Composable () -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .ledgerRuledPaper(),
            contentPadding = PaddingValues(start = AppDimensions.size20, top = AppDimensions.size18, end = AppDimensions.size20, bottom = AppDimensions.size28),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
        ) {
            item { TopBarSection(state.dateLabel, state.title) }
            item { StatusCard(state, onPrimaryAction, onSecondaryAction) }
            item { MetricGrid(state.metrics) }
            item { TimelineSection(state.timeline) }
            item { calendarContent() }
        }
    }
}

@Preview(
    name = "Screen - Heute",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerScreenPreview() {
    TimeTrackerScreenPreviewContent { TimeTrackerPreviewData.uiStateWorking() }
}

@Preview(
    name = "Screen - Bereit",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerScreenNotStartedPreview() {
    TimeTrackerScreenPreviewContent { TimeTrackerPreviewData.uiStateNotStarted() }
}

@Preview(
    name = "Screen - Am Arbeiten",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerScreenWorkingPreview() {
    TimeTrackerScreenPreviewContent { TimeTrackerPreviewData.uiStateWorking() }
}

@Preview(
    name = "Screen - Pause",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerScreenPausedPreview() {
    TimeTrackerScreenPreviewContent { TimeTrackerPreviewData.uiStatePaused() }
}

@Preview(
    name = "Screen - Feierabend",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun TimeTrackerScreenFinishedPreview() {
    TimeTrackerScreenPreviewContent { TimeTrackerPreviewData.uiStateFinished() }
}

@Composable
private fun TimeTrackerScreenPreviewContent(state: () -> TimeTrackerUiState) {
    ComposePreviewContext()
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = remember { state() },
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
