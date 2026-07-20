package com.iamapo.timetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamapo.timetracker.domain.TimeProvider
import com.iamapo.timetracker.domain.repository.WorkHistoryRepository
import com.iamapo.timetracker.domain.usecase.DeleteWorkEntriesUseCase
import com.iamapo.timetracker.domain.usecase.UpdateWorkSettingsUseCase
import com.iamapo.timetracker.presentation.state.SettingsUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: WorkHistoryRepository,
    private val timeProvider: TimeProvider,
    private val updateSettings: UpdateWorkSettingsUseCase = UpdateWorkSettingsUseCase(repository, timeProvider),
    private val deleteWorkEntries: DeleteWorkEntriesUseCase = DeleteWorkEntriesUseCase(repository)
) : ViewModel() {
    val uiState: StateFlow<SettingsUiModel> = repository.history
        .map(::mapState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = mapState(repository.history.value)
        )

    fun increaseDailyTarget() = updateSettings.increaseDailyTarget()
    fun decreaseDailyTarget() = updateSettings.decreaseDailyTarget()
    fun increaseRequiredBreak() = updateSettings.increaseRequiredBreak()
    fun decreaseRequiredBreak() = updateSettings.decreaseRequiredBreak()
    fun increaseWeeklyTarget() = updateSettings.increaseWeeklyTarget()
    fun decreaseWeeklyTarget() = updateSettings.decreaseWeeklyTarget()
    fun setLockScreenStatusEnabled(enabled: Boolean) = updateSettings.setLockScreenStatusEnabled(enabled)
    fun deleteAllEntries() = deleteWorkEntries()

    private fun mapState(history: com.iamapo.timetracker.domain.WorkHistory): SettingsUiModel {
        val day = history.dayWithWeeklySummary(timeProvider.now().date)
        return SettingsStateMapper.map(day.config, history.lockScreenStatusEnabled)
    }
}
