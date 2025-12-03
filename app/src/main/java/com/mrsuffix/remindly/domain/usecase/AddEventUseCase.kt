package com.mrsuffix.remindly.domain.usecase

import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for adding a new event
 */
class AddEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event): Long {
        return eventRepository.insertEvent(event)
    }
}
