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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.iamapo.timetracker.presentation.state.CalendarDayStyle
import com.iamapo.timetracker.presentation.state.CalendarDayUiModel
import com.iamapo.timetracker.presentation.state.CalendarUiState
import com.iamapo.timetracker.resources.*
import com.iamapo.timetracker.ui.theme.AppColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

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
                .background(AppColors.Background),
            contentPadding = PaddingValues(bottom = 20.dp)
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
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(304.dp + statusBarHeight)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - 42.dp.toPx())
                    cubicTo(
                        size.width * 0.72f,
                        size.height - 18.dp.toPx(),
                        size.width * 0.34f,
                        size.height - 2.dp.toPx(),
                        0f,
                        size.height
                    )
                    close()
                }
                drawPath(path, AppColors.Coral)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(start = 26.dp, top = 22.dp, end = 26.dp, bottom = 30.dp)
            ) {
                Text(
                    text = stringResource(Res.string.calendar_title),
                    color = AppColors.Navy,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = state.monthTitle,
                    color = AppColors.Navy,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthArrowButton(false, true, onPreviousMonth)
                    MonthArrowButton(true, canNavigateToNextMonth, onNextMonth)
                }

                Text(
                    text = stringResource(Res.string.week_balance),
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = formatBalance(state.weekOverview.balanceMinutes),
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 50.sp,
                    lineHeight = 54.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        Res.string.week_summary,
                        state.weekOverview.weekNumber,
                        state.weekOverview.reached,
                        state.plannedWeek
                    ),
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                .size(36.dp)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .clickable(enabled = enabled, onClick = onClick),
            shape = CircleShape,
            color = AppColors.Background.copy(alpha = if (enabled) 0.96f else 0.52f)
        ) {
            Canvas(Modifier.padding(11.dp)) {
                val xStart = if (pointsRight) size.width * 0.34f else size.width * 0.66f
                val xEnd = if (pointsRight) size.width * 0.68f else size.width * 0.32f
                drawLine(
                    color = AppColors.Navy.copy(alpha = if (enabled) 1f else 0.36f),
                    start = Offset(xStart, size.height * 0.12f),
                    end = Offset(xEnd, size.height * 0.5f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = AppColors.Navy.copy(alpha = if (enabled) 1f else 0.36f),
                    start = Offset(xEnd, size.height * 0.5f),
                    end = Offset(xStart, size.height * 0.88f),
                    strokeWidth = 2.2.dp.toPx(),
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
                .padding(horizontal = 12.dp)
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
                                strokeWidth = 1.dp.toPx()
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    week.forEach { day ->
                        CalendarDay(
                            day = day,
                            selected = day.date == selectedDate,
                            onClick = { onSelectDate(day.date) },
                            onLongClick = if (day.isCurrentMonth && day.style != CalendarDayStyle.Planned) {
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
                .height(44.dp)
                .drawBehind {
                    drawLine(
                        color = AppColors.CalendarLine,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
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
                    color = AppColors.Navy,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
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
        val shape = RoundedCornerShape(12.dp)
        val isMuted = !day.isCurrentMonth || day.style == CalendarDayStyle.Weekend
        val border = if (day.isToday) BorderStroke(1.5.dp, AppColors.Coral) else null

        Surface(
            modifier = modifier
                .height(58.dp)
                .padding(horizontal = 2.dp, vertical = 3.dp)
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
                        isMuted -> AppColors.Navy.copy(alpha = 0.35f)
                        else -> AppColors.Navy
                    },
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 17.sp,
                    lineHeight = 19.sp,
                    fontWeight = if (selected || day.isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (day.isToday) {
                    Text(
                        text = stringResource(Res.string.nav_today),
                        color = AppColors.Coral,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                    CalendarDayMarker(day)
                }
            }
        }
    }

    @Composable
    private fun CalendarDayMarker(day: CalendarDayUiModel) {
        when {
            day.style == CalendarDayStyle.Vacation -> StatusBar(AppColors.Sand)
            day.style == CalendarDayStyle.Sick -> StatusDot(AppColors.Navy.copy(alpha = 0.72f))
            day.style == CalendarDayStyle.Holiday -> StatusDot(AppColors.Coral.copy(alpha = 0.76f))
            day.workedMinutes > 0 -> StatusBar(AppColors.Green.copy(alpha = 0.88f))
            else -> Spacer(Modifier.height(5.dp))
        }
    }

    @Composable
    private fun StatusBar(color: Color) {
        Box(
            Modifier
                .width(17.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(color)
        )
    }

    @Composable
    private fun StatusDot(color: Color) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }

    @Composable
    private fun CalendarLegend() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(stringResource(Res.string.worked), AppColors.Green, isDot = false)
            LegendItem(stringResource(Res.string.vacation), AppColors.Sand, isDot = false)
            LegendItem(stringResource(Res.string.sick), AppColors.Navy.copy(alpha = 0.72f), isDot = true)
        }
    }

    @Composable
    private fun LegendItem(label: String, color: Color, isDot: Boolean) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDot) StatusDot(color) else StatusBar(color)
            Text(
                text = label,
                color = AppColors.Navy,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                lineHeight = 14.sp
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
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedDayTitle(day),
                        color = AppColors.Navy,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedDaySubtitle(day),
                        color = AppColors.Navy,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (day.scheduledTargetMinutes > 0 && day.workedMinutes > 0) {
                    Text(
                        text = formatBalance(day.workedMinutes - day.scheduledTargetMinutes),
                        color = if (day.workedMinutes >= day.scheduledTargetMinutes) AppColors.Success else AppColors.Coral,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Navy,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(Res.string.edit_day),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
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
        var start by remember(day.date) { mutableStateOf(formatClock(initialStart)) }
        var pause by remember(day.date) { mutableStateOf(formatClock(initialPause)) }
        var end by remember(day.date) { mutableStateOf(formatClock(initialEnd)) }
        val startMinute = parseClock(start)
        val pauseMinutes = parseDuration(pause)
        val endMinute = parseClock(end)
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
                border = BorderStroke(1.dp, AppColors.CalendarLine),
                shape = RoundedCornerShape(22.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(Res.string.edit_workday),
                            color = AppColors.Navy,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Text(
                            text = selectedDayTitle(day),
                            color = AppColors.Navy.copy(alpha = 0.68f),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp
                        )
                    }
                    item { TimeInput(stringResource(Res.string.work_start), start) { start = normalizeTimeInput(it) } }
                    item { TimeInput(stringResource(Res.string.break_label), pause) { pause = normalizeTimeInput(it) } }
                    item { TimeInput(stringResource(Res.string.work_end), end) { end = normalizeTimeInput(it) } }
                    item {
                        Surface(color = AppColors.Green.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.working_time),
                                    color = AppColors.Success,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = worked?.let(::formatClockDuration) ?: "–",
                                    color = AppColors.Navy,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ActionButton("-15 min", onDecreaseDay, Modifier.weight(1f))
                            ActionButton("+15 min", onIncreaseDay, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    color = AppColors.Navy,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onSave(startMinute!!, pauseMinutes!!, endMinute!!) },
                                enabled = worked != null,
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.save),
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
            label = { Text(label, fontFamily = FontFamily.SansSerif) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Navy,
                focusedLabelColor = AppColors.Navy,
                cursorColor = AppColors.Navy
            )
        )
    }

    @Composable
    private fun ActionButton(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = AppColors.Navy
    ) {
        val contentColor = if (containerColor == AppColors.Sand) AppColors.Navy else containerColor
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor.copy(alpha = if (containerColor == AppColors.Sand) 0.72f else 0.16f),
                contentColor = contentColor
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
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

    private fun formatBalance(minutes: Int): String {
        val prefix = when {
            minutes > 0 -> "+"
            minutes < 0 -> "−"
            else -> ""
        }
        return prefix + formatClockDuration(abs(minutes))
    }

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

    private fun normalizeTimeInput(value: String): String = value.filter { it.isDigit() || it == ':' }.take(5)

    private fun Int?.orDefault(default: Int): Int = this ?: default

    private const val SwipeThreshold = 80f
}
