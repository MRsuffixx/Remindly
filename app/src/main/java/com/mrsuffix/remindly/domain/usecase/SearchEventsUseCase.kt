package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching events by name
 */
class SearchEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(query: String): Flow<List<Event>> {
        return eventRepository.searchEvents(query)
    }
}
