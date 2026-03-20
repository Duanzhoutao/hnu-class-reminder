package com.hainanu.signinassistant.data.term

import java.time.LocalDate

object AcademicTermDefaults {
    private val firstWeekMondays = mapOf(
        "2025-2026-2" to LocalDate.of(2026, 3, 2),
    )

    fun firstWeekMondayFor(termId: String): LocalDate? = firstWeekMondays[termId]
}
