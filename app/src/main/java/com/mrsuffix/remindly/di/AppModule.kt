package com.mrsuffix.remindly.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mrsuffix.remindly.data.local.dao.EventDao
import com.mrsuffix.remindly.data.local.database.RemindlyDatabase
import com.mrsuffix.remindly.data.repository.EventRepositoryImpl
import com.mrsuffix.remindly.data.repository.SettingsRepositoryImpl
import com.mrsuffix.remindly.domain.repository.EventRepository
import com.mrsuffix.remindly.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "remindly_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideRemindlyDatabase(
        @ApplicationContext context: Context
    ): RemindlyDatabase {
        return Room.databaseBuilder(
            context,
            RemindlyDatabase::class.java,
            RemindlyDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideEventDao(database: RemindlyDatabase): EventDao {
        return database.eventDao()
    }
    
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
    
    @Provides
    @Singleton
    fun provideEventRepository(
        eventDao: EventDao,
        gson: Gson
    ): EventRepository {
        return EventRepositoryImpl(eventDao, gson)
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataStore)
    }
}
