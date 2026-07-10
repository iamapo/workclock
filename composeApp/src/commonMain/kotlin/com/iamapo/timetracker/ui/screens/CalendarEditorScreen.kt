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
import com.iamapo.timetracker.ui.components.WeekOverviewCard
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.presentation.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*

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
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Header(
                    monthTitle = state.monthTitle,
                    onBack = onBack
                )
            }
            item { WeekOverviewCard(state.weekOverview, showCarry = true) }
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
                    dailyTarget = state.settings.dailyTarget,
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
                    text = stringResource(Res.string.planning),
                    color = AppColors.Subtle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Black
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
                    Text(stringResource(Res.string.back), fontWeight = FontWeight.SemiBold)
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
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
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
        dailyTarget: String,
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
            shape = RoundedCornerShape(18.dp)
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
                        label = stringResource(Res.string.vacation),
                        onClick = { onVacation(day.date) },
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.Purple
                    )
                    ActionButton(
                        label = stringResource(Res.string.sick),
                        onClick = { onSick(day.date) },
                        modifier = Modifier.weight(1f),
                        containerColor = AppColors.Rose
                    )
                }
                ActionButton(
                    label = stringResource(Res.string.workday_value, dailyTarget),
                    onClick = { onForgottenWorkDay(day.date) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = AppColors.Green
                )
                ActionButton(
                    label = stringResource(Res.string.delete_entry),
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

    @Composable
    private fun selectedDayTitle(day: CalendarDayUiModel): String {
        val today = if (day.isToday) stringResource(Res.string.today_prefix) else ""
        return today + weekdayName(day.date.dayOfWeek.isoDayNumber) + ", " + day.date.day + ". " + monthName(day.date.month.number)
    }

    @Composable
    private fun selectedDaySubtitle(day: CalendarDayUiModel): String = when {
        day.style == CalendarDayStyle.Vacation -> stringResource(Res.string.vacation_with, formatDuration(day.workedMinutes))
        day.style == CalendarDayStyle.Sick -> stringResource(Res.string.sick_with, formatDuration(day.workedMinutes))
        day.workedMinutes > 0 -> stringResource(Res.string.working_time_value, formatDuration(day.workedMinutes))
        day.note.isNotBlank() -> day.note
        else -> stringResource(Res.string.no_entry)
    }

    @Composable private fun weekdayName(isoDayNumber: Int): String = stringResource(when (isoDayNumber) {
        1 -> Res.string.monday; 2 -> Res.string.tuesday; 3 -> Res.string.wednesday
        4 -> Res.string.thursday; 5 -> Res.string.friday; 6 -> Res.string.saturday
        else -> Res.string.sunday
    })

    @Composable private fun monthName(monthNumber: Int): String = stringResource(listOf(
        Res.string.january, Res.string.february, Res.string.march, Res.string.april,
        Res.string.may, Res.string.june, Res.string.july, Res.string.august,
        Res.string.september, Res.string.october, Res.string.november, Res.string.december
    )[monthNumber - 1])

    private fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours h" else "$hours h $minutes min"
    }
}

@Preview(
    name = "Screen - Kalender",
    showBackground = true,
    backgroundColor = 0xFFFFFAF2,
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
