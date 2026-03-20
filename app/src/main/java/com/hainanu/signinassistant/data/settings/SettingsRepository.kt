package com.hainanu.signinassistant.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hainanu.signinassistant.data.term.AcademicTermDefaults
import com.hainanu.signinassistant.domain.model.AppSettings
import com.hainanu.signinassistant.domain.model.ReminderTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            firstWeekMonday = preferences[FIRST_WEEK_MONDAY]
                ?.takeIf { it.isNotBlank() }
                ?.let(LocalDate::parse),
            preClassReminderEnabled = preferences[PRE_CLASS_ENABLED] ?: true,
            onClassReminderEnabled = preferences[ON_CLASS_ENABLED] ?: true,
            postClassReminderEnabled = preferences[POST_CLASS_ENABLED] ?: true,
            preClassMinutes = preferences[PRE_CLASS_MINUTES] ?: 20,
            postClassMinutes = preferences[POST_CLASS_MINUTES] ?: 5,
            reminderTier = preferences[REMINDER_TIER]?.let(ReminderTier::valueOf) ?: ReminderTier.STANDARD,
            vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
            soundEnabled = preferences[SOUND_ENABLED] ?: false,
            soundUri = preferences[SOUND_URI],
            exactAlarmHintDismissed = preferences[EXACT_ALARM_HINT_DISMISSED] ?: false,
            notificationPermissionPrompted = preferences[NOTIFICATION_PERMISSION_PROMPTED] ?: false,
        )
    }

    suspend fun updateFirstWeekMonday(date: LocalDate?) {
        dataStore.edit {
            if (date == null) {
                it.remove(FIRST_WEEK_MONDAY)
            } else {
                it[FIRST_WEEK_MONDAY] = date.toString()
            }
        }
    }

    suspend fun alignFirstWeekMondayForTerm(termId: String): LocalDate? {
        val knownDate = AcademicTermDefaults.firstWeekMondayFor(termId) ?: return null
        dataStore.edit { it[FIRST_WEEK_MONDAY] = knownDate.toString() }
        return knownDate
    }

    suspend fun updateReminderTier(tier: ReminderTier) {
        dataStore.edit { it[REMINDER_TIER] = tier.name }
    }

    suspend fun updateReminderSwitches(pre: Boolean? = null, on: Boolean? = null, post: Boolean? = null) {
        dataStore.edit {
            pre?.let { value -> it[PRE_CLASS_ENABLED] = value }
            on?.let { value -> it[ON_CLASS_ENABLED] = value }
            post?.let { value -> it[POST_CLASS_ENABLED] = value }
        }
    }

    suspend fun updateOffsets(preMinutes: Int? = null, postMinutes: Int? = null) {
        dataStore.edit {
            preMinutes?.let { value -> it[PRE_CLASS_MINUTES] = value }
            postMinutes?.let { value -> it[POST_CLASS_MINUTES] = value }
        }
    }

    suspend fun updateSoundOptions(vibrationEnabled: Boolean? = null, soundEnabled: Boolean? = null) {
        dataStore.edit {
            vibrationEnabled?.let { value -> it[VIBRATION_ENABLED] = value }
            soundEnabled?.let { value -> it[SOUND_ENABLED] = value }
        }
    }

    suspend fun dismissExactAlarmHint() {
        dataStore.edit { it[EXACT_ALARM_HINT_DISMISSED] = true }
    }

    suspend fun markNotificationPermissionPrompted() {
        dataStore.edit { it[NOTIFICATION_PERMISSION_PROMPTED] = true }
    }

    private companion object {
        val FIRST_WEEK_MONDAY = stringPreferencesKey("first_week_monday")
        val PRE_CLASS_ENABLED = booleanPreferencesKey("pre_class_enabled")
        val ON_CLASS_ENABLED = booleanPreferencesKey("on_class_enabled")
        val POST_CLASS_ENABLED = booleanPreferencesKey("post_class_enabled")
        val PRE_CLASS_MINUTES = intPreferencesKey("pre_class_minutes")
        val POST_CLASS_MINUTES = intPreferencesKey("post_class_minutes")
        val REMINDER_TIER = stringPreferencesKey("reminder_tier")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val SOUND_URI = stringPreferencesKey("sound_uri")
        val EXACT_ALARM_HINT_DISMISSED = booleanPreferencesKey("exact_alarm_hint_dismissed")
        val NOTIFICATION_PERMISSION_PROMPTED = booleanPreferencesKey("notification_permission_prompted")
    }
}
