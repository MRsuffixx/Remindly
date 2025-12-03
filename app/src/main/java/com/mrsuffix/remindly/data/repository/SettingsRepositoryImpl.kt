package com.mrsuffix.remindly.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.mrsuffix.remindly.domain.model.Settings
import com.mrsuffix.remindly.domain.model.ThemeMode
import com.mrsuffix.remindly.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    
    companion object {
        private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DEFAULT_REMINDER_DAYS = stringPreferencesKey("default_reminder_days")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }
    
    override fun getSettings(): Flow<Settings> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                Settings(
                    notificationTime = LocalTime.of(
                        preferences[NOTIFICATION_HOUR] ?: 9,
                        preferences[NOTIFICATION_MINUTE] ?: 0
                    ),
                    isDarkMode = when (preferences[THEME_MODE]) {
                        ThemeMode.DARK.name -> true
                        ThemeMode.LIGHT.name -> false
                        else -> null
                    },
                    isNotificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                    defaultReminderDays = preferences[DEFAULT_REMINDER_DAYS]
                        ?.split(",")
                        ?.filter { it.isNotBlank() }
                        ?.map { it.toInt() }
                        ?: listOf(1, 7)
                )
            }
    }
    
    override suspend fun setNotificationTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR] = time.hour
            preferences[NOTIFICATION_MINUTE] = time.minute
        }
    }
    
    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    override suspend fun setDefaultReminderDays(days: List<Int>) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_REMINDER_DAYS] = days.joinToString(",")
        }
    }
    
    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                when (preferences[THEME_MODE]) {
                    ThemeMode.DARK.name -> ThemeMode.DARK
                    ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                    else -> ThemeMode.SYSTEM
                }
            }
    }
    
    override suspend fun isFirstLaunch(): Boolean {
        return dataStore.data.first()[FIRST_LAUNCH] ?: true
    }
    
    override suspend fun setFirstLaunchComplete() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = false
        }
    }
}
