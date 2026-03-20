package com.hainanu.signinassistant.domain.usecase

import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.ReminderMoment
import com.hainanu.signinassistant.domain.model.ReminderType
import com.hainanu.signinassistant.domain.model.SectionSlot
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderMomentBuilder @Inject constructor() {

    fun build(
        course: Course,
        date: LocalDate,
        settings: AppSettings,
        sectionSlots: List<SectionSlot>,
    ): List<ReminderMoment> {
        val slot = sectionSlots.firstOrNull {
            it.startSection == course.startSection && it.endSection == course.endSection
        } ?: return emptyList()

        val courseStart = date.atTime(slot.startTime)
        val courseEnd = date.atTime(slot.endTime)
        return buildList {
            if (settings.preClassReminderEnabled) {
                add(
                    ReminderMoment(
                        type = ReminderType.PRE_CLASS,
                        tier = settings.reminderTier,
                        triggerAt = courseStart.minusMinutes(settings.preClassMinutes.toLong()),
                        title = "${settings.preClassMinutes} 分钟后上课",
                        body = "《${course.courseName}》\n地点：${course.location ?: "待确认"}",
                    ),
                )
            }
            if (settings.onClassReminderEnabled) {
                add(
                    ReminderMoment(
                        type = ReminderType.ON_CLASS,
                        tier = settings.reminderTier,
                        triggerAt = courseStart,
                        title = "课程开始了",
                        body = "《${course.courseName}》\n这节课可能有签到",
                    ),
                )
            }
            if (settings.postClassReminderEnabled) {
                add(
                    ReminderMoment(
                        type = ReminderType.POST_CLASS,
                        tier = settings.reminderTier,
                        triggerAt = courseEnd.plusMinutes(settings.postClassMinutes.toLong()),
                        title = "已开课 ${settings.postClassMinutes} 分钟",
                        body = "《${course.courseName}》\n如果还没签到请尽快处理",
                    ),
                )
            }
        }.filter { it.triggerAt.isAfter(java.time.LocalDateTime.now()) }
    }
}
