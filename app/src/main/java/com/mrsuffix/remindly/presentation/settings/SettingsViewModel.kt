package com.mrsuffix.remindly.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsuffix.remindly.data.contacts.ContactWithBirthday
import com.mrsuffix.remindly.data.contacts.ContactsHelper
import com.mrsuffix.remindly.domain.model.Settings
import com.mrsuffix.remindly.domain.model.ThemeMode
import com.mrsuffix.remindly.domain.repository.EventRepository
import com.mrsuffix.remindly.domain.repository.SettingsRepository
import com.mrsuffix.remindly.domain.usecase.AddTurkishHolidaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val settings: Settings = Settings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
    val showTimePicker: Boolean = false,
    val showThemePicker: Boolean = false,
    val showBackupDialog: Boolean = false,
    val showRestoreDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showDeleteAllDialog: Boolean = false,
    val showRestoreDefaultsDialog: Boolean = false,
    val showContactsDialog: Boolean = false,
    val exportedData: String? = null,
    val importResult: ImportResult? = null,
    val contactsWithBirthdays: List<ContactWithBirthday> = emptyList(),
    val isLoadingContacts: Boolean = false,
    val contactsImportResult: ContactsImportResult? = null
)

sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

sealed class ContactsImportResult {
    data class Success(val count: Int) : ContactsImportResult()
    data class Error(val message: String) : ContactsImportResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val eventRepository: EventRepository,
    private val addTurkishHolidaysUseCase: AddTurkishHolidaysUseCase,
    private val contactsHelper: ContactsHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                settingsRepository.getThemeMode()
            ) { settings, themeMode ->
                Pair(settings, themeMode)
            }.collect { (settings, themeMode) ->
                _uiState.update { state ->
                    state.copy(
                        settings = settings,
                        themeMode = themeMode,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun setNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setNotificationTime(time)
            _uiState.update { it.copy(showTimePicker = false) }
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _uiState.update { it.copy(showThemePicker = false) }
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
    
    fun showTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }
    
    fun showThemePicker(show: Boolean) {
        _uiState.update { it.copy(showThemePicker = show) }
    }
    
    fun showBackupDialog(show: Boolean) {
        _uiState.update { it.copy(showBackupDialog = show) }
    }
    
    fun showRestoreDialog(show: Boolean) {
        _uiState.update { it.copy(showRestoreDialog = show) }
    }
    
    fun showAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }
    
    fun exportData() {
        viewModelScope.launch {
            val data = eventRepository.exportEvents()
            _uiState.update { it.copy(exportedData = data, showBackupDialog = true) }
        }
    }
    
    fun importData(json: String) {
        viewModelScope.launch {
            val count = eventRepository.importEvents(json)
            val result = if (count > 0) {
                ImportResult.Success(count)
            } else {
                ImportResult.Error("Veri içe aktarılamadı")
            }
            _uiState.update { it.copy(importResult = result, showRestoreDialog = false) }
        }
    }
    
    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }
    
    fun clearExportedData() {
        _uiState.update { it.copy(exportedData = null) }
    }
    
    // Delete All Events
    fun showDeleteAllDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteAllDialog = show) }
    }
    
    fun deleteAllEvents() {
        viewModelScope.launch {
            eventRepository.deleteAllEvents()
            _uiState.update { it.copy(showDeleteAllDialog = false) }
        }
    }
    
    // Restore Default Holidays
    fun showRestoreDefaultsDialog(show: Boolean) {
        _uiState.update { it.copy(showRestoreDefaultsDialog = show) }
    }
    
    fun restoreDefaultHolidays() {
        viewModelScope.launch {
            eventRepository.deleteHolidayEvents()
            addTurkishHolidaysUseCase()
            _uiState.update { it.copy(showRestoreDefaultsDialog = false) }
        }
    }
    
    // Contacts Import
    fun showContactsDialog(show: Boolean) {
        _uiState.update { it.copy(showContactsDialog = show) }
        if (show) {
            loadContactsWithBirthdays()
        }
    }
    
    fun loadContactsWithBirthdays() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContacts = true) }
            try {
                val contacts = contactsHelper.getContactsWithBirthdays()
                _uiState.update { it.copy(
                    contactsWithBirthdays = contacts,
                    isLoadingContacts = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    contactsWithBirthdays = emptyList(),
                    isLoadingContacts = false,
                    contactsImportResult = ContactsImportResult.Error("Rehber okunamadı")
                ) }
            }
        }
    }
    
    fun toggleContactSelection(contactId: String) {
        _uiState.update { state ->
            val updatedContacts = state.contactsWithBirthdays.map { contact ->
                if (contact.id == contactId) {
                    contact.copy(isSelected = !contact.isSelected)
                } else {
                    contact
                }
            }
            state.copy(contactsWithBirthdays = updatedContacts)
        }
    }
    
    fun selectAllContacts(select: Boolean) {
        _uiState.update { state ->
            val updatedContacts = state.contactsWithBirthdays.map { it.copy(isSelected = select) }
            state.copy(contactsWithBirthdays = updatedContacts)
        }
    }
    
    fun importSelectedContacts() {
        viewModelScope.launch {
            val selectedContacts = _uiState.value.contactsWithBirthdays.filter { it.isSelected }
            if (selectedContacts.isEmpty()) {
                _uiState.update { it.copy(
                    contactsImportResult = ContactsImportResult.Error("Hiç kişi seçilmedi")
                ) }
                return@launch
            }
            
            try {
                val events = selectedContacts.map { contactsHelper.contactToEvent(it) }
                eventRepository.insertEvents(events)
                _uiState.update { it.copy(
                    showContactsDialog = false,
                    contactsImportResult = ContactsImportResult.Success(events.size)
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    contactsImportResult = ContactsImportResult.Error("İçe aktarma başarısız")
                ) }
            }
        }
    }
    
    fun clearContactsImportResult() {
        _uiState.update { it.copy(contactsImportResult = null) }
    }
}
