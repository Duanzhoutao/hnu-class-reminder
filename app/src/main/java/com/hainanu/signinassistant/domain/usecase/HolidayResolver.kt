package com.hainanu.signinassistant.domain.usecase

import com.hainanu.signinassistant.domain.model.HolidayRule
import com.hainanu.signinassistant.domain.model.HolidayType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayResolver @Inject constructor() {

    fun resolveAcademicWeekday(date: LocalDate, holidays: List<HolidayRule>): Int {
        val rule = holidays.firstOrNull { it.date == date }
        return if (rule?.type == HolidayType.MAKEUP_WORKDAY && rule.makeupWeekday != null) {
            rule.makeupWeekday
        } else {
            date.dayOfWeek.value
        }
    }

    fun isHoliday(date: LocalDate, holidays: List<HolidayRule>): Boolean {
        return holidays.firstOrNull { it.date == date }?.type == HolidayType.HOLIDAY
    }
}
