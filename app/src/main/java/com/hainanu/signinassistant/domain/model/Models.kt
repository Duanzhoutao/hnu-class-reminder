package com.hainanu.signinassistant.domain.model

import android.net.Uri
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ReminderType {
    PRE_CLASS,
    ON_CLASS,
    POST_CLASS,
}

enum class ReminderTier {
    NORMAL,
    STANDARD,
    STRONG,
}

enum class HolidayType {
    HOLIDAY,
    MAKEUP_WORKDAY,
    NORMAL,
}

data class Course(
    val id: Long,
    val courseName: String,
    val teacherName: String?,
    val location: String?,
    val weekday: Int,
    val startSection: Int,
    val endSection: Int,
    val weeks: Set<Int>,
    val rawText: String,
    val termId: String,
    val remindersEnabled: Boolean,
)

data class CourseNote(
    val id: Long,
    val title: String,
    val teacherName: String?,
    val weeks: Set<Int>,
    val rawText: String,
    val termId: String,
)

data class SectionSlot(
    val id: Long,
    val termId: String,
    val label: String,
    val startSection: Int,
    val endSection: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
)

data class HolidayRule(
    val date: LocalDate,
    val type: HolidayType,
    val makeupWeekday: Int?,
    val termId: String,
    val title: String?,
)

data class ParseError(
    val id: Long,
    val rowIndex: Int,
    val colIndex: Int,
    val rawText: String,
    val errorMessage: String,
    val termId: String,
)

data class ScheduledReminder(
    val requestCode: Int,
    val courseId: Long,
    val courseName: String,
    val reminderType: ReminderType,
    val reminderTier: ReminderTier,
    val triggerAt: LocalDateTime,
    val date: LocalDate,
    val title: String,
    val body: String,
)

data class NotificationLog(
    val id: Long,
    val requestCode: Int,
    val courseName: String,
    val reminderType: ReminderType,
    val reminderTier: ReminderTier,
    val triggeredAt: LocalDateTime,
)

data class AppSettings(
    val firstWeekMonday: LocalDate? = null,
    val preClassReminderEnabled: Boolean = true,
    val onClassReminderEnabled: Boolean = true,
    val postClassReminderEnabled: Boolean = true,
    val preClassMinutes: Int = 20,
    val postClassMinutes: Int = 5,
    val reminderTier: ReminderTier = ReminderTier.STANDARD,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val soundUri: String? = null,
    val exactAlarmHintDismissed: Boolean = false,
    val notificationPermissionPrompted: Boolean = false,
)

data class ReminderMoment(
    val type: ReminderType,
    val tier: ReminderTier,
    val triggerAt: LocalDateTime,
    val title: String,
    val body: String,
)

data class ImportedCourseDraft(
    val courseName: String,
    val teacherName: String?,
    val location: String?,
    val weekday: Int,
    val startSection: Int,
    val endSection: Int,
    val weeks: Set<Int>,
    val rawText: String,
)

data class ImportedCourseNoteDraft(
    val title: String,
    val teacherName: String?,
    val weeks: Set<Int>,
    val rawText: String,
)

data class ImportedTimetableBundle(
    val courses: List<ImportedCourseDraft>,
    val sectionSlots: List<SectionSlot>,
    val notes: List<ImportedCourseNoteDraft>,
    val parseErrors: List<ParseError>,
    val termId: String,
    val sourceFileName: String,
    val sourceUri: Uri,
)
