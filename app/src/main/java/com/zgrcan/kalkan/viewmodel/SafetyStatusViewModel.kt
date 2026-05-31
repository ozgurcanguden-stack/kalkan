package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.location.LocationFetchResult
import com.zgrcan.kalkan.data.location.LocationRepository
import com.zgrcan.kalkan.data.safety.LOCATION_PERMISSION_DENIED_MESSAGE
import com.zgrcan.kalkan.data.safety.LOCATION_SAVED_MESSAGE
import com.zgrcan.kalkan.data.safety.LOCATION_UNAVAILABLE_MESSAGE
import com.zgrcan.kalkan.data.safety.SAFETY_STATUS_AUTH_USER_MESSAGE
import com.zgrcan.kalkan.data.safety.SAFETY_STATUS_SAVE_USER_MESSAGE
import com.zgrcan.kalkan.data.safety.SafetyStatusRepository
import com.zgrcan.kalkan.data.safety.logSafetyStatusError
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.model.UserLocation
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
    private val locationRepository: LocationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SafetyStatusUiState())
    val uiState: StateFlow<SafetyStatusUiState> = _uiState.asStateFlow()

    private val submitMutex = Mutex()
    private var submitJob: Job? = null

    fun submitSafetyStatus(statusType: SafetyStatusType, user: AppUser?) {
        submit(statusType, user, permissionGranted = null)
    }

    fun submitSafetyStatusWithLocation(
        statusType: SafetyStatusType,
        user: AppUser?,
        permissionGranted: Boolean,
    ) {
        if (!statusType.requiresLocationAttempt) {
            submitSafetyStatus(statusType, user)
            return
        }
        submit(statusType, user, permissionGranted = permissionGranted)
    }

    private fun submit(
        statusType: SafetyStatusType,
        user: AppUser?,
        permissionGranted: Boolean?,
    ) {
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

                val (userLocation, locationResult) = resolveLocationIfNeeded(
                    statusType = statusType,
                    permissionGranted = permissionGranted,
                )

                val resolvedMessage = buildSnackbarMessage(
                    statusType = statusType,
                    locationResult = locationResult,
                )

                safetyStatusRepository.createSafetyStatus(
                    uid = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    statusType = statusType,
                    location = userLocation,
                ).onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = resolvedMessage,
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

    private suspend fun resolveLocationIfNeeded(
        statusType: SafetyStatusType,
        permissionGranted: Boolean?,
    ): Pair<UserLocation?, LocationFetchResult?> {
        if (!statusType.requiresLocationAttempt) {
            return null to null
        }

        val fetchResult = locationRepository.getCurrentLocation(
            hasPermission = permissionGranted == true,
        )

        return when (fetchResult) {
            is LocationFetchResult.Success -> fetchResult.location to fetchResult
            LocationFetchResult.PermissionDenied -> null to fetchResult
            LocationFetchResult.Unavailable -> null to fetchResult
        }
    }

    private fun buildSnackbarMessage(
        statusType: SafetyStatusType,
        locationResult: LocationFetchResult?,
    ): String {
        if (locationResult == null) {
            return statusType.successMessage
        }

        return when (locationResult) {
            is LocationFetchResult.Success -> when (statusType) {
                SafetyStatusType.SOS -> "${statusType.successMessage} $LOCATION_SAVED_MESSAGE"
                SafetyStatusType.SHARE_LOCATION -> LOCATION_SAVED_MESSAGE
                else -> statusType.successMessage
            }
            LocationFetchResult.PermissionDenied -> LOCATION_PERMISSION_DENIED_MESSAGE
            LocationFetchResult.Unavailable -> LOCATION_UNAVAILABLE_MESSAGE
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
