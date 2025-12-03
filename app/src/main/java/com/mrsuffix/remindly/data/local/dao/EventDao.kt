package com.mrsuffix.remindly.data.local.dao

import androidx.room.*
import com.mrsuffix.remindly.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Event operations
 */
@Dao
interface EventDao {
    
    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventType = :eventType ORDER BY dateEpochDay ASC")
    fun getEventsByType(eventType: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventCategory = :category ORDER BY dateEpochDay ASC")
    fun getEventsByCategory(category: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    fun getEventsInDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY dateEpochDay ASC LIMIT :limit")
    fun getUpcomingEvents(limit: Int): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?
    
    @Query("SELECT * FROM events WHERE name LIKE '%' || :query || '%' ORDER BY dateEpochDay ASC")
    fun searchEvents(query: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE eventType = 'HOLIDAY' ORDER BY dateEpochDay ASC")
    fun getHolidayEvents(): Flow<List<EventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
    
    @Query("SELECT * FROM events")
    suspend fun getAllEventsSync(): List<EventEntity>
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
