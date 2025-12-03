package com.mrsuffix.remindly.presentation.addevent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsuffix.remindly.domain.model.*
import com.mrsuffix.remindly.domain.usecase.AddEventUseCase
import com.mrsuffix.remindly.domain.usecase.GetEventByIdUseCase
import com.mrsuffix.remindly.domain.usecase.UpdateEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditEventUiState(
    val id: Long = 0,
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val eventType: EventType = EventType.BIRTHDAY,
    val eventCategory: EventCategory = EventCategory.BIRTHDAY,
    val repeatType: RepeatType = RepeatType.YEARLY,
    val reminderDays: List<Int> = listOf(1),
    val note: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showReminderPicker: Boolean = false
)

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val addEventUseCase: AddEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0L
    
    private val _uiState = MutableStateFlow(AddEditEventUiState())
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()
    
    init {
        if (eventId != 0L) {
            loadEvent(eventId)
        }
    }
    
    private fun loadEvent(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val event = getEventByIdUseCase(id)
            if (event != null) {
                _uiState.update { state ->
                    state.copy(
                        id = event.id,
                        name = event.name,
                        date = event.date,
                        eventType = event.eventType,
                        eventCategory = event.eventCategory,
                        repeatType = event.repeatType,
                        reminderDays = event.reminderDays,
                        note = event.note,
                        isEditing = true,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }
    
    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(date = date, showDatePicker = false) }
    }
    
    fun updateEventType(type: EventType) {
        val defaultCategory = when (type) {
            EventType.BIRTHDAY -> EventCategory.BIRTHDAY
            EventType.ANNIVERSARY -> EventCategory.WEDDING_ANNIVERSARY
            EventType.FAMILY -> EventCategory.MOTHERS_DAY
            EventType.HOLIDAY -> EventCategory.NEW_YEARS_EVE
            EventType.CUSTOM -> EventCategory.CUSTOM
        }
        _uiState.update { it.copy(eventType = type, eventCategory = defaultCategory) }
    }
    
    fun updateEventCategory(category: EventCategory) {
        _uiState.update { state ->
            var newDate = state.date
            var newRepeatType = state.repeatType
            
            // Auto-fill date for fixed holidays
            if (category.hasFixedDate()) {
                newDate = category.getFixedDate() ?: state.date
            }
            
            // Set ONE_TIME for religious holidays (dates change every year)
            if (category.isReligious) {
                newRepeatType = RepeatType.ONE_TIME
            } else if (!state.isEditing) {
                // Default to YEARLY for non-religious events
                newRepeatType = RepeatType.YEARLY
            }
            
            state.copy(
                eventCategory = category, 
                date = newDate,
                repeatType = newRepeatType,
                showCategoryPicker = false
            )
        }
    }
    
    /**
     * Check if the selected category has a fixed date (date picker should be disabled)
     */
    fun isDateEditable(): Boolean {
        return !_uiState.value.eventCategory.hasFixedDate()
    }
    
    /**
     * Check if the selected category is a religious holiday
     */
    fun isReligiousHoliday(): Boolean {
        return _uiState.value.eventCategory.isReligious
    }
    
    fun updateRepeatType(repeatType: RepeatType) {
        _uiState.update { it.copy(repeatType = repeatType) }
    }
    
    fun updateReminderDays(days: List<Int>) {
        _uiState.update { it.copy(reminderDays = days, showReminderPicker = false) }
    }
    
    fun toggleReminderDay(day: Int) {
        val currentDays = _uiState.value.reminderDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.update { it.copy(reminderDays = currentDays.sorted()) }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }
    
    fun showCategoryPicker(show: Boolean) {
        _uiState.update { it.copy(showCategoryPicker = show) }
    }
    
    fun showReminderPicker(show: Boolean) {
        _uiState.update { it.copy(showReminderPicker = show) }
    }
    
    fun saveEvent() {
        val state = _uiState.value
        
        // Security: Validate input
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "İsim boş olamaz") }
            return
        }
        
        // Security: Limit name length
        if (state.name.length > 200) {
            _uiState.update { it.copy(nameError = "İsim çok uzun (max 200 karakter)") }
            return
        }
        
        // Security: Limit note length
        if (state.note.length > 2000) {
            _uiState.update { it.copy(nameError = "Not çok uzun (max 2000 karakter)") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Security: Sanitize input - remove potential XSS characters
            val sanitizedName = state.name.trim()
                .replace(Regex("[<>\"'&]"), "")
                .take(200)
            
            val sanitizedNote = state.note.trim()
                .replace(Regex("[<>\"'&]"), "")
                .take(2000)
            
            val event = Event(
                id = state.id,
                name = sanitizedName,
                date = state.date,
                eventType = state.eventType,
                eventCategory = state.eventCategory,
                repeatType = state.repeatType,
                reminderDays = state.reminderDays
                    .filter { it in 0..365 }
                    .take(10)
                    .ifEmpty { listOf(1) },
                note = sanitizedNote,
                isActive = true
            )
            
            if (state.isEditing) {
                updateEventUseCase(event)
            } else {
                addEventUseCase(event)
            }
            
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
    
    fun getCategoriesForType(type: EventType): List<EventCategory> {
        return when (type) {
            EventType.BIRTHDAY -> listOf(
                EventCategory.BIRTHDAY,
                EventCategory.CHILDREN_BIRTHDAY,
                EventCategory.SIBLING_BIRTHDAY,
                EventCategory.RELATIVE_BIRTHDAY,
                EventCategory.PET_BIRTHDAY
            )
            EventType.ANNIVERSARY -> listOf(
                EventCategory.WEDDING_ANNIVERSARY,
                EventCategory.RELATIONSHIP_ANNIVERSARY,
                EventCategory.DATING_ANNIVERSARY,
                EventCategory.ENGAGEMENT_ANNIVERSARY,
                EventCategory.PROMISE_ANNIVERSARY,
                EventCategory.GRADUATION_DAY,
                EventCategory.WORK_ANNIVERSARY,
                EventCategory.FIRST_DAY_OF_WORK,
                EventCategory.HOUSE_ANNIVERSARY,
                EventCategory.FAMILY_ANNIVERSARY
            )
            EventType.FAMILY -> listOf(
                EventCategory.MOTHERS_DAY,
                EventCategory.FATHERS_DAY
            )
            EventType.HOLIDAY -> listOf(
                EventCategory.EID_AL_FITR,
                EventCategory.EID_AL_ADHA,
                EventCategory.NEW_YEARS_EVE,
                EventCategory.VALENTINES_DAY,
                EventCategory.TEACHERS_DAY,
                EventCategory.APRIL_23,
                EventCategory.MAY_19,
                EventCategory.AUGUST_30,
                EventCategory.OCTOBER_29
            )
            EventType.CUSTOM -> listOf(EventCategory.CUSTOM)
        }
    }
}
