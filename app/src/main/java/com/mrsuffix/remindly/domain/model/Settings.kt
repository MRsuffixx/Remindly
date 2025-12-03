package com.mrsuffix.remindly.domain.model

import java.time.LocalTime

/**
 * Domain model for app settings
 */
data class Settings(
    val notificationTime: LocalTime = LocalTime.of(9, 0), // Default 9:00 AM
    val isDarkMode: Boolean? = null, // null = follow system
    val isNotificationsEnabled: Boolean = true,
    val defaultReminderDays: List<Int> = listOf(1, 7), // Default reminder periods
    val language: String = "tr" // Turkish by default
)

/**
 * Theme mode options
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("Sistem Ayarını Kullan"),
    LIGHT("Açık Tema"),
    DARK("Koyu Tema")
}
