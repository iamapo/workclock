package com.iamapo.timetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.presentation.state.CalendarUiState
import com.iamapo.timetracker.presentation.TimeInputFormatter
import com.iamapo.timetracker.presentation.TimeTextFormatter
import com.iamapo.timetracker.resources.*
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource

object CalendarEditorScreen {
    @Composable
    operator fun invoke(
        state: CalendarUiState,
        selectedDate: LocalDate,
        onSelectDate: (LocalDate) -> Unit,
        onPreviousMonth: () -> Unit,
        onNextMonth: () -> Unit,
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
            ?: state.days.first { it.isCurrentMonth }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = AppDimensions.size24)
        ) {
            item {
                CalendarHero(
                    state = state,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    canNavigateToNextMonth = state.canNavigateToNextMonth
                )
            }
            item {
                MonthGrid(
                    monthTitle = state.monthTitle,
                    days = state.days,
                    selectedDate = selectedDay.date,
                    onSelectDate = onSelectDate,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    canNavigateToNextMonth = state.canNavigateToNextMonth,
                    onEditDate = { day ->
                        onSelectDate(day.date)
                        editDay = day
                    }
                )
            }
            item { CalendarLegend() }
            item {
                SelectedDayPanel(
                    day = selectedDay,
                    onEdit = { editDay = selectedDay }
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
                },
                onIncreaseDay = {
                    onIncreaseDay(day.date)
                    editDay = null
                },
                onDecreaseDay = {
                    onDecreaseDay(day.date)
                    editDay = null
                },
                onVacation = {
                    onVacation(day.date)
                    editDay = null
                },
                onSick = {
                    onSick(day.date)
                    editDay = null
                },
                onForgottenWorkDay = {
                    onForgottenWorkDay(day.date)
                    editDay = null
                },
                onClear = {
                    onClear(day.date)
                    editDay = null
                }
            )
        }

    }

    @Composable
    private fun CalendarHero(
        state: CalendarUiState,
        onPreviousMonth: () -> Unit,
        onNextMonth: () -> Unit,
        canNavigateToNextMonth: Boolean
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AppDimensions.size24,
                    top = AppDimensions.size24,
                    end = AppDimensions.size24,
                    bottom = AppDimensions.size12)
        ) {
            Text(
                text = stringResource(Res.string.calendar_title),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size40,
                lineHeight = AppFontSizes.size44,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = state.monthTitle,
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size16,
                lineHeight = AppFontSizes.size20,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = AppDimensions.size4)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimensions.size12),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthArrowButton(false, true, onPreviousMonth)
                MonthArrowButton(true, canNavigateToNextMonth, onNextMonth)
            }

            Text(
                text = stringResource(Res.string.week_balance),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size13,
                lineHeight = AppFontSizes.size16,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = AppDimensions.size12)
            )
            Text(
                text = formatBalance(state.weekOverview.balanceMinutes),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size48,
                lineHeight = AppFontSizes.size52,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    Res.string.week_summary,
                    state.weekOverview.weekNumber,
                    state.weekOverview.reached,
                    state.plannedWeek
                ),
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size16,
                lineHeight = AppFontSizes.size20,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    private fun MonthArrowButton(
        pointsRight: Boolean,
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .size(AppDimensions.size36)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onClick),
            shape = CircleShape,
            color = AppColors.Background.copy(alpha = if (enabled) 0.96f else 0.52f)
        ) {
            Canvas(Modifier.padding(AppDimensions.size12)) {
                val xStart = if (pointsRight) size.width * 0.34f else size.width * 0.66f
                val xEnd = if (pointsRight) size.width * 0.68f else size.width * 0.32f
                drawLine(
                    color = if (enabled) AppColors.Ink else AppColors.Disabled,
                    start = Offset(xStart, size.height * 0.12f),
                    end = Offset(xEnd, size.height * 0.5f),
                    strokeWidth = AppDimensions.size2.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = if (enabled) AppColors.Ink else AppColors.Disabled,
                    start = Offset(xEnd, size.height * 0.5f),
                    end = Offset(xStart, size.height * 0.88f),
                    strokeWidth = AppDimensions.size2.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }

    @Composable
    private fun MonthGrid(
        monthTitle: String,
        days: List<CalendarDayUiModel>,
        selectedDate: LocalDate,
        onSelectDate: (LocalDate) -> Unit,
        onPreviousMonth: () -> Unit,
        onNextMonth: () -> Unit,
        canNavigateToNextMonth: Boolean,
        onEditDate: (CalendarDayUiModel) -> Unit
    ) {
        var horizontalDrag by remember { mutableStateOf(0f) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(monthTitle, canNavigateToNextMonth) {
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            horizontalDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                horizontalDrag < -SwipeThreshold && canNavigateToNextMonth -> onNextMonth()
                                horizontalDrag > SwipeThreshold -> onPreviousMonth()
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
                    )
                }
                .background(AppColors.Background)
                .padding(horizontal = AppDimensions.size20)
        ) {
            CalendarWeekdayHeader()
            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = AppColors.CalendarLine,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = AppDimensions.size1.toPx()
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    week.forEach { day ->
                        CalendarDay(
                            day = day,
                            selected = day.date == selectedDate,
                            onClick = { onSelectDate(day.date) },
                            onLongClick = if (day.isCurrentMonth) {
                                { onEditDate(day) }
                            } else {
                                null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CalendarWeekdayHeader() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimensions.size44)
                .drawBehind {
                    drawLine(
                        color = AppColors.CalendarLine,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = AppDimensions.size1.toPx()
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Res.string.monday_short,
                Res.string.tuesday_short,
                Res.string.wednesday_short,
                Res.string.thursday_short,
                Res.string.friday_short,
                Res.string.saturday_short,
                Res.string.sunday_short
            ).forEach { label ->
                Text(
                    text = stringResource(label),
                    modifier = Modifier.weight(1f),
                    color = AppColors.Ink,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = AppFontSizes.size13,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun CalendarDay(
        day: CalendarDayUiModel,
        selected: Boolean,
        onClick: () -> Unit,
        onLongClick: (() -> Unit)?,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(AppDimensions.size12)
        val isMuted = !day.isCurrentMonth || day.style == CalendarDayStyle.Weekend
        val border = if (day.isToday) BorderStroke(AppDimensions.size2, AppColors.Coral) else null

        Surface(
            modifier = modifier
                .height(AppDimensions.size58)
                .padding(horizontal = AppDimensions.size4, vertical = AppDimensions.size4)
                .clip(shape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            shape = shape,
            color = if (selected) AppColors.Green.copy(alpha = 0.16f) else Color.Transparent,
            border = border
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.day,
                    color = when {
                        day.isToday -> AppColors.Coral
                        isMuted -> AppColors.Disabled
                        else -> AppColors.Ink
                    },
                    fontFamily = FontFamily.SansSerif,
                    fontSize = AppFontSizes.size16,
                    lineHeight = AppFontSizes.size20,
                    fontWeight = if (selected || day.isToday) FontWeight.Medium else FontWeight.Normal
                )
                if (day.isToday) {
                    Text(
                        text = stringResource(Res.string.nav_today),
                        color = AppColors.Coral,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = AppFontSizes.size11,
                        lineHeight = AppFontSizes.size14,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(Modifier.height(AppDimensions.size4))
                    CalendarDayMarker(day)
                }
            }
        }
    }

    @Composable
    private fun CalendarDayMarker(day: CalendarDayUiModel) {
        when {
            day.style == CalendarDayStyle.Vacation -> StatusBar(AppColors.Sand)
            day.style == CalendarDayStyle.Sick -> StatusDot(AppColors.Subtle)
            day.style == CalendarDayStyle.Holiday -> StatusDot(AppColors.Coral.copy(alpha = 0.76f))
            day.workedMinutes > 0 -> StatusBar(AppColors.Green.copy(alpha = 0.88f))
            else -> Spacer(Modifier.height(AppDimensions.size4))
        }
    }

    @Composable
    private fun StatusBar(color: Color) {
        Box(
            Modifier
                .width(AppDimensions.size16)
                .height(AppDimensions.size4)
                .clip(RoundedCornerShape(AppDimensions.size99))
                .background(color)
        )
    }

    @Composable
    private fun StatusDot(color: Color) {
        Box(
            Modifier
                .size(AppDimensions.size8)
                .clip(CircleShape)
                .background(color)
        )
    }

    @Composable
    private fun CalendarLegend() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.size24, vertical = AppDimensions.size12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(stringResource(Res.string.worked), AppColors.Green, isDot = false)
            LegendItem(stringResource(Res.string.vacation), AppColors.Sand, isDot = false)
            LegendItem(stringResource(Res.string.sick), AppColors.Subtle, isDot = true)
        }
    }

    @Composable
    private fun LegendItem(label: String, color: Color, isDot: Boolean) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDot) StatusDot(color) else StatusBar(color)
            Text(
                text = label,
                color = AppColors.Ink,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size12,
                lineHeight = AppFontSizes.size14
            )
        }
    }

    @Composable
    private fun SelectedDayPanel(
        day: CalendarDayUiModel,
        onEdit: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = AppColors.CalendarLine,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = AppDimensions.size1.toPx()
                    )
                }
                .padding(horizontal = AppDimensions.size24, vertical = AppDimensions.size16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedDayTitle(day),
                        color = AppColors.Ink,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = AppFontSizes.size18,
                        lineHeight = AppFontSizes.size22,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = selectedDaySubtitle(day),
                        color = AppColors.Ink,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = AppFontSizes.size15,
                        lineHeight = AppFontSizes.size20,
                        modifier = Modifier.padding(top = AppDimensions.size4)
                    )
                }
                if (day.scheduledTargetMinutes > 0 && day.workedMinutes > 0) {
                    Text(
                        text = formatBalance(day.workedMinutes - day.scheduledTargetMinutes),
                        color = if (day.workedMinutes >= day.scheduledTargetMinutes) AppColors.Success else AppColors.Coral,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = AppFontSizes.size18,
                        lineHeight = AppFontSizes.size22,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = AppDimensions.size12)
                    )
                }
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.size52)
                    .padding(top = AppDimensions.size12),
                shape = RoundedCornerShape(AppDimensions.size10),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Ink,
                    contentColor = AppColors.Paper
                )
            ) {
                Text(
                    text = stringResource(Res.string.edit_day),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = AppFontSizes.size15,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun WorkTimeDialog(
        day: CalendarDayUiModel,
        onDismiss: () -> Unit,
        onSave: (Int, Int, Int) -> Unit,
        onIncreaseDay: () -> Unit,
        onDecreaseDay: () -> Unit,
        onVacation: () -> Unit,
        onSick: () -> Unit,
        onForgottenWorkDay: () -> Unit,
        onClear: () -> Unit
    ) {
        val initialStart = day.startMinute ?: 8 * 60
        val initialPause = day.breakMinutes.takeIf { it > 0 } ?: 30
        val initialEnd = day.endMinute
            ?: (initialStart + day.workedMinutes.takeIf { it > 0 }.orDefault(8 * 60) + initialPause)
                .coerceAtMost(23 * 60 + 59)
        var start by remember(day.date) { mutableStateOf(TextFieldValue(formatClock(initialStart))) }
        var pause by remember(day.date) { mutableStateOf(TextFieldValue(formatClock(initialPause))) }
        var end by remember(day.date) { mutableStateOf(TextFieldValue(formatClock(initialEnd))) }
        val startMinute = parseClock(start.text)
        val pauseMinutes = parseDuration(pause.text)
        val endMinute = parseClock(end.text)
        val elapsed = if (startMinute != null && endMinute != null) {
            if (endMinute >= startMinute) endMinute - startMinute else 24 * 60 - startMinute + endMinute
        } else {
            null
        }
        val worked = if (elapsed != null && pauseMinutes != null && pauseMinutes <= elapsed) {
            elapsed - pauseMinutes
        } else {
            null
        }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                color = AppColors.Background,
                border = BorderStroke(AppDimensions.size1, AppColors.CalendarLine),
                shape = RoundedCornerShape(AppDimensions.size20)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = AppDimensions.size680)
                        .padding(AppDimensions.size20),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.size12)
                ) {
                    item {
                        Text(
                            text = stringResource(Res.string.edit_workday),
                            color = AppColors.Ink,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = AppFontSizes.size24,
                            lineHeight = AppFontSizes.size28,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    item {
                        Text(
                            text = selectedDayTitle(day),
                            color = AppColors.Subtle,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = AppFontSizes.size13
                        )
                    }
                    item { TimeInput(stringResource(Res.string.work_start), start) { start = TimeInputFormatter.normalize(it) } }
                    item { TimeInput(stringResource(Res.string.break_label), pause) { pause = TimeInputFormatter.normalize(it) } }
                    item { TimeInput(stringResource(Res.string.work_end), end) { end = TimeInputFormatter.normalize(it) } }
                    item {
                        Surface(color = AppColors.Green.copy(alpha = 0.14f), shape = RoundedCornerShape(AppDimensions.size12)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppDimensions.size16, vertical = AppDimensions.size12),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.working_time),
                                    color = AppColors.Success,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = worked?.let(::formatClockDuration) ?: "–",
                                    color = AppColors.Ink,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = AppFontSizes.size20,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
                        ) {
                            ActionButton("-15 min", onDecreaseDay, Modifier.weight(1f))
                            ActionButton("+15 min", onIncreaseDay, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8)
                        ) {
                            ActionButton(stringResource(Res.string.vacation), onVacation, Modifier.weight(1f), AppColors.Sand)
                            ActionButton(stringResource(Res.string.sick), onSick, Modifier.weight(1f), AppColors.Coral)
                        }
                    }
                    item {
                        ActionButton(
                            label = if (day.style == CalendarDayStyle.Holiday) {
                                stringResource(Res.string.work_on_holiday, formatDuration(day.scheduledTargetMinutes))
                            } else {
                                stringResource(Res.string.workday_value, formatDuration(day.scheduledTargetMinutes))
                            },
                            onClick = onForgottenWorkDay,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = AppColors.Green
                        )
                    }
                    item {
                        ActionButton(
                            label = stringResource(Res.string.delete_entry),
                            onClick = onClear,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = AppColors.Coral
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(Res.string.cancel),
                                    color = AppColors.Ink,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(AppDimensions.size8))
                            Button(
                                onClick = { onSave(startMinute!!, pauseMinutes!!, endMinute!!) },
                                enabled = worked != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Ink,
                                    contentColor = AppColors.Paper
                                ),
                                shape = RoundedCornerShape(AppDimensions.size10)
                            ) {
                                Text(
                                    text = stringResource(Res.string.save),
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TimeInput(
        label: String,
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, fontFamily = FontFamily.SansSerif) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(AppDimensions.size10),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Ink,
                focusedLabelColor = AppColors.Ink,
                cursorColor = AppColors.Ink
            )
        )
    }

    @Composable
    private fun ActionButton(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = AppColors.Ink
    ) {
        // A quiet tint of the button's own colour. The neutral default would be ink at
        // 16%, which reads as a cold grey next to the coloured ones, so it takes the
        // same warm surface step as the steppers in Settings.
        val background = when (containerColor) {
            AppColors.Ink -> AppColors.Soft
            AppColors.Sand -> AppColors.Sand.copy(alpha = 0.72f)
            else -> containerColor.copy(alpha = 0.16f)
        }
        val contentColor = if (containerColor == AppColors.Sand) AppColors.Ink else containerColor
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = AppDimensions.size44),
            shape = RoundedCornerShape(AppDimensions.size10),
            colors = ButtonDefaults.buttonColors(
                containerColor = background,
                contentColor = contentColor
            ),
            contentPadding = PaddingValues(horizontal = AppDimensions.size12, vertical = AppDimensions.size8)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.SansSerif,
                fontSize = AppFontSizes.size12,
                fontWeight = FontWeight.Medium
            )
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
        day.workedMinutes > 0 -> stringResource(Res.string.worked_suffix, formatClockDuration(day.workedMinutes))
        day.note.isNotBlank() -> day.note
        else -> stringResource(Res.string.no_entry)
    }

    @Composable
    private fun weekdayName(isoDayNumber: Int): String = stringResource(
        when (isoDayNumber) {
            1 -> Res.string.monday
            2 -> Res.string.tuesday
            3 -> Res.string.wednesday
            4 -> Res.string.thursday
            5 -> Res.string.friday
            6 -> Res.string.saturday
            else -> Res.string.sunday
        }
    )

    @Composable
    private fun monthName(monthNumber: Int): String = stringResource(
        listOf(
            Res.string.january,
            Res.string.february,
            Res.string.march,
            Res.string.april,
            Res.string.may,
            Res.string.june,
            Res.string.july,
            Res.string.august,
            Res.string.september,
            Res.string.october,
            Res.string.november,
            Res.string.december
        )[monthNumber - 1]
    )

    private fun formatBalance(minutes: Int): String = TimeTextFormatter.signedClockDuration(minutes)

    private fun formatClockDuration(totalMinutes: Int): String =
        "${totalMinutes / 60}:${(totalMinutes % 60).toString().padStart(2, '0')} h"

    private fun formatDuration(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) "$hours h" else "$hours h $minutes min"
    }

    private fun formatClock(minutes: Int): String =
        "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"

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

    private fun Int?.orDefault(default: Int): Int = this ?: default

    private const val SwipeThreshold = 80f
}
