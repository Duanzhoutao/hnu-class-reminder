package com.hainanu.signinassistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseName: String,
    val teacherName: String?,
    val location: String?,
    val weekday: Int,
    val startSection: Int,
    val endSection: Int,
    val weeksEncoded: String,
    val rawText: String,
    val termId: String,
    val remindersEnabled: Boolean,
)

@Entity(tableName = "section_slots")
data class SectionSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val termId: String,
    val label: String,
    val startSection: Int,
    val endSection: Int,
    val startTime: String,
    val endTime: String,
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = false) val date: String,
    val type: String,
    val makeupWeekday: Int?,
    val termId: String,
    val title: String?,
)

@Entity(tableName = "daily_mutes")
data class DailyMuteEntity(
    @PrimaryKey(autoGenerate = false) val date: String,
    val enabled: Boolean,
)

@Entity(tableName = "parse_errors")
data class ParseErrorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rowIndex: Int,
    val colIndex: Int,
    val rawText: String,
    val errorMessage: String,
    val termId: String,
)

@Entity(tableName = "course_notes")
data class CourseNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val teacherName: String?,
    val weeksEncoded: String,
    val rawText: String,
    val termId: String,
)

@Entity(tableName = "scheduled_reminders")
data class ScheduledReminderEntity(
    @PrimaryKey(autoGenerate = false) val requestCode: Int,
    val courseId: Long,
    val courseName: String,
    val reminderType: String,
    val reminderTier: String,
    val triggerAt: String,
    val date: String,
    val title: String,
    val body: String,
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestCode: Int,
    val courseName: String,
    val reminderType: String,
    val reminderTier: String,
    val triggeredAt: String,
)
