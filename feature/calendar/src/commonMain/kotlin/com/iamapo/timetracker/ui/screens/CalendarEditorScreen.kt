package com.iamapo.timetracker.ui.screens

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.iamapo.timetracker.ui.components.CalendarDayCell
import com.iamapo.timetracker.ui.components.CalendarWeekdays
import com.iamapo.timetracker.ui.components.WeekOverviewCard
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.presentation.state.CalendarUiState
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.*

object CalendarEditorScreen {
    @Composable
    operator fun invoke(
        state: CalendarUiState,
        selectedDate: LocalDate,
        onSelectDate: (LocalDate) -> Unit,
        onBack: (() -> Unit)? = null,
        onIncreaseDay: (LocalDate) -> Unit,
        onDecreaseDay: (LocalDate) -> Unit,
        onVacation: (LocalDate) -> Unit,
        onSick: (LocalDate) -> Unit,
        onForgottenWorkDay: (LocalDate) -> Unit,
        onClear: (LocalDate) -> Unit,
        onSetWorkTimes: (LocalDate, Int, Int, Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var editDay by remember { mutableStateOf<CalendarDayUiModel?>(null) }
        val selectedDay = state.days.firstOrNull { it.date == selectedDate }
            ?: state.days.firstOrNull { it.isToday }
            ?: state.days.first()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentPadding = PaddingValues(start = AppDimensions.size20, top = AppDimensions.size18, end = AppDimensions.size20, bottom = AppDimensions.size28),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
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
                    days = state.days,
                    selectedDate = selectedDay.date,
                    onSelectDate = onSelectDate,
                    onEditDate = { day ->
                        onSelectDate(day.date)
                        editDay = day
                    }
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
        editDay?.let { day ->
            WorkTimeDialog(
                day = day,
                onDismiss = { editDay = null },
                onSave = { start, pause, end ->
                    onSetWorkTimes(day.date, start, pause, end)
                    editDay = null
                }
            )
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
                    fontSize = AppFontSizes.size12,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0_4
                )
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size30,
                    lineHeight = AppFontSizes.size32,
                    fontWeight = FontWeight.Black
                )
            }
            if (onBack != null) {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(AppDimensions.size10),
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
        onSelectDate: (LocalDate) -> Unit,
        onEditDate: (CalendarDayUiModel) -> Unit
    ) {
        val today = days.firstOrNull { it.isToday }?.date
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size12)
            ) {
                Text(
                    text = monthTitle,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size24,
                    fontWeight = FontWeight.Black
                )
                CalendarWeekdays()
                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.size6)
                    ) {
                        week.forEach { day ->
                            CalendarDayCell(
                                day = day,
                                modifier = Modifier.weight(1f),
                                selected = day.date == selectedDate,
                                onClick = { onSelectDate(day.date) },
                                onLongClick = if (today != null && day.date <= today) {
                                    { onEditDate(day) }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WorkTimeDialog(
        day: CalendarDayUiModel,
        onDismiss: () -> Unit,
        onSave: (Int, Int, Int) -> Unit
    ) {
        val initialStart = day.startMinute ?: 8 * 60
        val initialPause = day.breakMinutes.takeIf { it > 0 } ?: 30
        val initialEnd = day.endMinute ?: (initialStart + day.workedMinutes.takeIf { it > 0 }.orDefault(8 * 60) + initialPause)
            .coerceAtMost(23 * 60 + 59)
        var start by remember(day.date) { mutableStateOf(formatClock(initialStart)) }
        var pause by remember(day.date) { mutableStateOf(formatClock(initialPause)) }
        var end by remember(day.date) { mutableStateOf(formatClock(initialEnd)) }
        val startMinute = parseClock(start)
        val pauseMinutes = parseDuration(pause)
        val endMinute = parseClock(end)
        val elapsed = if (startMinute != null && endMinute != null) {
            if (endMinute >= startMinute) endMinute - startMinute else 24 * 60 - startMinute + endMinute
        } else null
        val worked = if (elapsed != null && pauseMinutes != null && pauseMinutes <= elapsed) elapsed - pauseMinutes else null

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                color = AppColors.Panel,
                border = BorderStroke(AppDimensions.size1, AppColors.Line),
                shape = RoundedCornerShape(AppDimensions.size18)
            ) {
                Column(
                    modifier = Modifier.padding(AppDimensions.size20),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
                ) {
                    Text(stringResource(Res.string.edit_workday), color = AppColors.Ink, fontSize = AppFontSizes.size24, fontWeight = FontWeight.Black)
                    Text(selectedDayTitle(day), color = AppColors.Muted, fontSize = AppFontSizes.size13)
                    Text(stringResource(Res.string.adjust_day_times), color = AppColors.Subtle, fontSize = AppFontSizes.size12)
                    TimeInput(stringResource(Res.string.work_start), start) { start = normalizeTimeInput(it) }
                    TimeInput(stringResource(Res.string.break_label), pause) { pause = normalizeTimeInput(it) }
                    TimeInput(stringResource(Res.string.work_end), end) { end = normalizeTimeInput(it) }
                    Surface(color = AppColors.Blue.copy(alpha = 0.08f), shape = RoundedCornerShape(AppDimensions.size10)) {
                        Column(Modifier.fillMaxWidth().padding(AppDimensions.size14)) {
                            Text(stringResource(Res.string.working_time), color = AppColors.Blue, fontWeight = FontWeight.Bold)
                            Text(worked?.let(::formatDuration) ?: "–", color = AppColors.Ink, fontSize = AppFontSizes.size24, fontWeight = FontWeight.Black)
                            if (worked != null) Text("$start – $end · ${formatDuration(pauseMinutes!!)} ${stringResource(Res.string.break_label)}", color = AppColors.Muted, fontSize = AppFontSizes.size12)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel), color = AppColors.Blue, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(AppDimensions.size10))
                        Button(
                            onClick = { onSave(startMinute!!, pauseMinutes!!, endMinute!!) },
                            enabled = worked != null,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue),
                            shape = RoundedCornerShape(AppDimensions.size10)
                        ) { Text(stringResource(Res.string.save), fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    @Composable
    private fun TimeInput(label: String, value: String, onValueChange: (String) -> Unit) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(AppDimensions.size10),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Blue,
                focusedLabelColor = AppColors.Blue,
                cursorColor = AppColors.Blue
            )
        )
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
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size18)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
            ) {
                Text(
                    text = selectedDayTitle(day),
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size18,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedDaySubtitle(day),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size13,
                    fontWeight = FontWeight.Normal
                )
                if (day.style == CalendarDayStyle.Holiday) {
                    Text(
                        text = stringResource(
                            Res.string.regular_target,
                            formatDuration(day.scheduledTargetMinutes)
                        ) + " · " + stringResource(Res.string.holiday_target),
                        color = AppColors.Purple,
                        fontSize = AppFontSizes.size12,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size10)
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
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size10)
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
                    label = if (day.style == CalendarDayStyle.Holiday) {
                        stringResource(
                            Res.string.work_on_holiday,
                            formatDuration(day.scheduledTargetMinutes)
                        )
                    } else {
                        stringResource(
                            Res.string.workday_value,
                            formatDuration(day.scheduledTargetMinutes)
                        )
                    },
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
            modifier = modifier.heightIn(min = AppDimensions.size48),
            shape = RoundedCornerShape(AppDimensions.size8),
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
        day.style == CalendarDayStyle.Holiday -> day.holidayName ?: stringResource(Res.string.public_holiday)
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

    private fun formatClock(minutes: Int): String = "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"

    private fun parseClock(value: String): Int? {
        val parts = value.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return if (hour in 0..23 && minute in 0..59) hour * 60 + minute else null
    }

    private fun parseDuration(value: String): Int? {
        val parts = value.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return if (hour >= 0 && minute in 0..59) hour * 60 + minute else null
    }

    private fun normalizeTimeInput(value: String): String = value.filter { it.isDigit() || it == ':' }.take(5)

    private fun Int?.orDefault(default: Int): Int = this ?: default
}
