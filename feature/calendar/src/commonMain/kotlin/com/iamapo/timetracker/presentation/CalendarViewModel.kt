package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.EditCalendarDayUseCase
import com.iamapo.timetracker.presentation.state.CalendarUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class CalendarViewModel(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val stateMapper: CalendarStateMapper,
    private val editDay: EditCalendarDayUseCase = EditCalendarDayUseCase(repository)
) : ViewModel() {
    private val ticker = MutableStateFlow(0)
    private val displayedMonth = MutableStateFlow(firstOfMonth(timeProvider.now().date))

    val uiState: StateFlow<CalendarUiState> = combine(repository.history, ticker, displayedMonth) { history, _, month ->
        val snapshot = timeProvider.now()
        stateMapper.map(history, snapshot, month)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                ticker.update { it + 1 }
            }
        }
    }

    fun increaseDay(date: LocalDate) = editDay.increaseDay(date)
    fun decreaseDay(date: LocalDate) = editDay.decreaseDay(date)
    fun setVacation(date: LocalDate) = editDay.setVacation(date)
    fun setSick(date: LocalDate) = editDay.setSick(date)
    fun setForgottenWorkDay(date: LocalDate) = editDay.setForgottenWorkDay(date)
    fun clearDay(date: LocalDate) = editDay.clearDay(date)
    fun setWorkTimes(date: LocalDate, startMinute: Int, breakMinutes: Int, endMinute: Int) =
        editDay.setWorkTimes(date, startMinute, breakMinutes, endMinute)

    fun showPreviousMonth() {
        displayedMonth.update { it - DatePeriod(months = 1) }
    }

    fun showNextMonth() {
        val currentMonth = firstOfMonth(timeProvider.now().date)
        displayedMonth.update { month ->
            (month + DatePeriod(months = 1)).coerceAtMost(currentMonth)
        }
    }

    private fun initialState(): CalendarUiState {
        val history = repository.history.value
        val snapshot = timeProvider.now()
        return stateMapper.map(history, snapshot, displayedMonth.value)
    }


    private fun firstOfMonth(date: LocalDate) = LocalDate(date.year, date.month, 1)
}
