package com.hainanu.signinassistant.domain.usecase

import com.hainanu.signinassistant.alarm.ReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RescheduleAllRemindersUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(reason: String) {
        reminderScheduler.rescheduleHorizon(reason = reason)
    }
}
