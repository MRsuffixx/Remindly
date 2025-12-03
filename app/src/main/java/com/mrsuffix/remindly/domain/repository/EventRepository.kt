package com.mrsuffix.remindly.domain.repository

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventCategory
import com.mrsuffix.remindly.domain.model.EventType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Event operations
 * Following Clean Architecture, this is defined in the domain layer
 * and implemented in the data layer
 */
interface EventRepository {
    
    /**
     * Get all events as a Flow for reactive updates
     */
    fun getAllEvents(): Flow<List<Event>>
    
    /**
     * Get events by type
     */
    fun getEventsByType(eventType: EventType): Flow<List<Event>>
    
    /**
     * Get events by category
     */
    fun getEventsByCategory(category: EventCategory): Flow<List<Event>>
    
    /**
     * Get events within a date range
     */
    fun getEventsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Event>>
    
    /**
     * Get upcoming events (sorted by next occurrence)
     */
    fun getUpcomingEvents(limit: Int = 10): Flow<List<Event>>
    
    /**
     * Get events happening this week
     */
    fun getEventsThisWeek(): Flow<List<Event>>
    
    /**
     * Get events happening this month
     */
    fun getEventsThisMonth(): Flow<List<Event>>
    
    /**
     * Get a single event by ID
     */
    suspend fun getEventById(id: Long): Event?
    
    /**
     * Insert a new event
     */
    suspend fun insertEvent(event: Event): Long
    
    /**
     * Update an existing event
     */
    suspend fun updateEvent(event: Event)
    
    /**
     * Delete an event
     */
    suspend fun deleteEvent(event: Event)
    
    /**
     * Delete an event by ID
     */
    suspend fun deleteEventById(id: Long)
    
    /**
     * Search events by name
     */
    fun searchEvents(query: String): Flow<List<Event>>
    
    /**
     * Get events that need reminders today
     */
    fun getEventsNeedingReminder(reminderDays: List<Int>): Flow<List<Event>>
    
    /**
     * Get all holiday events
     */
    fun getHolidayEvents(): Flow<List<Event>>
    
    /**
     * Insert multiple events (for bulk operations like adding holidays)
     */
    suspend fun insertEvents(events: List<Event>)
    
    /**
     * Export all events as JSON string
     */
    suspend fun exportEvents(): String
    
    /**
     * Import events from JSON string
     */
    suspend fun importEvents(json: String): Int
    
    /**
     * Delete all events
     */
    suspend fun deleteAllEvents()
    
    /**
     * Delete all holiday events (for restore defaults)
     */
    suspend fun deleteHolidayEvents()
}
