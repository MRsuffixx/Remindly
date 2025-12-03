package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting events happening this month
 */
class GetEventsThisMonthUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): Flow<List<Event>> {
        return eventRepository.getAllEvents().map { events ->
            events
                .filter { it.isActive && it.daysUntilNext() in 0..30 }
                .sortedBy { it.daysUntilNext() }
        }
    }
}
