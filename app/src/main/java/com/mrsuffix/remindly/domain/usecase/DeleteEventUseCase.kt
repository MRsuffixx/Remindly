package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for deleting an event
 */
class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: Long) {
        eventRepository.deleteEventById(eventId)
    }
}
