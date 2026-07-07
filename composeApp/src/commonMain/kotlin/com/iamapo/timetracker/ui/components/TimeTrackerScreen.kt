package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors

object TimeTrackerScreen {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TopBarSection(state.dateLabel, state.title) }
            item { StatusCard(state, onPrimaryAction, onSecondaryAction) }
            item { TargetSummaryStrip(state.targets) }
            item { MetricGrid(state.metrics) }
            item { TimelineSection(state.timeline) }
            item { SettingsPanel(state.settings) }
            item { CalendarPanel(state.monthTitle, state.calendarDays, state.plannedWeek, state.reachedWeek) }
            item { WatchCompanionCard(state.watchState, state.watchRemaining, state.watchCaption) }
            item { NotesPanel() }
        }
    }
}
