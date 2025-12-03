package com.mrsuffix.remindly.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrsuffix.remindly.data.local.dao.EventDao
import com.mrsuffix.remindly.data.local.entity.EventEntity
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventCategory
import com.mrsuffix.remindly.domain.model.EventType
import com.mrsuffix.remindly.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EventRepository using Room database
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val gson: Gson
) : EventRepository {
    
    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsByType(eventType: EventType): Flow<List<Event>> {
        return eventDao.getEventsByType(eventType.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsByCategory(category: EventCategory): Flow<List<Event>> {
        return eventDao.getEventsByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Event>> {
        return eventDao.getEventsInDateRange(
            startDate.toEpochDay(),
            endDate.toEpochDay()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getUpcomingEvents(limit: Int): Flow<List<Event>> {
        return eventDao.getUpcomingEvents(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsThisWeek(): Flow<List<Event>> {
        val today = LocalDate.now()
        val endOfWeek = today.plusDays(7)
        return getEventsInDateRange(today, endOfWeek)
    }
    
    override fun getEventsThisMonth(): Flow<List<Event>> {
        val today = LocalDate.now()
        val endOfMonth = today.plusDays(30)
        return getEventsInDateRange(today, endOfMonth)
    }
    
    override suspend fun getEventById(id: Long): Event? {
        return eventDao.getEventById(id)?.toDomain()
    }
    
    override suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(EventEntity.fromDomain(event))
    }
    
    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(EventEntity.fromDomain(event))
    }
    
    override suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(EventEntity.fromDomain(event))
    }
    
    override suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }
    
    override fun searchEvents(query: String): Flow<List<Event>> {
        // Security: Sanitize search query - limit length and remove special characters
        val sanitizedQuery = query
            .take(100) // Limit query length
            .replace(Regex("[%_\\[\\]^]"), "") // Remove SQL LIKE wildcards
            .trim()
        
        if (sanitizedQuery.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        
        return eventDao.searchEvents(sanitizedQuery).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getEventsNeedingReminder(reminderDays: List<Int>): Flow<List<Event>> {
        return getAllEvents().map { events ->
            events.filter { event ->
                val daysUntil = event.daysUntilNext()
                event.isActive && event.reminderDays.any { it == daysUntil }
            }
        }
    }
    
    override fun getHolidayEvents(): Flow<List<Event>> {
        return eventDao.getHolidayEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertEvents(events: List<Event>) {
        eventDao.insertEvents(events.map { EventEntity.fromDomain(it) })
    }
    
    override suspend fun exportEvents(): String {
        val events = eventDao.getAllEventsSync().map { it.toDomain() }
        val exportModels = events.map { event ->
            EventExportModel(
                name = event.name,
                dateEpochDay = event.date.toEpochDay(),
                eventType = event.eventType.name,
                eventCategory = event.eventCategory.name,
                repeatType = event.repeatType.name,
                reminderDays = event.reminderDays,
                note = event.note,
                isActive = event.isActive
            )
        }
        return gson.toJson(exportModels)
    }
    
    override suspend fun importEvents(json: String): Int {
        return try {
            // Security: Limit JSON size to prevent memory exhaustion (5MB max)
            if (json.length > 5_000_000) return 0
            
            // Security: Basic JSON validation
            val trimmedJson = json.trim()
            if (!trimmedJson.startsWith("[") || !trimmedJson.endsWith("]")) return 0
            
            val type = object : TypeToken<List<EventExportModel>>() {}.type
            val exportedEvents: List<EventExportModel> = gson.fromJson(json, type)
            
            // Security: Limit number of events to import (max 10000)
            if (exportedEvents.size > 10000) return 0
            
            // Security: Validate and sanitize each event
            val validEvents = exportedEvents.mapNotNull { model ->
                try {
                    model.toEventSafe()
                } catch (e: Exception) {
                    null // Skip invalid events
                }
            }
            
            if (validEvents.isEmpty()) return 0
            
            eventDao.insertEvents(validEvents.map { EventEntity.fromDomain(it) })
            validEvents.size
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }
    
    override suspend fun deleteHolidayEvents() {
        eventDao.deleteHolidayEvents()
    }
}

/**
 * Model for JSON export/import
 */
data class EventExportModel(
    val name: String,
    val dateEpochDay: Long,
    val eventType: String,
    val eventCategory: String,
    val repeatType: String,
    val reminderDays: List<Int>,
    val note: String,
    val isActive: Boolean
) {
    companion object {
        // Security: Maximum allowed string lengths
        private const val MAX_NAME_LENGTH = 200
        private const val MAX_NOTE_LENGTH = 2000
        // Security: Valid date range (year 1900 to 2200)
        private val MIN_EPOCH_DAY = LocalDate.of(1900, 1, 1).toEpochDay()
        private val MAX_EPOCH_DAY = LocalDate.of(2200, 12, 31).toEpochDay()
    }
    
    fun toEvent(): Event {
        return Event(
            name = name,
            date = LocalDate.ofEpochDay(dateEpochDay),
            eventType = EventType.valueOf(eventType),
            eventCategory = EventCategory.valueOf(eventCategory),
            repeatType = com.mrsuffix.remindly.domain.model.RepeatType.valueOf(repeatType),
            reminderDays = reminderDays,
            note = note,
            isActive = isActive
        )
    }
    
    /**
     * Security: Safe conversion with input validation and sanitization
     */
    fun toEventSafe(): Event? {
        // Validate name
        if (name.isBlank() || name.length > MAX_NAME_LENGTH) return null
        val sanitizedName = name.trim()
            .replace(Regex("[<>\"'&]"), "") // Remove potential XSS characters
            .take(MAX_NAME_LENGTH)
        
        // Validate date
        if (dateEpochDay < MIN_EPOCH_DAY || dateEpochDay > MAX_EPOCH_DAY) return null
        
        // Validate enums (will throw if invalid)
        val validEventType = try {
            EventType.valueOf(eventType)
        } catch (e: Exception) {
            return null
        }
        
        val validEventCategory = try {
            EventCategory.valueOf(eventCategory)
        } catch (e: Exception) {
            return null
        }
        
        val validRepeatType = try {
            com.mrsuffix.remindly.domain.model.RepeatType.valueOf(repeatType)
        } catch (e: Exception) {
            return null
        }
        
        // Validate reminder days (must be 0-365)
        val validReminderDays = reminderDays
            .filter { it in 0..365 }
            .take(10) // Max 10 reminder days
            .ifEmpty { listOf(1) } // Default to 1 day if empty
        
        // Sanitize note
        val sanitizedNote = note
            .take(MAX_NOTE_LENGTH)
            .replace(Regex("[<>\"'&]"), "") // Remove potential XSS characters
        
        return Event(
            name = sanitizedName,
            date = LocalDate.ofEpochDay(dateEpochDay),
            eventType = validEventType,
            eventCategory = validEventCategory,
            repeatType = validRepeatType,
            reminderDays = validReminderDays,
            note = sanitizedNote,
            isActive = isActive
        )
    }
}
