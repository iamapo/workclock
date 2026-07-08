package com.iamapo.timetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

@Preview(
    name = "Android Arbeitszeit Dashboard",
    showBackground = true,
    backgroundColor = 0xFF070B12,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun AndroidTimeTrackerPreview() {
    TimeTrackerTheme {
        TimeTrackerScreen(
            state = TimeTrackerPreviewData.uiState,
            onPrimaryAction = {},
            onSecondaryAction = {},
            onDecreaseRequiredBreak = {},
            onIncreaseRequiredBreak = {},
            onOpenCalendar = {}
        )
    }
}
