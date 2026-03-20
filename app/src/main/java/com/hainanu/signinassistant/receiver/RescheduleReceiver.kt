package com.hainanu.signinassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hainanu.signinassistant.alarm.ReminderMaintenanceWorker

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<ReminderMaintenanceWorker>().build(),
        )
    }
}
