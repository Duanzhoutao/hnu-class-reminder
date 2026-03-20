package com.hainanu.signinassistant.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hainanu.signinassistant.data.repository.DailyMuteRepository
import com.hainanu.signinassistant.data.repository.HolidayRepository
import com.hainanu.signinassistant.data.repository.ReminderDebugRepository
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.domain.model.NotificationLog
import com.hainanu.signinassistant.domain.model.ReminderType
import com.hainanu.signinassistant.domain.model.ScheduledReminder
import com.hainanu.signinassistant.domain.usecase.CourseActiveUseCase
import com.hainanu.signinassistant.domain.usecase.ReminderMomentBuilder
import com.hainanu.signinassistant.receiver.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
    private val timetableRepository: TimetableRepository,
    private val holidayRepository: HolidayRepository,
    private val dailyMuteRepository: DailyMuteRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderMomentBuilder: ReminderMomentBuilder,
    private val courseActiveUseCase: CourseActiveUseCase,
    private val reminderDebugRepository: ReminderDebugRepository,
) {

    suspend fun rescheduleHorizon(reason: String, horizonDays: Int = 14) {
        val courses = timetableRepository.getCourses()
        cancelAllScheduled()
        if (courses.isEmpty()) return

        val sectionSlots = timetableRepository.getSectionSlots()
        val settings = settingsRepository.settingsFlow.first()
        val termId = courses.first().termId
        val holidays = holidayRepository.getForTerm(termId)
        val reminders = mutableListOf<ScheduledReminder>()
        val today = LocalDate.now()

        for (offset in 0 until horizonDays) {
            val date = today.plusDays(offset.toLong())
            val dailyMuted = dailyMuteRepository.isMuted(date)
            courses.forEach { course ->
                if (
                    courseActiveUseCase.isActive(
                        course = course,
                        date = date,
                        settings = settings,
                        holidays = holidays,
                        dailyMuted = dailyMuted,
                    )
                ) {
                    reminderMomentBuilder.build(course, date, settings, sectionSlots)
                        .forEach { moment ->
                            val requestCode = buildRequestCode(course.id, date, moment.type)
                            reminders += ScheduledReminder(
                                requestCode = requestCode,
                                courseId = course.id,
                                courseName = course.courseName,
                                reminderType = moment.type,
                                reminderTier = moment.tier,
                                triggerAt = moment.triggerAt,
                                date = date,
                                title = moment.title,
                                body = moment.body,
                            )
                        }
                }
            }
        }

        reminders
            .sortedBy { it.triggerAt }
            .forEach { scheduleSingle(it) }
        reminderDebugRepository.replaceScheduled(reminders)
        android.util.Log.d("ReminderScheduler", "Rescheduled ${reminders.size} reminders, reason=$reason")
        ensureMaintenanceWork()
    }

    suspend fun cancelAllScheduled() {
        reminderDebugRepository.getScheduled().forEach { scheduled ->
            val intent = buildReminderIntent(scheduled)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduled.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        reminderDebugRepository.clearScheduled()
    }

    suspend fun logDeliveredNotification(
        requestCode: Int,
        courseName: String,
        reminderType: ReminderType,
        reminderTier: com.hainanu.signinassistant.domain.model.ReminderTier,
    ) {
        reminderDebugRepository.logNotification(
            NotificationLog(
                id = 0,
                requestCode = requestCode,
                courseName = courseName,
                reminderType = reminderType,
                reminderTier = reminderTier,
                triggeredAt = LocalDateTime.now(),
            ),
        )
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun ensureMaintenanceWork() {
        val request = PeriodicWorkRequestBuilder<ReminderMaintenanceWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MAINTENANCE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun scheduleSingle(reminder: ScheduledReminder) {
        val triggerAtMillis = reminder.triggerAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            buildReminderIntent(reminder),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun buildReminderIntent(reminder: ScheduledReminder): Intent =
        Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REQUEST_CODE, reminder.requestCode)
            putExtra(ReminderReceiver.EXTRA_COURSE_NAME, reminder.courseName)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderReceiver.EXTRA_BODY, reminder.body)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TYPE, reminder.reminderType.name)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TIER, reminder.reminderTier.name)
        }

    private fun buildRequestCode(courseId: Long, date: LocalDate, type: ReminderType): Int {
        return "$courseId-${date}-$type".hashCode()
    }

    companion object {
        const val MAINTENANCE_WORK_NAME = "reminder_maintenance_work"
    }
}
