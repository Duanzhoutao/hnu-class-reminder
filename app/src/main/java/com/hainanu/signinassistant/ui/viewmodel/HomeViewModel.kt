package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.repository.DailyMuteRepository
import com.hainanu.signinassistant.data.repository.HolidayRepository
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.data.term.AcademicTermDefaults
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.SectionSlot
import com.hainanu.signinassistant.domain.usecase.RescheduleAllRemindersUseCase
import com.hainanu.signinassistant.domain.usecase.WeekCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val weekIndex: Int? = null,
    val today: LocalDate = LocalDate.now(),
    val todayCourses: List<Course> = emptyList(),
    val sectionSlots: List<SectionSlot> = emptyList(),
    val dailyMuted: Boolean = false,
    val holidayConfigured: Boolean = true,
    val notesCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    timetableRepository: TimetableRepository,
    settingsRepository: SettingsRepository,
    private val dailyMuteRepository: DailyMuteRepository,
    private val holidayRepository: HolidayRepository,
    private val weekCalculator: WeekCalculator,
    private val rescheduleAllRemindersUseCase: RescheduleAllRemindersUseCase,
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState = combine(
        timetableRepository.snapshotFlow,
        settingsRepository.settingsFlow,
        dailyMuteRepository.observe(today),
    ) { snapshot, settings, dailyMuted ->
        val termId = snapshot.courses.firstOrNull()?.termId
        val holidays = termId?.let { holidayRepository.getForTerm(it) }.orEmpty()
        val firstWeekMonday = settings.firstWeekMonday ?: termId?.let(AcademicTermDefaults::firstWeekMondayFor)
        val weekIndex = weekCalculator.getWeekIndex(firstWeekMonday, today)
        val holidayRule = holidays.firstOrNull { it.date == today }
        val academicWeekday = holidayRule?.makeupWeekday ?: today.dayOfWeek.value
        val holiday = holidayRule?.type?.name == "HOLIDAY"
        val todayCourses = snapshot.courses.filter { course ->
            !dailyMuted &&
                !holiday &&
                weekIndex != null &&
                course.remindersEnabled &&
                course.weekday == academicWeekday &&
                weekIndex in course.weeks
        }
        HomeUiState(
            weekIndex = weekIndex,
            today = today,
            todayCourses = todayCourses,
            sectionSlots = snapshot.sectionSlots,
            dailyMuted = dailyMuted,
            holidayConfigured = termId == null || holidays.isNotEmpty(),
            notesCount = snapshot.notes.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setDailyMute(enabled: Boolean) {
        viewModelScope.launch {
            dailyMuteRepository.setMuted(today, enabled)
            rescheduleAllRemindersUseCase("daily_mute_changed")
        }
    }
}
