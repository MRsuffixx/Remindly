package com.mrsuffix.remindly.domain.repository

import com.mrsuffix.remindly.domain.model.Settings
import com.mrsuffix.remindly.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

/**
 * Repository interface for Settings operations
 */
interface SettingsRepository {
    
    /**
     * Get current settings as a Flow
     */
    fun getSettings(): Flow<Settings>
    
    /**
     * Update notification time
     */
    suspend fun setNotificationTime(time: LocalTime)
    
    /**
     * Update theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode)
    
    /**
     * Enable/disable notifications
     */
    suspend fun setNotificationsEnabled(enabled: Boolean)
    
    /**
     * Set default reminder days
     */
    suspend fun setDefaultReminderDays(days: List<Int>)
    
    /**
     * Get theme mode
     */
    fun getThemeMode(): Flow<ThemeMode>
    
    /**
     * Check if this is the first app launch
     */
    suspend fun isFirstLaunch(): Boolean
    
    /**
     * Mark first launch as complete
     */
    suspend fun setFirstLaunchComplete()
}
