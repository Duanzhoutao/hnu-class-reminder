package com.hainanu.signinassistant.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hainanu.signinassistant.R
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.ReminderTier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
) {

    fun createChannels(settings: AppSettings) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        ReminderTier.values().forEach { tier ->
            val channel = NotificationChannel(
                channelId(tier, settings),
                channelName(tier),
                channelImportance(tier),
            ).apply {
                description = "海大课程签到提醒"
                enableVibration(settings.vibrationEnabled && tier != ReminderTier.NORMAL)
                if (settings.soundEnabled && tier != ReminderTier.NORMAL) {
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build(),
                    )
                } else {
                    setSound(null, null)
                }
                if (tier == ReminderTier.STRONG) {
                    vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminder(
        requestCode: Int,
        title: String,
        body: String,
        tier: ReminderTier,
        settings: AppSettings,
    ): Boolean {
        createChannels(settings)
        if (!canPostNotifications()) return false

        val notification = NotificationCompat.Builder(context, channelId(tier, settings))
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                when (tier) {
                    ReminderTier.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
                    ReminderTier.STANDARD -> NotificationCompat.PRIORITY_HIGH
                    ReminderTier.STRONG -> NotificationCompat.PRIORITY_MAX
                },
            )
            .setCategory(
                if (tier == ReminderTier.STRONG) NotificationCompat.CATEGORY_ALARM
                else NotificationCompat.CATEGORY_REMINDER,
            )
            .setOngoing(tier == ReminderTier.STRONG)
            .setAutoCancel(tier != ReminderTier.STRONG)
            .setTimeoutAfter(if (tier == ReminderTier.STRONG) 120_000L else 30_000L)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
        return true
    }

    fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun channelName(tier: ReminderTier): String =
        when (tier) {
            ReminderTier.NORMAL -> "普通提醒"
            ReminderTier.STANDARD -> "标准提醒"
            ReminderTier.STRONG -> "强提醒"
        }

    private fun channelImportance(tier: ReminderTier): Int =
        when (tier) {
            ReminderTier.NORMAL -> NotificationManager.IMPORTANCE_DEFAULT
            ReminderTier.STANDARD -> NotificationManager.IMPORTANCE_HIGH
            ReminderTier.STRONG -> NotificationManager.IMPORTANCE_HIGH
        }

    private fun channelId(tier: ReminderTier, settings: AppSettings): String {
        val vibration = if (settings.vibrationEnabled) "v1" else "v0"
        val sound = if (settings.soundEnabled) "s1" else "s0"
        return "course_reminder_${tier.name.lowercase()}_${vibration}_${sound}"
    }
}
