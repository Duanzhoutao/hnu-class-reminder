package com.hainanu.signinassistant.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.hainanu.signinassistant.data.local.dao.CourseDao
import com.hainanu.signinassistant.data.local.dao.CourseNoteDao
import com.hainanu.signinassistant.data.local.dao.DailyMuteDao
import com.hainanu.signinassistant.data.local.dao.HolidayDao
import com.hainanu.signinassistant.data.local.dao.NotificationLogDao
import com.hainanu.signinassistant.data.local.dao.ParseErrorDao
import com.hainanu.signinassistant.data.local.dao.ScheduledReminderDao
import com.hainanu.signinassistant.data.local.dao.SectionSlotDao
import com.hainanu.signinassistant.data.local.db.AppDatabase
import com.hainanu.signinassistant.data.parser.PoiTimetableParser
import com.hainanu.signinassistant.data.parser.TimetableParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sign_in_assistant.db",
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    @Provides
    fun provideSectionSlotDao(db: AppDatabase): SectionSlotDao = db.sectionSlotDao()

    @Provides
    fun provideHolidayDao(db: AppDatabase): HolidayDao = db.holidayDao()

    @Provides
    fun provideDailyMuteDao(db: AppDatabase): DailyMuteDao = db.dailyMuteDao()

    @Provides
    fun provideParseErrorDao(db: AppDatabase): ParseErrorDao = db.parseErrorDao()

    @Provides
    fun provideCourseNoteDao(db: AppDatabase): CourseNoteDao = db.courseNoteDao()

    @Provides
    fun provideScheduledReminderDao(db: AppDatabase): ScheduledReminderDao = db.scheduledReminderDao()

    @Provides
    fun provideNotificationLogDao(db: AppDatabase): NotificationLogDao = db.notificationLogDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") },
        )

    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideTimetableParser(parser: PoiTimetableParser): TimetableParser = parser
}
