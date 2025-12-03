package com.mrsuffix.remindly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.repository.EventRepository
import com.mrsuffix.remindly.domain.repository.SettingsRepository
import com.mrsuffix.remindly.domain.usecase.AddTurkishHolidaysUseCase
import com.mrsuffix.remindly.domain.usecase.DeleteEventUseCase
import com.mrsuffix.remindly.domain.usecase.GetEventsThisMonthUseCase
import com.mrsuffix.remindly.domain.usecase.GetEventsThisWeekUseCase
import com.mrsuffix.remindly.domain.usecase.GetUpcomingEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val upcomingEvents: List<Event> = emptyList(),
    val thisWeekEvents: List<Event> = emptyList(),
    val thisMonthEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: HomeTab = HomeTab.UPCOMING,
    val searchQuery: String = "",
    val searchResults: List<Event> = emptyList(),
    val isSearchActive: Boolean = false
)

enum class HomeTab {
    UPCOMING, THIS_WEEK, THIS_MONTH
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
    private val getEventsThisWeekUseCase: GetEventsThisWeekUseCase,
    private val getEventsThisMonthUseCase: GetEventsThisMonthUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val addTurkishHolidaysUseCase: AddTurkishHolidaysUseCase,
    private val settingsRepository: SettingsRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadEvents()
        checkFirstLaunch()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            combine(
                getUpcomingEventsUseCase(),
                getEventsThisWeekUseCase(),
                getEventsThisMonthUseCase()
            ) { upcoming, thisWeek, thisMonth ->
                Triple(upcoming, thisWeek, thisMonth)
            }.collect { (upcoming, thisWeek, thisMonth) ->
                _uiState.update { state ->
                    state.copy(
                        upcomingEvents = upcoming,
                        thisWeekEvents = thisWeek,
                        thisMonthEvents = thisMonth,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun checkFirstLaunch() {
        viewModelScope.launch {
            if (settingsRepository.isFirstLaunch()) {
                addTurkishHolidaysUseCase()
                settingsRepository.setFirstLaunchComplete()
            }
        }
    }
    
    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            deleteEventUseCase(eventId)
        }
    }
    
    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active, searchQuery = if (!active) "" else it.searchQuery) }
    }
    
    private var searchJob: kotlinx.coroutines.Job? = null
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                // Small delay for debouncing
                kotlinx.coroutines.delay(150)
                eventRepository.searchEvents(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }
}
