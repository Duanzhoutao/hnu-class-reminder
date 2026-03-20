package com.hainanu.signinassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.domain.model.ReminderTier
import com.hainanu.signinassistant.domain.model.ReminderType
import com.hainanu.signinassistant.notification.NotificationHelper
import com.hainanu.signinassistant.alarm.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)
                val courseName = intent.getStringExtra(EXTRA_COURSE_NAME).orEmpty()
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
                val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE)
                    ?.let(ReminderType::valueOf)
                    ?: ReminderType.ON_CLASS
                val reminderTier = intent.getStringExtra(EXTRA_REMINDER_TIER)
                    ?.let(ReminderTier::valueOf)
                    ?: ReminderTier.STANDARD
                val settings = settingsRepository.settingsFlow.first()
                val delivered = notificationHelper.showReminder(
                    requestCode = requestCode,
                    title = title,
                    body = body,
                    tier = reminderTier,
                    settings = settings,
                )
                if (delivered) {
                    reminderScheduler.logDeliveredNotification(
                        requestCode = requestCode,
                        courseName = courseName,
                        reminderType = reminderType,
                        reminderTier = reminderTier,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_COURSE_NAME = "extra_course_name"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        const val EXTRA_REMINDER_TIER = "extra_reminder_tier"
    }
}
