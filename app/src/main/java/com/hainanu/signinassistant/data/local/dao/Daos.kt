package com.hainanu.signinassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hainanu.signinassistant.data.local.entity.CourseEntity
import com.hainanu.signinassistant.data.local.entity.CourseNoteEntity
import com.hainanu.signinassistant.data.local.entity.DailyMuteEntity
import com.hainanu.signinassistant.data.local.entity.HolidayEntity
import com.hainanu.signinassistant.data.local.entity.NotificationLogEntity
import com.hainanu.signinassistant.data.local.entity.ParseErrorEntity
import com.hainanu.signinassistant.data.local.entity.ScheduledReminderEntity
import com.hainanu.signinassistant.data.local.entity.SectionSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY weekday, startSection, endSection, courseName")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY weekday, startSection, endSection, courseName")
    suspend fun getAll(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    fun observeById(courseId: Long): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getById(courseId: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CourseEntity>): List<Long>

    @Query("DELETE FROM courses")
    suspend fun clear()

    @Query("UPDATE courses SET remindersEnabled = :enabled WHERE id = :courseId")
    suspend fun updateReminderEnabled(courseId: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM courses")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun count(): Int
}

@Dao
interface SectionSlotDao {
    @Query("SELECT * FROM section_slots ORDER BY startSection")
    fun observeAll(): Flow<List<SectionSlotEntity>>

    @Query("SELECT * FROM section_slots ORDER BY startSection")
    suspend fun getAll(): List<SectionSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SectionSlotEntity>)

    @Query("DELETE FROM section_slots")
    suspend fun clear()
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE termId = :termId ORDER BY date")
    fun observeForTerm(termId: String): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE termId = :termId ORDER BY date")
    suspend fun getForTerm(termId: String): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE termId = :termId")
    suspend fun clearForTerm(termId: String)
}

@Dao
interface DailyMuteDao {
    @Query("SELECT * FROM daily_mutes ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyMuteEntity>>

    @Query("SELECT * FROM daily_mutes WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyMuteEntity?>

    @Query("SELECT * FROM daily_mutes WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyMuteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DailyMuteEntity)

    @Query("DELETE FROM daily_mutes WHERE date < :date")
    suspend fun deleteBefore(date: String)
}

@Dao
interface ParseErrorDao {
    @Query("SELECT * FROM parse_errors ORDER BY id DESC")
    fun observeAll(): Flow<List<ParseErrorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ParseErrorEntity>)

    @Query("DELETE FROM parse_errors")
    suspend fun clear()
}

@Dao
interface CourseNoteDao {
    @Query("SELECT * FROM course_notes ORDER BY title")
    fun observeAll(): Flow<List<CourseNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CourseNoteEntity>)

    @Query("DELETE FROM course_notes")
    suspend fun clear()
}

@Dao
interface ScheduledReminderDao {
    @Query("SELECT * FROM scheduled_reminders ORDER BY triggerAt")
    fun observeAll(): Flow<List<ScheduledReminderEntity>>

    @Query("SELECT * FROM scheduled_reminders ORDER BY triggerAt")
    suspend fun getAll(): List<ScheduledReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScheduledReminderEntity>)

    @Query("DELETE FROM scheduled_reminders")
    suspend fun clear()
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY triggeredAt DESC LIMIT 50")
    fun observeLatest(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotificationLogEntity)

    @Query("DELETE FROM notification_logs WHERE triggeredAt < :threshold")
    suspend fun deleteBefore(threshold: String)
}
