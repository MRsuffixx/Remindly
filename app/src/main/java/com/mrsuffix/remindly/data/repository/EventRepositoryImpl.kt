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
        return eventDao.searchEvents(query).map { entities ->
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
        return gson.toJson(events)
    }
    
    override suspend fun importEvents(json: String): Int {
        return try {
            val type = object : TypeToken<List<EventExportModel>>() {}.type
            val exportedEvents: List<EventExportModel> = gson.fromJson(json, type)
            val events = exportedEvents.map { it.toEvent() }
            eventDao.insertEvents(events.map { EventEntity.fromDomain(it) })
            events.size
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
}
