package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.data.term.AcademicTermDefaults
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.SectionSlot
import com.hainanu.signinassistant.domain.usecase.WeekCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WeekScheduleUiState(
    val weekIndex: Int? = null,
    val groupedCourses: Map<Int, List<Course>> = emptyMap(),
    val sectionSlots: List<SectionSlot> = emptyList(),
)

@HiltViewModel
class WeekScheduleViewModel @Inject constructor(
    timetableRepository: TimetableRepository,
    settingsRepository: SettingsRepository,
    private val weekCalculator: WeekCalculator,
) : ViewModel() {

    val uiState = combine(
        timetableRepository.snapshotFlow,
        settingsRepository.settingsFlow,
    ) { snapshot, settings ->
        val termId = snapshot.courses.firstOrNull()?.termId
        val firstWeekMonday = settings.firstWeekMonday ?: termId?.let(AcademicTermDefaults::firstWeekMondayFor)
        val weekIndex = weekCalculator.getWeekIndex(firstWeekMonday, LocalDate.now())
        val grouped = if (weekIndex == null) {
            emptyMap()
        } else {
            snapshot.courses
                .filter { weekIndex in it.weeks }
                .groupBy { it.weekday }
                .toSortedMap()
        }
        WeekScheduleUiState(
            weekIndex = weekIndex,
            groupedCourses = grouped,
            sectionSlots = snapshot.sectionSlots,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekScheduleUiState())
}
