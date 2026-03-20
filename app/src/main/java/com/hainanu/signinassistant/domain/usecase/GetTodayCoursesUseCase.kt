package com.hainanu.signinassistant.domain.usecase

import com.hainanu.signinassistant.data.repository.DailyMuteRepository
import com.hainanu.signinassistant.data.repository.HolidayRepository
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.HolidayRule
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetTodayCoursesUseCase @Inject constructor(
    private val courseActiveUseCase: CourseActiveUseCase,
    private val holidayRepository: HolidayRepository,
    private val dailyMuteRepository: DailyMuteRepository,
) {

    suspend fun execute(
        courses: List<Course>,
        settings: AppSettings,
        date: LocalDate = LocalDate.now(),
    ): List<Course> {
        val termId = courses.firstOrNull()?.termId ?: return emptyList()
        val holidays: List<HolidayRule> = holidayRepository.getForTerm(termId)
        val dailyMuted = dailyMuteRepository.isMuted(date)
        return courses.filter { course ->
            courseActiveUseCase.isActive(
                course = course,
                date = date,
                settings = settings,
                holidays = holidays,
                dailyMuted = dailyMuted,
            )
        }
    }
}
