package com.mrsuffix.remindly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventCategory
import com.mrsuffix.remindly.domain.model.EventType
import com.mrsuffix.remindly.domain.model.RepeatType
import java.time.LocalDate

/**
 * Room entity for Event
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateEpochDay: Long, // Store LocalDate as epoch day
    val eventType: String,
    val eventCategory: String,
    val repeatType: String,
    val reminderDays: String, // Store as comma-separated string
    val note: String,
    val isActive: Boolean,
    val createdAtEpochDay: Long
) {
    /**
     * Convert entity to domain model
     */
    fun toDomain(): Event {
        return Event(
            id = id,
            name = name,
            date = LocalDate.ofEpochDay(dateEpochDay),
            eventType = EventType.valueOf(eventType),
            eventCategory = EventCategory.valueOf(eventCategory),
            repeatType = RepeatType.valueOf(repeatType),
            reminderDays = reminderDays.split(",").filter { it.isNotBlank() }.map { it.toInt() },
            note = note,
            isActive = isActive,
            createdAt = LocalDate.ofEpochDay(createdAtEpochDay)
        )
    }
    
    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromDomain(event: Event): EventEntity {
            return EventEntity(
                id = event.id,
                name = event.name,
                dateEpochDay = event.date.toEpochDay(),
                eventType = event.eventType.name,
                eventCategory = event.eventCategory.name,
                repeatType = event.repeatType.name,
                reminderDays = event.reminderDays.joinToString(","),
                note = event.note,
                isActive = event.isActive,
                createdAtEpochDay = event.createdAt.toEpochDay()
            )
        }
    }
}
