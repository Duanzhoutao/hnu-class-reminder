package com.hainanu.signinassistant

import com.google.common.truth.Truth.assertThat
import com.hainanu.signinassistant.domain.usecase.WeekCalculator
import java.time.LocalDate
import org.junit.Test

class WeekCalculatorTest {

    private val calculator = WeekCalculator()

    @Test
    fun returnsNullBeforeTermStarts() {
        val result = calculator.getWeekIndex(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 1))
        assertThat(result).isNull()
    }

    @Test
    fun returnsCorrectWeekIndex() {
        val result = calculator.getWeekIndex(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 16))
        assertThat(result).isEqualTo(3)
    }
}
