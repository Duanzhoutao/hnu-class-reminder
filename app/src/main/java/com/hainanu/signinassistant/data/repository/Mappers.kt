package com.hainanu.signinassistant.data.repository

import com.hainanu.signinassistant.data.local.entity.CourseEntity
import com.hainanu.signinassistant.data.local.entity.CourseNoteEntity
import com.hainanu.signinassistant.data.local.entity.HolidayEntity
import com.hainanu.signinassistant.data.local.entity.NotificationLogEntity
import com.hainanu.signinassistant.data.local.entity.ParseErrorEntity
import com.hainanu.signinassistant.data.local.entity.ScheduledReminderEntity
import com.hainanu.signinassistant.data.local.entity.SectionSlotEntity
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.CourseNote
import com.hainanu.signinassistant.domain.model.HolidayRule
import com.hainanu.signinassistant.domain.model.HolidayType
import com.hainanu.signinassistant.domain.model.ImportedCourseDraft
import com.hainanu.signinassistant.domain.model.ImportedCourseNoteDraft
import com.hainanu.signinassistant.domain.model.NotificationLog
import com.hainanu.signinassistant.domain.model.ParseError
import com.hainanu.signinassistant.domain.model.ReminderTier
import com.hainanu.signinassistant.domain.model.ReminderType
import com.hainanu.signinassistant.domain.model.ScheduledReminder
import com.hainanu.signinassistant.domain.model.SectionSlot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun Set<Int>.encodeWeeks(): String = sorted().joinToString(",")

fun String.decodeWeeks(): Set<Int> = split(",")
    .mapNotNull { it.trim().takeIf(String::isNotBlank)?.toIntOrNull() }
    .toSet()

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    courseName = courseName,
    teacherName = teacherName,
    location = location,
    weekday = weekday,
    startSection = startSection,
    endSection = endSection,
    weeks = weeksEncoded.decodeWeeks(),
    rawText = rawText,
    termId = termId,
    remindersEnabled = remindersEnabled,
)

fun ImportedCourseDraft.toEntity(termId: String): CourseEntity = CourseEntity(
    courseName = courseName,
    teacherName = teacherName,
    location = location,
    weekday = weekday,
    startSection = startSection,
    endSection = endSection,
    weeksEncoded = weeks.encodeWeeks(),
    rawText = rawText,
    termId = termId,
    remindersEnabled = true,
)

fun SectionSlotEntity.toDomain(): SectionSlot = SectionSlot(
    id = id,
    termId = termId,
    label = label,
    startSection = startSection,
    endSection = endSection,
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
)

fun SectionSlot.toEntity(): SectionSlotEntity = SectionSlotEntity(
    id = id,
    termId = termId,
    label = label,
    startSection = startSection,
    endSection = endSection,
    startTime = startTime.toString(),
    endTime = endTime.toString(),
)

fun HolidayEntity.toDomain(): HolidayRule = HolidayRule(
    date = LocalDate.parse(date),
    type = HolidayType.valueOf(type),
    makeupWeekday = makeupWeekday,
    termId = termId,
    title = title,
)

fun HolidayRule.toEntity(): HolidayEntity = HolidayEntity(
    date = date.toString(),
    type = type.name,
    makeupWeekday = makeupWeekday,
    termId = termId,
    title = title,
)

fun ParseErrorEntity.toDomain(): ParseError = ParseError(
    id = id,
    rowIndex = rowIndex,
    colIndex = colIndex,
    rawText = rawText,
    errorMessage = errorMessage,
    termId = termId,
)

fun ParseError.toEntity(): ParseErrorEntity = ParseErrorEntity(
    id = id,
    rowIndex = rowIndex,
    colIndex = colIndex,
    rawText = rawText,
    errorMessage = errorMessage,
    termId = termId,
)

fun CourseNoteEntity.toDomain(): CourseNote = CourseNote(
    id = id,
    title = title,
    teacherName = teacherName,
    weeks = weeksEncoded.decodeWeeks(),
    rawText = rawText,
    termId = termId,
)

fun ImportedCourseNoteDraft.toEntity(termId: String): CourseNoteEntity = CourseNoteEntity(
    title = title,
    teacherName = teacherName,
    weeksEncoded = weeks.encodeWeeks(),
    rawText = rawText,
    termId = termId,
)

fun ScheduledReminderEntity.toDomain(): ScheduledReminder = ScheduledReminder(
    requestCode = requestCode,
    courseId = courseId,
    courseName = courseName,
    reminderType = ReminderType.valueOf(reminderType),
    reminderTier = ReminderTier.valueOf(reminderTier),
    triggerAt = LocalDateTime.parse(triggerAt),
    date = LocalDate.parse(date),
    title = title,
    body = body,
)

fun ScheduledReminder.toEntity(): ScheduledReminderEntity = ScheduledReminderEntity(
    requestCode = requestCode,
    courseId = courseId,
    courseName = courseName,
    reminderType = reminderType.name,
    reminderTier = reminderTier.name,
    triggerAt = triggerAt.toString(),
    date = date.toString(),
    title = title,
    body = body,
)

fun NotificationLogEntity.toDomain(): NotificationLog = NotificationLog(
    id = id,
    requestCode = requestCode,
    courseName = courseName,
    reminderType = ReminderType.valueOf(reminderType),
    reminderTier = ReminderTier.valueOf(reminderTier),
    triggeredAt = LocalDateTime.parse(triggeredAt),
)

fun NotificationLog.toEntity(): NotificationLogEntity = NotificationLogEntity(
    id = id,
    requestCode = requestCode,
    courseName = courseName,
    reminderType = reminderType.name,
    reminderTier = reminderTier.name,
    triggeredAt = triggeredAt.toString(),
)
