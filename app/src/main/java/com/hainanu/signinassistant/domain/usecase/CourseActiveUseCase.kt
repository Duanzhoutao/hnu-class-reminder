package com.hainanu.signinassistant.domain.usecase

import com.hainanu.signinassistant.data.term.AcademicTermDefaults
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.HolidayRule
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseActiveUseCase @Inject constructor(
    private val weekCalculator: WeekCalculator,
    private val holidayResolver: HolidayResolver,
) {

    fun isActive(
        course: Course,
        date: LocalDate,
        settings: AppSettings,
        holidays: List<HolidayRule>,
        dailyMuted: Boolean,
    ): Boolean {
        if (dailyMuted) return false
        if (!course.remindersEnabled) return false
        if (holidayResolver.isHoliday(date, holidays)) return false
        val academicWeekday = holidayResolver.resolveAcademicWeekday(date, holidays)
        val firstWeekMonday = settings.firstWeekMonday ?: AcademicTermDefaults.firstWeekMondayFor(course.termId)
        val weekIndex = weekCalculator.getWeekIndex(firstWeekMonday, date) ?: return false
        return course.weekday == academicWeekday && weekIndex in course.weeks
    }
}
