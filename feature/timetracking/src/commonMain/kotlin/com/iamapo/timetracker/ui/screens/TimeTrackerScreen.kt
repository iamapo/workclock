package com.iamapo.timetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.ComposePreviewContext
import com.iamapo.timetracker.ui.components.StatusCard
import com.iamapo.timetracker.ui.components.TimelineEventTimeDialog
import com.iamapo.timetracker.ui.components.TimelineSection
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TimeTrackerScreen {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        onEventTimeChanged: ((Int, Int) -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        // Kept as an index so the open dialog follows the live state: its editable
        // range grows with the clock and it closes when the event stops being editable.
        var editEventIndex by remember { mutableStateOf<Int?>(null) }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { StatusCard(state, onPrimaryAction, onSecondaryAction) }
            item {
                TimelineSection(
                    items = state.timeline,
                    weeklyBalance = state.weeklyBalance,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onEditItem = if (onEventTimeChanged != null) {
                        { item -> editEventIndex = item.edit?.eventIndex }
                    } else {
                        null
                    }
                )
            }
        }

        val editItem = editEventIndex?.let { index ->
            state.timeline.firstOrNull { it.edit?.eventIndex == index }
        }
        val edit = editItem?.edit
        if (editItem != null && edit != null && onEventTimeChanged != null) {
            TimelineEventTimeDialog(
                item = editItem,
                edit = edit,
                onDismiss = { editEventIndex = null },
                onSave = { minuteOfDay ->
                    onEventTimeChanged(edit.eventIndex, minuteOfDay)
                    editEventIndex = null
                }
            )
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
            onEventTimeChanged = { _, _ -> }
        )
    }
}
