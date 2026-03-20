package com.hainanu.signinassistant.data.repository

import com.hainanu.signinassistant.data.local.dao.NotificationLogDao
import com.hainanu.signinassistant.data.local.dao.ScheduledReminderDao
import com.hainanu.signinassistant.domain.model.NotificationLog
import com.hainanu.signinassistant.domain.model.ScheduledReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderDebugRepository @Inject constructor(
    private val scheduledReminderDao: ScheduledReminderDao,
    private val notificationLogDao: NotificationLogDao,
) {

    val scheduledFlow: Flow<List<ScheduledReminder>> =
        scheduledReminderDao.observeAll().map { items -> items.map { it.toDomain() } }

    val notificationLogsFlow: Flow<List<NotificationLog>> =
        notificationLogDao.observeLatest().map { items -> items.map { it.toDomain() } }

    suspend fun replaceScheduled(items: List<ScheduledReminder>) {
        scheduledReminderDao.clear()
        scheduledReminderDao.insertAll(items.map { it.toEntity() })
    }

    suspend fun getScheduled(): List<ScheduledReminder> =
        scheduledReminderDao.getAll().map { it.toDomain() }

    suspend fun clearScheduled() {
        scheduledReminderDao.clear()
    }

    suspend fun logNotification(item: NotificationLog) {
        notificationLogDao.insert(item.toEntity())
        notificationLogDao.deleteBefore(LocalDateTime.now().minusDays(14).toString())
    }
}
