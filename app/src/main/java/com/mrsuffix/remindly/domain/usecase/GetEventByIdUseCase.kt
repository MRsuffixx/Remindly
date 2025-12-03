package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for getting a single event by ID
 */
class GetEventByIdUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: Long): Event? {
        return eventRepository.getEventById(eventId)
    }
}
