package com.hainanu.signinassistant

import com.google.common.truth.Truth.assertThat
import com.hainanu.signinassistant.data.parser.WeekPatternParser
import org.junit.Test

class WeekPatternParserTest {

    @Test
    fun parseContinuousWeeks() {
        assertThat(WeekPatternParser.parse("1-4周")).containsExactly(1, 2, 3, 4)
    }

    @Test
    fun parseSeparatedRanges() {
        assertThat(WeekPatternParser.parse("1-3,5-6周")).containsExactly(1, 2, 3, 5, 6)
    }

    @Test
    fun parseOddWeeks() {
        assertThat(WeekPatternParser.parse("1-8(单周)")).containsExactly(1, 3, 5, 7)
    }

    @Test
    fun parseEvenWeeks() {
        assertThat(WeekPatternParser.parse("2-10(双周)")).containsExactly(2, 4, 6, 8, 10)
    }

    @Test
    fun parseDiscreteWeeks() {
        assertThat(WeekPatternParser.parse("1,3,5周")).containsExactly(1, 3, 5)
    }
}
