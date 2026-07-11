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

class CalendarViewModel(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val stateMapper: CalendarStateMapper,
    private val editDay: EditCalendarDayUseCase = EditCalendarDayUseCase(repository)
) : ViewModel() {
    private val ticker = MutableStateFlow(0)

    val uiState: StateFlow<CalendarUiState> = combine(repository.history, ticker) { history, _ ->
        val snapshot = timeProvider.now()
        stateMapper.map(history, snapshot)
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

    private fun initialState(): CalendarUiState {
        val history = repository.history.value
        val snapshot = timeProvider.now()
        return stateMapper.map(history, snapshot)
    }
}
