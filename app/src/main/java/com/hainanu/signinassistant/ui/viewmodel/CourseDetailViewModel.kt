package com.hainanu.signinassistant.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.SectionSlot
import com.hainanu.signinassistant.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val sectionSlot: SectionSlot? = null,
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timetableRepository: TimetableRepository,
    private val rescheduleAllRemindersUseCase: RescheduleAllRemindersUseCase,
) : ViewModel() {

    private val courseId: Long = checkNotNull(savedStateHandle["courseId"])

    val uiState = combine(
        timetableRepository.observeCourse(courseId),
        timetableRepository.sectionSlotsFlow,
    ) { course, sectionSlots ->
        CourseDetailUiState(
            course = course,
            sectionSlot = course?.let { item ->
                sectionSlots.firstOrNull {
                    it.startSection == item.startSection && it.endSection == item.endSection
                }
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState())

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timetableRepository.updateCourseReminderEnabled(courseId, enabled)
            rescheduleAllRemindersUseCase("course_reminder_toggled")
        }
    }
}
