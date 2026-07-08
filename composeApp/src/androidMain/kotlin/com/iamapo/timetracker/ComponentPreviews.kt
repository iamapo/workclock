package com.iamapo.timetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.CalendarDayCell
import com.iamapo.timetracker.ui.components.CalendarEditorScreen
import com.iamapo.timetracker.ui.components.CalendarPanel
import com.iamapo.timetracker.ui.components.CalendarWeekdays
import com.iamapo.timetracker.ui.components.EndTimeCard
import com.iamapo.timetracker.ui.components.MetricCard
import com.iamapo.timetracker.ui.components.MetricGrid
import com.iamapo.timetracker.ui.components.NotesPanel
import com.iamapo.timetracker.ui.components.PrimaryActionsRow
import com.iamapo.timetracker.ui.components.SettingsPanel
import com.iamapo.timetracker.ui.components.SettingsRow
import com.iamapo.timetracker.ui.components.StatusCard
import com.iamapo.timetracker.ui.components.TargetSummaryStrip
import com.iamapo.timetracker.ui.components.TimeProgressBar
import com.iamapo.timetracker.ui.components.TimeTrackerRoute
import com.iamapo.timetracker.ui.components.TimeTrackerScreen
import com.iamapo.timetracker.ui.components.TimelineRow
import com.iamapo.timetracker.ui.components.TimelineSection
import com.iamapo.timetracker.ui.components.TopBarSection
import com.iamapo.timetracker.ui.components.WatchActionButton
import com.iamapo.timetracker.ui.components.WatchCompanionCard
import com.iamapo.timetracker.ui.components.WeekSummaryRow
import com.iamapo.timetracker.ui.components.WeekSummaryTile
import com.iamapo.timetracker.ui.state.MetricUiModel
import com.iamapo.timetracker.ui.state.SettingsUiModel
import com.iamapo.timetracker.ui.state.TargetItemUiModel
import com.iamapo.timetracker.ui.state.TimelineItemUiModel
import com.iamapo.timetracker.ui.state.TimelineKind
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlinx.datetime.LocalDate

@Preview
@Composable
private fun CalendarDayCellPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            CalendarDayCell(TimeTrackerPreviewData.uiState.calendarDays.first { it.isToday })
        }
    }
}

@Preview
@Composable
private fun CalendarEditorScreenPreview() {
    TimeTrackerTheme {
        CalendarEditorScreen(
            state = TimeTrackerPreviewData.uiState,
            selectedDate = LocalDate(2026, 7, 7),
            onSelectDate = {},
            onBack = {},
            onIncreaseDay = {},
            onDecreaseDay = {},
            onVacation = {},
            onSick = {},
            onClear = {}
        )
    }
}

@Preview
@Composable
private fun CalendarPanelPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            CalendarPanel(
                monthTitle = TimeTrackerPreviewData.uiState.monthTitle,
                days = TimeTrackerPreviewData.uiState.calendarDays.take(14),
                plannedWeek = TimeTrackerPreviewData.uiState.plannedWeek,
                reachedWeek = TimeTrackerPreviewData.uiState.reachedWeek,
                onOpenCalendar = {}
            )
        }
    }
}

@Preview
@Composable
private fun CalendarWeekdaysPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            CalendarWeekdays()
        }
    }
}

@Preview
@Composable
private fun EndTimeCardPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            EndTimeCard("17:21 Uhr", "inkl. 30 min Pflichtpause")
        }
    }
}

@Preview
@Composable
private fun MetricCardPreview() {
    TimeTrackerTheme {
        PreviewFrame {
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

@Preview
@Composable
private fun NotesPanelPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            NotesPanel()
        }
    }
}

@Preview
@Composable
private fun PrimaryActionsRowPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            PrimaryActionsRow(
                primaryLabel = "Pause starten",
                secondaryLabel = "Tag beenden",
                onPrimaryAction = {},
                onSecondaryAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun SettingsPanelPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            SettingsPanel(
                settings = SettingsUiModel(
                    dailyTarget = "8:00 h",
                    requiredBreak = "30 min",
                    canDecreaseRequiredBreak = true,
                    canIncreaseRequiredBreak = true,
                    weeklyTarget = "40:00 h"
                ),
                onDecreaseRequiredBreak = {},
                onIncreaseRequiredBreak = {}
            )
        }
    }
}

@Preview
@Composable
private fun SettingsRowPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            SettingsRow(
                label = "Pflichtpause",
                value = "30 min",
                onDecrease = {},
                onIncrease = {}
            )
        }
    }
}

@Preview
@Composable
private fun StatusCardPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            StatusCard(
                state = TimeTrackerPreviewData.uiState,
                onPrimaryAction = {},
                onSecondaryAction = {}
            )
        }
    }
}

@Preview
@Composable
private fun TargetSummaryStripPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            TargetSummaryStrip(
                listOf(
                    TargetItemUiModel("Arbeiten bis", "17:21"),
                    TargetItemUiModel("Soll heute", "8 h"),
                    TargetItemUiModel("Pflichtpause", "30 min")
                )
            )
        }
    }
}

@Preview
@Composable
private fun TimeProgressBarPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            TimeProgressBar(0.64f)
        }
    }
}

@Preview
@Composable
private fun TimeTrackerRoutePreview() {
    TimeTrackerRoute()
}

@Preview
@Composable
private fun TimeTrackerScreenPreview() {
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

@Preview
@Composable
private fun TimelineRowPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            TimelineRow(
                TimelineItemUiModel(
                    time = "12:26",
                    title = "Weitergearbeitet",
                    kind = TimelineKind.Work
                )
            )
        }
    }
}

@Preview
@Composable
private fun TimelineSectionPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            TimelineSection(TimeTrackerPreviewData.uiState.timeline)
        }
    }
}

@Preview
@Composable
private fun TopBarSectionPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            TopBarSection(
                dateLabel = TimeTrackerPreviewData.uiState.dateLabel,
                title = TimeTrackerPreviewData.uiState.title
            )
        }
    }
}

@Preview
@Composable
private fun WatchActionButtonPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WatchActionButton("II")
        }
    }
}

@Preview
@Composable
private fun WatchCompanionCardPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WatchCompanionCard(
                state = "Aktiv",
                remaining = "2:48",
                caption = "noch bis 17:21"
            )
        }
    }
}

@Preview
@Composable
private fun WeekSummaryRowPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WeekSummaryRow(
                plannedWeek = "40:00 h",
                reachedWeek = "21:40 h"
            )
        }
    }
}

@Preview
@Composable
private fun WeekSummaryTilePreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WeekSummaryTile(
                label = "Aktuell erreicht",
                value = "21:40 h"
            )
        }
    }
}

@Preview
@Composable
private fun TimeTrackerThemePreview() {
    TimeTrackerTheme {
        PreviewFrame {
            Column {
                TopBarSection("Dienstag, 7. Juli", "WorkClock")
            }
        }
    }
}

@Preview
@Composable
private fun PreviewFramePreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WatchActionButton("II")
        }
    }
}

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(420.dp)
            .background(AppColors.Background)
            .padding(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
