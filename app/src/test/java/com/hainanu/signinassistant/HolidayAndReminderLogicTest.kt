package com.hainanu.signinassistant

import com.google.common.truth.Truth.assertThat
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.HolidayRule
import com.hainanu.signinassistant.domain.model.HolidayType
import com.hainanu.signinassistant.domain.model.SectionSlot
import com.hainanu.signinassistant.domain.usecase.CourseActiveUseCase
import com.hainanu.signinassistant.domain.usecase.HolidayResolver
import com.hainanu.signinassistant.domain.usecase.ReminderMomentBuilder
import com.hainanu.signinassistant.domain.usecase.WeekCalculator
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Test

class HolidayAndReminderLogicTest {

    private val activeUseCase = CourseActiveUseCase(WeekCalculator(), HolidayResolver())
    private val reminderMomentBuilder = ReminderMomentBuilder()

    private val sampleCourse = Course(
        id = 1,
        courseName = "神经网络与深度学习",
        teacherName = "赵瑶池",
        location = "(海甸)4-209",
        weekday = 2,
        startSection = 3,
        endSection = 4,
        weeks = (1..16).toSet(),
        rawText = "",
        termId = "2025-2026-2",
        remindersEnabled = true,
    )

    @Test
    fun holidayBlocksCourse() {
        val active = activeUseCase.isActive(
            course = sampleCourse,
            date = LocalDate.of(2026, 5, 1),
            settings = AppSettings(firstWeekMonday = LocalDate.of(2026, 3, 2)),
            holidays = listOf(
                HolidayRule(
                    date = LocalDate.of(2026, 5, 1),
                    type = HolidayType.HOLIDAY,
                    makeupWeekday = null,
                    termId = "2025-2026-2",
                    title = "劳动节",
                ),
            ),
            dailyMuted = false,
        )

        assertThat(active).isFalse()
    }

    @Test
    fun reminderBuilderCreatesThreeMoments() {
        val moments = reminderMomentBuilder.build(
            course = sampleCourse,
            date = LocalDate.now().plusDays(1),
            settings = AppSettings(firstWeekMonday = LocalDate.of(2026, 3, 2)),
            sectionSlots = listOf(
                SectionSlot(
                    id = 1,
                    termId = "2025-2026-2",
                    label = "3、4节",
                    startSection = 3,
                    endSection = 4,
                    startTime = LocalTime.of(9, 45),
                    endTime = LocalTime.of(11, 25),
                ),
            ),
        )

        assertThat(moments).hasSize(3)
    }

    @Test
    fun makeupWorkdayMapsToAnotherWeekday() {
        val mondayCourse = sampleCourse.copy(weekday = 5)
        val active = activeUseCase.isActive(
            course = mondayCourse,
            date = LocalDate.of(2026, 4, 11),
            settings = AppSettings(firstWeekMonday = LocalDate.of(2026, 3, 2)),
            holidays = listOf(
                HolidayRule(
                    date = LocalDate.of(2026, 4, 11),
                    type = HolidayType.MAKEUP_WORKDAY,
                    makeupWeekday = 5,
                    termId = "2025-2026-2",
                    title = "周六补周五课程",
                ),
            ),
            dailyMuted = false,
        )

        assertThat(active).isTrue()
    }

    @Test
    fun onlyOneHalfSemesterCourseMatchesInSameSlot() {
        val firstHalfCourse = sampleCourse.copy(
            id = 2,
            courseName = "大数据可视化",
            weekday = 5,
            weeks = (1..12).toSet(),
        )
        val secondHalfCourse = sampleCourse.copy(
            id = 3,
            courseName = "大数据安全与隐私保护",
            weekday = 5,
            weeks = (13..16).toSet(),
        )
        val settings = AppSettings(firstWeekMonday = LocalDate.of(2026, 3, 2))

        val weekThreeDate = LocalDate.of(2026, 3, 20)
        val weekFourteenDate = LocalDate.of(2026, 6, 5)

        assertThat(
            activeUseCase.isActive(
                course = firstHalfCourse,
                date = weekThreeDate,
                settings = settings,
                holidays = emptyList(),
                dailyMuted = false,
            ),
        ).isTrue()
        assertThat(
            activeUseCase.isActive(
                course = secondHalfCourse,
                date = weekThreeDate,
                settings = settings,
                holidays = emptyList(),
                dailyMuted = false,
            ),
        ).isFalse()
        assertThat(
            activeUseCase.isActive(
                course = firstHalfCourse,
                date = weekFourteenDate,
                settings = settings,
                holidays = emptyList(),
                dailyMuted = false,
            ),
        ).isFalse()
        assertThat(
            activeUseCase.isActive(
                course = secondHalfCourse,
                date = weekFourteenDate,
                settings = settings,
                holidays = emptyList(),
                dailyMuted = false,
            ),
        ).isTrue()
    }

    @Test
    fun knownTermDefaultIsUsedWhenFirstWeekMondayMissing() {
        val active = activeUseCase.isActive(
            course = sampleCourse.copy(weekday = 5),
            date = LocalDate.of(2026, 3, 20),
            settings = AppSettings(firstWeekMonday = null),
            holidays = emptyList(),
            dailyMuted = false,
        )

        assertThat(active).isTrue()
    }
}
