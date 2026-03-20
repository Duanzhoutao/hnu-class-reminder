package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RootViewModel @Inject constructor(
    timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val hasImported = timetableRepository.hasImportedTimetableFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val settings = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun markNotificationPermissionPrompted() {
        viewModelScope.launch {
            settingsRepository.markNotificationPermissionPrompted()
        }
    }
}
