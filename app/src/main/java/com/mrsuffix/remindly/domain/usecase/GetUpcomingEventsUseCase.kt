package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting upcoming events sorted by days until next occurrence
 */
class GetUpcomingEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<Event>> {
        return eventRepository.getAllEvents().map { events ->
            events
                .filter { it.isActive && it.daysUntilNext() >= 0 }
                .sortedBy { it.daysUntilNext() }
                .take(limit)
        }
    }
}
