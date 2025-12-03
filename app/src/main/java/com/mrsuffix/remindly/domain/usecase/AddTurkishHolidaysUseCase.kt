package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventCategory
import com.mrsuffix.remindly.domain.model.EventType
import com.mrsuffix.remindly.domain.model.RepeatType
import com.mrsuffix.remindly.domain.repository.EventRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for adding Turkish holidays to the database
 */
class AddTurkishHolidaysUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(year: Int = LocalDate.now().year) {
        val holidays = listOf(
            // New Year's Eve
            Event(
                name = "Yılbaşı",
                date = LocalDate.of(year, 1, 1),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.NEW_YEARS_EVE,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // Valentine's Day
            Event(
                name = "Sevgililer Günü",
                date = LocalDate.of(year, 2, 14),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.VALENTINES_DAY,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // April 23 - National Sovereignty and Children's Day
            Event(
                name = "23 Nisan Ulusal Egemenlik ve Çocuk Bayramı",
                date = LocalDate.of(year, 4, 23),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.APRIL_23,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // May 1 - Labor Day
            Event(
                name = "1 Mayıs İşçi Bayramı",
                date = LocalDate.of(year, 5, 1),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.CUSTOM,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1)
            ),
            // Mother's Day (Second Sunday of May - approximated to May 12)
            Event(
                name = "Anneler Günü",
                date = LocalDate.of(year, 5, 12),
                eventType = EventType.FAMILY,
                eventCategory = EventCategory.MOTHERS_DAY,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // May 19 - Commemoration of Atatürk, Youth and Sports Day
            Event(
                name = "19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı",
                date = LocalDate.of(year, 5, 19),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.MAY_19,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // Father's Day (Third Sunday of June - approximated to June 16)
            Event(
                name = "Babalar Günü",
                date = LocalDate.of(year, 6, 16),
                eventType = EventType.FAMILY,
                eventCategory = EventCategory.FATHERS_DAY,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // August 30 - Victory Day
            Event(
                name = "30 Ağustos Zafer Bayramı",
                date = LocalDate.of(year, 8, 30),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.AUGUST_30,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // October 29 - Republic Day
            Event(
                name = "29 Ekim Cumhuriyet Bayramı",
                date = LocalDate.of(year, 10, 29),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.OCTOBER_29,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            ),
            // November 24 - Teachers' Day
            Event(
                name = "Öğretmenler Günü",
                date = LocalDate.of(year, 11, 24),
                eventType = EventType.HOLIDAY,
                eventCategory = EventCategory.TEACHERS_DAY,
                repeatType = RepeatType.YEARLY,
                reminderDays = listOf(1, 7)
            )
        )
        
        eventRepository.insertEvents(holidays)
    }
}
