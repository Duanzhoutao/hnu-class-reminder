package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.ReminderTier
import com.hainanu.signinassistant.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val rescheduleAllRemindersUseCase: RescheduleAllRemindersUseCase,
) : ViewModel() {

    val uiState = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun updateFirstWeekMonday(date: LocalDate) {
        viewModelScope.launch {
            settingsRepository.updateFirstWeekMonday(date)
            rescheduleAllRemindersUseCase("first_week_changed")
        }
    }

    fun updateTier(tier: ReminderTier) {
        viewModelScope.launch {
            settingsRepository.updateReminderTier(tier)
            rescheduleAllRemindersUseCase("reminder_tier_changed")
        }
    }

    fun updateReminderSwitches(pre: Boolean? = null, on: Boolean? = null, post: Boolean? = null) {
        viewModelScope.launch {
            settingsRepository.updateReminderSwitches(pre, on, post)
            rescheduleAllRemindersUseCase("reminder_switches_changed")
        }
    }

    fun updateSound(vibrationEnabled: Boolean? = null, soundEnabled: Boolean? = null) {
        viewModelScope.launch {
            settingsRepository.updateSoundOptions(vibrationEnabled, soundEnabled)
        }
    }

    fun markNotificationPermissionPrompted() {
        viewModelScope.launch {
            settingsRepository.markNotificationPermissionPrompted()
        }
    }
}
