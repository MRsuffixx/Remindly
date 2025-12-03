package com.mrsuffix.remindly.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrsuffix.remindly.data.local.dao.EventDao
import com.mrsuffix.remindly.data.local.entity.EventEntity

/**
 * Room database for Remindly app
 */
@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RemindlyDatabase : RoomDatabase() {
    
    abstract fun eventDao(): EventDao
    
    companion object {
        const val DATABASE_NAME = "remindly_database"
    }
}
