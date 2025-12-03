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
 * fixedMonth and fixedDay are for holidays with fixed dates (null means user must enter date)
 * isReligious indicates holidays that change every year (should use ONE_TIME repeat)
 */
enum class EventCategory(
    val displayName: String, 
    val emoji: String,
    val fixedMonth: Int? = null,
    val fixedDay: Int? = null,
    val isReligious: Boolean = false
) {
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
    
    // Family types - Fixed dates (approximate, second Sunday of May / third Sunday of June)
    MOTHERS_DAY("Anneler Günü", "👩", 5, 12),
    FATHERS_DAY("Babalar Günü", "👨", 6, 16),
    
    // Turkish Holidays - Religious (dates change every year based on Islamic calendar)
    EID_AL_FITR("Ramazan Bayramı", "🌙", isReligious = true),
    EID_AL_ADHA("Kurban Bayramı", "🐑", isReligious = true),
    
    // Turkish Holidays - National (Fixed dates)
    NEW_YEARS_EVE("Yılbaşı", "🎆", 1, 1),
    VALENTINES_DAY("Sevgililer Günü", "❤️", 2, 14),
    TEACHERS_DAY("Öğretmenler Günü", "📚", 11, 24),
    APRIL_23("23 Nisan", "🇹🇷", 4, 23),
    MAY_19("19 Mayıs", "🇹🇷", 5, 19),
    AUGUST_30("30 Ağustos", "🇹🇷", 8, 30),
    OCTOBER_29("29 Ekim", "🇹🇷", 10, 29),
    
    // Custom
    CUSTOM("Özel Gün", "⭐");
    
    /**
     * Check if this category has a fixed date
     */
    fun hasFixedDate(): Boolean = fixedMonth != null && fixedDay != null
    
    /**
     * Get the fixed date for the current year (or null if no fixed date)
     */
    fun getFixedDate(year: Int = java.time.LocalDate.now().year): java.time.LocalDate? {
        return if (fixedMonth != null && fixedDay != null) {
            java.time.LocalDate.of(year, fixedMonth, fixedDay)
        } else null
    }
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
