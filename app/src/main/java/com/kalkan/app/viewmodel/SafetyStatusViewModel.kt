package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.safety.SAFETY_STATUS_AUTH_USER_MESSAGE
import com.kalkan.app.data.safety.SAFETY_STATUS_SAVE_USER_MESSAGE
import com.kalkan.app.data.safety.SafetyStatusRepository
import com.kalkan.app.data.safety.logSafetyStatusError
import com.kalkan.app.model.AppUser
import com.kalkan.app.model.SafetyStatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class SafetyStatusViewModel @Inject constructor(
    private val safetyStatusRepository: SafetyStatusRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SafetyStatusUiState())
    val uiState: StateFlow<SafetyStatusUiState> = _uiState.asStateFlow()

    private val submitMutex = Mutex()
    private var submitJob: Job? = null

    fun submitSafetyStatus(statusType: SafetyStatusType, user: AppUser?) {
        if (submitJob?.isActive == true) {
            return
        }

        if (user == null || user.uid.isBlank()) {
            _uiState.update {
                it.copy(
                    snackbarMessage = SAFETY_STATUS_AUTH_USER_MESSAGE,
                    isError = true,
                )
            }
            return
        }

        submitJob = viewModelScope.launch {
            submitMutex.withLock {
                _uiState.update { it.copy(isSubmitting = true) }
                safetyStatusRepository.createSafetyStatus(
                    uid = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    statusType = statusType,
                ).onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = statusType.successMessage,
                            isError = false,
                        )
                    }
                }.onFailure { error ->
                    logSafetyStatusError("createSafetyStatus", error)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = SAFETY_STATUS_SAVE_USER_MESSAGE,
                            isError = true,
                        )
                    }
                }
            }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

data class SafetyStatusUiState(
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val isError: Boolean = false,
)
