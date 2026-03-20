package com.hainanu.signinassistant.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeekCalculator @Inject constructor() {
    fun getWeekIndex(firstWeekMonday: LocalDate?, date: LocalDate): Int? {
        if (firstWeekMonday == null || date.isBefore(firstWeekMonday)) return null
        return (ChronoUnit.DAYS.between(firstWeekMonday, date) / 7).toInt() + 1
    }
}
