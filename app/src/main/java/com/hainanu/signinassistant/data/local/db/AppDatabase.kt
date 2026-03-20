package com.hainanu.signinassistant.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hainanu.signinassistant.data.local.dao.CourseDao
import com.hainanu.signinassistant.data.local.dao.CourseNoteDao
import com.hainanu.signinassistant.data.local.dao.DailyMuteDao
import com.hainanu.signinassistant.data.local.dao.HolidayDao
import com.hainanu.signinassistant.data.local.dao.NotificationLogDao
import com.hainanu.signinassistant.data.local.dao.ParseErrorDao
import com.hainanu.signinassistant.data.local.dao.ScheduledReminderDao
import com.hainanu.signinassistant.data.local.dao.SectionSlotDao
import com.hainanu.signinassistant.data.local.entity.CourseEntity
import com.hainanu.signinassistant.data.local.entity.CourseNoteEntity
import com.hainanu.signinassistant.data.local.entity.DailyMuteEntity
import com.hainanu.signinassistant.data.local.entity.HolidayEntity
import com.hainanu.signinassistant.data.local.entity.NotificationLogEntity
import com.hainanu.signinassistant.data.local.entity.ParseErrorEntity
import com.hainanu.signinassistant.data.local.entity.ScheduledReminderEntity
import com.hainanu.signinassistant.data.local.entity.SectionSlotEntity

@Database(
    entities = [
        CourseEntity::class,
        SectionSlotEntity::class,
        HolidayEntity::class,
        DailyMuteEntity::class,
        ParseErrorEntity::class,
        CourseNoteEntity::class,
        ScheduledReminderEntity::class,
        NotificationLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun sectionSlotDao(): SectionSlotDao
    abstract fun holidayDao(): HolidayDao
    abstract fun dailyMuteDao(): DailyMuteDao
    abstract fun parseErrorDao(): ParseErrorDao
    abstract fun courseNoteDao(): CourseNoteDao
    abstract fun scheduledReminderDao(): ScheduledReminderDao
    abstract fun notificationLogDao(): NotificationLogDao
}
