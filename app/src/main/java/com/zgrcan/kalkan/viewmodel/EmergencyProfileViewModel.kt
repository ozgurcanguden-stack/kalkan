package com.zgrcan.kalkan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.contacts.normalizedPhoneDigits
import com.zgrcan.kalkan.data.emergencyprofile.EmergencyProfileRepository
import com.zgrcan.kalkan.model.EmergencyBloodTypes
import com.zgrcan.kalkan.model.EmergencyProfile
import com.zgrcan.kalkan.util.GuestFeatureMessages
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
class EmergencyProfileViewModel @Inject constructor(
    private val emergencyProfileRepository: EmergencyProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyProfileUiState())
    val uiState: StateFlow<EmergencyProfileUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var activeUid: String? = null
    private var isGuestUser: Boolean = false

    /** Acil Durum Kartı ekranı — tek seferlik okuma; sürekli listener açmaz. */
    fun openViewScreen(uid: String?, isGuest: Boolean) {
        observeJob?.cancel()
        activeUid = uid
        isGuestUser = isGuest

        if (uid.isNullOrBlank() || isGuest) {
            _uiState.value = EmergencyProfileUiState(
                isLoading = false,
                errorMessage = if (isGuest) null else "Acil Durum Kartı için giriş yapmalısınız.",
            )
            return
        }

        val cached = emergencyProfileRepository.getCachedProfile(uid)
        _uiState.value = EmergencyProfileUiState(
            profile = cached,
            isLoading = cached == null,
            errorMessage = null,
        )

        observeJob = viewModelScope.launch {
            val profile = emergencyProfileRepository.fetchProfileOnce(uid)
            _uiState.update {
                it.copy(profile = profile, isLoading = false, errorMessage = null)
            }
        }
    }

    fun startObserving(uid: String?, isGuest: Boolean = false) {
        observeJob?.cancel()
        activeUid = uid
        isGuestUser = isGuest

        if (uid.isNullOrBlank()) {
            _uiState.value = EmergencyProfileUiState(
                isLoading = false,
                errorMessage = "Acil Durum Kartı için giriş yapmalısınız.",
            )
            return
        }

        val cached = emergencyProfileRepository.getCachedProfile(uid)
        _uiState.update {
            it.copy(
                isLoading = cached == null,
                profile = cached,
                errorMessage = null,
            )
        }

        observeJob = viewModelScope.launch {
            emergencyProfileRepository.observeProfile(uid)
                .catch { error ->
                    Log.e(TAG, "Failed to observe emergency profile", error)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            profile = state.profile ?: emergencyProfileRepository.getCachedProfile(uid),
                            errorMessage = if (state.profile == null) {
                                "Acil Durum Kartı yüklenemedi. Son bilinen veriler gösteriliyor."
                            } else {
                                null
                            },
                        )
                    }
                }
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun prepareEditForm() {
        val profile = _uiState.value.profile
        _uiState.update {
            it.copy(
                form = profile?.toFormState() ?: EmergencyProfileFormState(),
                formError = null,
            )
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(fullName = value), formError = null) }
    }

    fun onBloodTypeChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(bloodType = value), formError = null) }
    }

    fun onAllergiesChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(allergies = value), formError = null) }
    }

    fun onChronicDiseasesChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(chronicDiseases = value), formError = null) }
    }

    fun onMedicationsChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(medications = value), formError = null) }
    }

    fun onEmergencyNoteChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(emergencyNote = value), formError = null) }
    }

    fun onPrimaryContactNameChange(value: String) {
        _uiState.update { it.copy(form = it.form.copy(primaryContactName = value), formError = null) }
    }

    fun onPrimaryContactPhoneChange(value: String) {
        var digits = value.filter { it.isDigit() }.trimStart('0')
        if (digits.isNotEmpty() && !digits.startsWith("5")) {
            digits = digits.dropWhile { it != '5' }
        }
        if (digits.length > 10) digits = digits.take(10)
        _uiState.update { it.copy(form = it.form.copy(primaryContactPhone = digits), formError = null) }
    }

    fun saveProfile(onSuccess: () -> Unit = {}) {
        val uid = activeUid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(formError = "Kaydetmek için giriş yapmalısınız.") }
            return
        }
        if (isGuestUser) {
            _uiState.update { it.copy(formError = GuestFeatureMessages.SIGN_IN_REQUIRED) }
            return
        }

        val form = _uiState.value.form
        val phoneDigits = form.primaryContactPhone.normalizedPhoneDigits()
        if (phoneDigits.isNotEmpty() && phoneDigits.length < 10) {
            _uiState.update { it.copy(formError = "Telefon numarası 10 hane olmalıdır.") }
            return
        }
        if (phoneDigits.isNotEmpty() && !phoneDigits.startsWith("5")) {
            _uiState.update { it.copy(formError = "Telefon numarası 5 ile başlamalıdır.") }
            return
        }
        if (form.bloodType !in EmergencyBloodTypes.options) {
            _uiState.update { it.copy(formError = "Geçerli bir kan grubu seçin.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, formError = null) }
            emergencyProfileRepository.saveProfile(
                uid = uid,
                profile = form.toProfile(),
            ).onSuccess { saved ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        profile = saved,
                        successMessage = "Acil Durum Kartı kaydedildi.",
                        form = saved.toFormState(),
                    )
                }
                onSuccess()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        formError = "Acil Durum Kartı kaydedilemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }

    fun deleteProfile(onSuccess: () -> Unit = {}) {
        val uid = activeUid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Silmek için giriş yapmalısınız.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            emergencyProfileRepository.deleteProfile(uid)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            profile = null,
                            form = EmergencyProfileFormState(),
                            successMessage = "Acil Durum Kartı silindi.",
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "Silme işlemi başarısız oldu.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, formError = null) }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val TAG = "EmergencyProfileVM"
    }
}

data class EmergencyProfileUiState(
    val profile: EmergencyProfile? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val form: EmergencyProfileFormState = EmergencyProfileFormState(),
    val formError: String? = null,
)

data class EmergencyProfileFormState(
    val fullName: String = "",
    val bloodType: String = EmergencyBloodTypes.UNKNOWN,
    val allergies: String = "",
    val chronicDiseases: String = "",
    val medications: String = "",
    val emergencyNote: String = "",
    val primaryContactName: String = "",
    val primaryContactPhone: String = "",
)

private fun EmergencyProfile.toFormState() = EmergencyProfileFormState(
    fullName = fullName,
    bloodType = bloodType,
    allergies = allergies,
    chronicDiseases = chronicDiseases,
    medications = medications,
    emergencyNote = emergencyNote,
    primaryContactName = primaryContactName,
    primaryContactPhone = primaryContactPhone,
)

private fun EmergencyProfileFormState.toProfile() = EmergencyProfile(
    fullName = fullName.trim(),
    bloodType = bloodType,
    allergies = allergies.trim(),
    chronicDiseases = chronicDiseases.trim(),
    medications = medications.trim(),
    emergencyNote = emergencyNote.trim(),
    primaryContactName = primaryContactName.trim(),
    primaryContactPhone = primaryContactPhone.normalizedPhoneDigits(),
)
