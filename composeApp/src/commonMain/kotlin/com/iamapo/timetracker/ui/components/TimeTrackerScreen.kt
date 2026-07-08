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
        onDecreaseRequiredBreak: () -> Unit,
        onIncreaseRequiredBreak: () -> Unit,
        onOpenCalendar: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { TopBarSection(state.dateLabel, state.title) }
            item { StatusCard(state, onPrimaryAction, onSecondaryAction) }
            item { TargetSummaryStrip(state.targets) }
            item { MetricGrid(state.metrics) }
            item { TimelineSection(state.timeline) }
            item {
                CalendarPanel(
                    monthTitle = state.monthTitle,
                    days = state.calendarDays.take(14),
                    plannedWeek = state.plannedWeek,
                    reachedWeek = state.reachedWeek,
                    onOpenCalendar = onOpenCalendar
                )
            }
        }
    }
}
