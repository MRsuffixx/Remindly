package com.mrsuffix.remindly.domain.model

import java.time.LocalDate

/**
 * Domain model representing a reminder event (birthday, anniversary, holiday, etc.)
 */
data class Event(
    val id: Long = 0,
    val name: String,
    val date: LocalDate,
    val eventType: EventType,
    val eventCategory: EventCategory,
    val repeatType: RepeatType = RepeatType.YEARLY,
    val reminderDays: List<Int> = listOf(1), // Days before event to remind
    val note: String = "",
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now()
) {
    /**
     * Calculate days until next occurrence of this event
     */
    fun daysUntilNext(): Int {
        val today = LocalDate.now()
        val thisYearDate = date.withYear(today.year)
        
        return if (thisYearDate.isBefore(today) || thisYearDate.isEqual(today)) {
            if (repeatType == RepeatType.ONE_TIME && thisYearDate.isBefore(today)) {
                -1 // Event has passed and won't repeat
            } else {
                val nextYearDate = thisYearDate.plusYears(1)
                java.time.temporal.ChronoUnit.DAYS.between(today, nextYearDate).toInt()
            }
        } else {
            java.time.temporal.ChronoUnit.DAYS.between(today, thisYearDate).toInt()
        }
    }
    
    /**
     * Get the next occurrence date
     */
    fun nextOccurrence(): LocalDate {
        val today = LocalDate.now()
        val thisYearDate = date.withYear(today.year)
        
        return if (thisYearDate.isBefore(today) || thisYearDate.isEqual(today)) {
            if (repeatType == RepeatType.ONE_TIME) {
                thisYearDate // Return original date for one-time events
            } else {
                thisYearDate.plusYears(1)
            }
        } else {
            thisYearDate
        }
    }
    
    /**
     * Calculate age/years since the original date
     */
    fun yearsSince(): Int {
        val today = LocalDate.now()
        return today.year - date.year
    }
}

/**
 * Main event types
 */
enum class EventType {
    BIRTHDAY,
    ANNIVERSARY,
    FAMILY,
    HOLIDAY,
    CUSTOM
}

/**
 * Detailed event categories
 */
enum class EventCategory(val displayName: String, val emoji: String) {
    // Birthday types
    BIRTHDAY("Doğum Günü", "🎂"),
    CHILDREN_BIRTHDAY("Çocuk Doğum Günü", "👶"),
    SIBLING_BIRTHDAY("Kardeş Doğum Günü", "👫"),
    RELATIVE_BIRTHDAY("Akraba Doğum Günü", "👨‍👩‍👧‍👦"),
    PET_BIRTHDAY("Evcil Hayvan Doğum Günü", "🐾"),
    
    // Anniversary types
    WEDDING_ANNIVERSARY("Evlilik Yıldönümü", "💒"),
    RELATIONSHIP_ANNIVERSARY("İlişki Yıldönümü", "💑"),
    DATING_ANNIVERSARY("Tanışma Günü", "💕"),
    ENGAGEMENT_ANNIVERSARY("Nişan Yıldönümü", "💍"),
    PROMISE_ANNIVERSARY("Söz Yıldönümü", "🤝"),
    GRADUATION_DAY("Mezuniyet Günü", "🎓"),
    WORK_ANNIVERSARY("İş Yıldönümü", "💼"),
    FIRST_DAY_OF_WORK("İlk İş Günü", "🏢"),
    HOUSE_ANNIVERSARY("Ev Yıldönümü", "🏠"),
    FAMILY_ANNIVERSARY("Aile Yıldönümü", "👨‍👩‍👧"),
    
    // Family types
    MOTHERS_DAY("Anneler Günü", "👩"),
    FATHERS_DAY("Babalar Günü", "👨"),
    
    // Turkish Holidays - Religious
    EID_AL_FITR("Ramazan Bayramı", "🌙"),
    EID_AL_ADHA("Kurban Bayramı", "🐑"),
    
    // Turkish Holidays - National
    NEW_YEARS_EVE("Yılbaşı", "🎆"),
    VALENTINES_DAY("Sevgililer Günü", "❤️"),
    TEACHERS_DAY("Öğretmenler Günü", "📚"),
    APRIL_23("23 Nisan", "🇹🇷"),
    MAY_19("19 Mayıs", "🇹🇷"),
    AUGUST_30("30 Ağustos", "🇹🇷"),
    OCTOBER_29("29 Ekim", "🇹🇷"),
    
    // Custom
    CUSTOM("Özel Gün", "⭐")
}

/**
 * Repeat types for events
 */
enum class RepeatType(val displayName: String) {
    ONE_TIME("Tek Seferlik"),
    YEARLY("Her Yıl")
}

/**
 * Reminder period options
 */
enum class ReminderPeriod(val days: Int, val displayName: String) {
    SAME_DAY(0, "Aynı Gün"),
    ONE_DAY(1, "1 Gün Önce"),
    THREE_DAYS(3, "3 Gün Önce"),
    SEVEN_DAYS(7, "7 Gün Önce"),
    FOURTEEN_DAYS(14, "14 Gün Önce"),
    THIRTY_DAYS(30, "30 Gün Önce")
}
