package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.repository.ReminderDebugRepository
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.domain.model.NotificationLog
import com.hainanu.signinassistant.domain.model.ParseError
import com.hainanu.signinassistant.domain.model.ScheduledReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DebugUiState(
    val scheduledReminders: List<ScheduledReminder> = emptyList(),
    val notificationLogs: List<NotificationLog> = emptyList(),
    val parseErrors: List<ParseError> = emptyList(),
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    reminderDebugRepository: ReminderDebugRepository,
    timetableRepository: TimetableRepository,
) : ViewModel() {

    val uiState = combine(
        reminderDebugRepository.scheduledFlow,
        reminderDebugRepository.notificationLogsFlow,
        timetableRepository.parseErrorsFlow,
    ) { reminders, logs, errors ->
        DebugUiState(reminders, logs, errors)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugUiState())
}
