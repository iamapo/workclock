package com.iamapo.timetracker

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.iamapo.timetracker.data.IosWorkDayStore
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.TimeTrackerViewModel
import com.iamapo.timetracker.ui.screens.CalendarEditorScreen
import com.iamapo.timetracker.ui.screens.TimeTrackerScreen
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.watch.IosWatchSessionController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val workDayStore = remember { IosWorkDayStore() }
    val viewModel = remember { TimeTrackerViewModel(workDayStore = workDayStore) }
    val watchSession = remember {
        IosWatchSessionController(onCommand = viewModel::onWatchCommand).also { it.activate() }
    }
    val state by viewModel.uiState.collectAsState()
    var isCalendarOpen by remember { mutableStateOf(false) }
    var selectedCalendarDate by remember { mutableStateOf(state.calendarDays.firstOrNull { it.isToday }?.date) }

    LaunchedEffect(state) {
        watchSession.publish(state)
    }

    TimeTrackerTheme {
        if (isCalendarOpen) {
            CalendarEditorScreen(
                state = state,
                selectedDate = selectedCalendarDate
                    ?: state.calendarDays.firstOrNull { it.isToday }?.date
                    ?: state.calendarDays.first().date,
                onSelectDate = { selectedCalendarDate = it },
                onBack = { isCalendarOpen = false },
                onIncreaseDay = viewModel::increaseCalendarDay,
                onDecreaseDay = viewModel::decreaseCalendarDay,
                onVacation = viewModel::setCalendarDayVacation,
                onSick = viewModel::setCalendarDaySick,
                onClear = viewModel::clearCalendarDay
            )
        } else {
            TimeTrackerScreen(
                state = state,
                onPrimaryAction = viewModel::onPrimaryAction,
                onSecondaryAction = viewModel::onSecondaryAction,
                onDecreaseRequiredBreak = viewModel::decreaseRequiredBreak,
                onIncreaseRequiredBreak = viewModel::increaseRequiredBreak,
                onOpenCalendar = {
                    selectedCalendarDate = state.calendarDays.firstOrNull { it.isToday }?.date
                        ?: state.calendarDays.firstOrNull()?.date
                    isCalendarOpen = true
                }
            )
        }
    }
}

fun PreviewViewController(): UIViewController = ComposeUIViewController {
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
