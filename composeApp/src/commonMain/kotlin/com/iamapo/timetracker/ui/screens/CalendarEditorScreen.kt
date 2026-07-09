package com.iamapo.timetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.components.CalendarDayCell
import com.iamapo.timetracker.ui.components.CalendarWeekdays
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

object CalendarEditorScreen {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        selectedDate: LocalDate,
        onSelectDate: (LocalDate) -> Unit,
        onBack: (() -> Unit)? = null,
        onIncreaseDay: (LocalDate) -> Unit,
        onDecreaseDay: (LocalDate) -> Unit,
        onVacation: (LocalDate) -> Unit,
        onSick: (LocalDate) -> Unit,
        onForgottenWorkDay: (LocalDate) -> Unit,
        onClear: (LocalDate) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val selectedDay = state.calendarDays.firstOrNull { it.date == selectedDate }
            ?: state.calendarDays.firstOrNull { it.isToday }
            ?: state.calendarDays.first()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Header(
                    monthTitle = state.monthTitle,
                    onBack = onBack
                )
            }
            item {
                MonthGrid(
                    monthTitle = state.monthTitle,
                    days = state.calendarDays,
                    selectedDate = selectedDay.date,
                    onSelectDate = onSelectDate
                )
            }
            item {
                SelectedDayPanel(
                    day = selectedDay,
                    onIncreaseDay = onIncreaseDay,
                    onDecreaseDay = onDecreaseDay,
                    onVacation = onVacation,
                    onSick = onSick,
                    onForgottenWorkDay = onForgottenWorkDay,
                    onClear = onClear
                )
            }
        }
    }

    @Composable
    private fun Header(
        monthTitle: String,
        onBack: (() -> Unit)?
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PLANUNG",
                    color = AppColors.Subtle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (onBack != null) {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PanelRaised,
                        contentColor = AppColors.Muted
                    )
                ) {
                    Text("Zurück", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    @Composable
    private fun MonthGrid(
        monthTitle: String,
        days: List<CalendarDayUiModel>,
        selectedDate: LocalDate,
        onSelectDate: (LocalDate) -> Unit
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                CalendarWeekdays()
                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { day ->
                            CalendarDayCell(
                                day = day,
                                modifier = Modifier.weight(1f),
                                selected = day.date == selectedDate,
                                onClick = { onSelectDate(day.date) }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SelectedDayPanel(
        day: CalendarDayUiModel,
        onIncreaseDay: (LocalDate) -> Unit,
        onDecreaseDay: (LocalDate) -> Unit,
        onVacation: (LocalDate) -> Unit,
        onSick: (LocalDate) -> Unit,
        onForgottenWorkDay: (LocalDate) -> Unit,
        onClear: (LocalDate) -> Unit
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = selectedDayTitle(day),
                    color = AppColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedDaySubtitle(day),
                    color = AppColors.Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        label = "-15 min",
                        onClick = { onDecreaseDay(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "+15 min",
                        onClick = { onIncreaseDay(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButton(
                        label = "Urlaub 8h",
                        onClick = { onVacation(day.date) },
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.Purple
                    )
                    ActionButton(
                        label = "Krank 8h",
                        onClick = { onSick(day.date) },
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.Rose
                    )
                }
                ActionButton(
                    label = "Arbeitstag 8h",
                    onClick = { onForgottenWorkDay(day.date) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = AppColors.Green
                )
                ActionButton(
                    label = "Eintrag löschen",
                    onClick = { onClear(day.date) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = AppColors.Soft
                )
            }
        }
    }

    @Composable
    private fun ActionButton(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: androidx.compose.ui.graphics.Color = AppColors.Blue
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor.copy(alpha = 0.16f),
                contentColor = containerColor
            )
        ) {
            Text(label, fontWeight = FontWeight.Bold)
        }
    }

    private fun selectedDayTitle(day: CalendarDayUiModel): String {
        val today = if (day.isToday) "Heute, " else ""
        return today + weekdayName(day.date.dayOfWeek.isoDayNumber) + ", " + day.date.day + ". " + monthName(day.date.month.number)
    }

    private fun selectedDaySubtitle(day: CalendarDayUiModel): String = when {
        day.style == CalendarDayStyle.Vacation -> "Urlaub mit " + formatDuration(day.workedMinutes)
        day.style == CalendarDayStyle.Sick -> "Krank mit " + formatDuration(day.workedMinutes)
        day.workedMinutes > 0 -> "Arbeitszeit: " + formatDuration(day.workedMinutes)
        day.note.isNotBlank() -> day.note
        else -> "Kein Eintrag"
    }

    private fun weekdayName(isoDayNumber: Int): String = when (isoDayNumber) {
        1 -> "Montag"
        2 -> "Dienstag"
        3 -> "Mittwoch"
        4 -> "Donnerstag"
        5 -> "Freitag"
        6 -> "Samstag"
        else -> "Sonntag"
    }

    private fun monthName(monthNumber: Int): String = when (monthNumber) {
        1 -> "Januar"
        2 -> "Februar"
        3 -> "März"
        4 -> "April"
        5 -> "Mai"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "August"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        else -> "Dezember"
    }

    private fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours h" else "$hours h $minutes min"
    }
}

@Preview(
    name = "Screen - Kalender",
    showBackground = true,
    backgroundColor = 0xFF07080D,
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Composable
private fun CalendarEditorScreenPreview() {
    TimeTrackerTheme {
        CalendarEditorScreen(
            state = TimeTrackerPreviewData.uiState,
            selectedDate = LocalDate(2026, 7, 7),
            onSelectDate = {},
            onBack = null,
            onIncreaseDay = {},
            onDecreaseDay = {},
            onVacation = {},
            onSick = {},
            onForgottenWorkDay = {},
            onClear = {}
        )
    }
}
