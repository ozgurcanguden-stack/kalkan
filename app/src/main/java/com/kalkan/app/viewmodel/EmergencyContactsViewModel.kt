package com.kalkan.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.contacts.EmergencyContactRepository
import com.kalkan.app.data.safety.SafetyStatusRepository
import com.kalkan.app.model.UserLocation
import com.kalkan.app.util.EmergencyIntentHelper
import com.kalkan.app.data.contacts.MAX_EMERGENCY_CONTACTS
import com.kalkan.app.data.contacts.normalizedPhoneDigits
import com.kalkan.app.data.contacts.validateEmergencyContact
import com.kalkan.app.model.EmergencyContact
import com.kalkan.app.model.EmergencyContactRelations
import com.kalkan.app.model.SafetyStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val emergencyContactRepository: EmergencyContactRepository,
    private val safetyStatusRepository: SafetyStatusRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var activeUid: String? = null

    fun startObserving(uid: String?) {
        observeJob?.cancel()
        activeUid = uid

        if (uid.isNullOrBlank()) {
            _uiState.value = EmergencyContactsUiState(
                isLoading = false,
                errorMessage = "Acil kişiler için giriş yapmalısınız.",
            )
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadEmergencyMessage(uid)
        observeJob = viewModelScope.launch {
            emergencyContactRepository.observeContacts(uid)
                .catch { error ->
                    Log.e(TAG, "Failed to observe emergency contacts", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Acil kişiler yüklenemedi. Lütfen tekrar deneyin.",
                        )
                    }
                }
                .collect { contacts ->
                    _uiState.update {
                        it.copy(
                            contacts = contacts,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun showAddSheet() {
        if (_uiState.value.contacts.size >= MAX_EMERGENCY_CONTACTS) {
            _uiState.update {
                it.copy(snackbarMessage = "En fazla $MAX_EMERGENCY_CONTACTS acil kişi ekleyebilirsiniz.")
            }
            return
        }
        _uiState.update {
            it.copy(
                showAddSheet = true,
                form = AddEmergencyContactFormState(),
                formError = null,
            )
        }
    }

    fun dismissAddSheet() {
        _uiState.update {
            it.copy(showAddSheet = false, formError = null, isSaving = false)
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(name = value), formError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(phone = value), formError = null) }
    }

    fun onRelationChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(relation = value), formError = null) }
    }

    fun onPrimaryChange(value: Boolean) {
        _uiState.update { it.copy(form = it.form.copy(isPrimary = value), formError = null) }
    }

    fun saveContact() {
        val uid = activeUid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(formError = "Acil kişi eklemek için giriş yapmalısınız.") }
            return
        }

        val state = _uiState.value
        val form = state.form
        val validationError = validateEmergencyContact(
            name = form.name,
            phone = form.phone,
            relation = form.relation,
            existingContacts = state.contacts,
        )
        if (validationError != null) {
            _uiState.update { it.copy(formError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, formError = null) }
            val phoneDigits = form.phone.normalizedPhoneDigits()
            emergencyContactRepository.addContact(
                uid = uid,
                contact = EmergencyContact(
                    name = form.name.trim(),
                    phone = phoneDigits,
                    relation = form.relation,
                    isPrimary = form.isPrimary,
                ),
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showAddSheet = false,
                        snackbarMessage = "Acil kişi kaydedildi.",
                        form = AddEmergencyContactFormState(),
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to add emergency contact", error)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        formError = "Kişi kaydedilemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }

    fun deleteContact(contactId: String) {
        val uid = activeUid ?: return
        viewModelScope.launch {
            emergencyContactRepository.deleteContact(uid, contactId)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Acil kişi silindi.") }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to delete emergency contact", error)
                    _uiState.update {
                        it.copy(snackbarMessage = "Kişi silinemedi. Lütfen tekrar deneyin.")
                    }
                }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showActionMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun loadEmergencyMessage(uid: String) {
        viewModelScope.launch {
            safetyStatusRepository.getLatestSafetyStatus(uid)
                .onSuccess { status ->
                    val location = status?.toUserLocation()
                    _uiState.update {
                        it.copy(emergencyMessage = EmergencyIntentHelper.buildEmergencyMessage(location))
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(emergencyMessage = EmergencyIntentHelper.DEFAULT_EMERGENCY_MESSAGE)
                    }
                }
        }
    }

    private fun SafetyStatus.toUserLocation(): UserLocation? {
        val lat = latitude ?: return null
        val lng = longitude ?: return null
        return UserLocation(
            latitude = lat,
            longitude = lng,
            accuracy = locationAccuracy,
            provider = locationProvider ?: UserLocation.PROVIDER_FUSED,
        )
    }

    companion object {
        private const val TAG = "EmergencyContactsViewModel"
    }
}

data class EmergencyContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val emergencyMessage: String = EmergencyIntentHelper.DEFAULT_EMERGENCY_MESSAGE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showAddSheet: Boolean = false,
    val isSaving: Boolean = false,
    val form: AddEmergencyContactFormState = AddEmergencyContactFormState(),
    val formError: String? = null,
    val snackbarMessage: String? = null,
)

data class AddEmergencyContactFormState(
    val name: String = "",
    val phone: String = "",
    val relation: String = EmergencyContactRelations.options.first(),
    val isPrimary: Boolean = false,
)
