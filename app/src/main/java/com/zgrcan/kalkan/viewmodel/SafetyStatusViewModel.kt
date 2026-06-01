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

    fun loadLatestStatus(uid: String?) {
        viewModelScope.launch {
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(currentStatusType = null) }
                return@launch
            }
            safetyStatusRepository.getLatestSafetyStatus(uid)
                .onSuccess { status ->
                    _uiState.update { it.copy(currentStatusType = status?.statusType) }
                }
        }
    }

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
                val cooldownMessage = resolveCooldownMessage(statusType, user)
                if (cooldownMessage != null) {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = cooldownMessage,
                            isError = true,
                        )
                    }
                    return@withLock
                }

                _uiState.update {
                    it.copy(
                        isSubmitting = true,
                        currentStatusType = statusType,
                    )
                }

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
                    val latest = safetyStatusRepository.getLatestSafetyStatus(user.uid).getOrNull()
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentStatusType = latest?.statusType,
                            snackbarMessage = SAFETY_STATUS_SAVE_USER_MESSAGE,
                            isError = true,
                        )
                    }
                }
            }
        }
    }

    private suspend fun resolveCooldownMessage(
        statusType: SafetyStatusType,
        user: AppUser,
    ): String? {
        if (statusType == SafetyStatusType.SAFE) return null
        val cooldownSnapshot = safetyStatusRepository.getStatusCooldownSnapshot(user.uid).getOrNull()
            ?: return null
        val now = System.currentTimeMillis()
        val remainingMs = when (statusType) {
            SafetyStatusType.SOS -> cooldownRemaining(
                now = now,
                lastSentAt = cooldownSnapshot.lastSosAt,
                cooldownMs = SAFETY_ACTION_COOLDOWN_MS,
            )
            SafetyStatusType.NEED_HELP -> cooldownRemaining(
                now = now,
                lastSentAt = cooldownSnapshot.lastHelpRequestAt,
                cooldownMs = SAFETY_ACTION_COOLDOWN_MS,
            )
            SafetyStatusType.SHARE_LOCATION -> cooldownRemaining(
                now = now,
                lastSentAt = cooldownSnapshot.lastLocationShareAt,
                cooldownMs = SAFETY_ACTION_COOLDOWN_MS,
            )
            SafetyStatusType.SAFE -> 0L
        }
        if (remainingMs <= 0L) return null
        val remainingSeconds = millisToCeilSeconds(remainingMs)
        return when (statusType) {
            SafetyStatusType.SOS ->
                "SOS çağrınız kısa süre önce gönderildi. Tekrar göndermek için $remainingSeconds saniye bekleyin."
            SafetyStatusType.NEED_HELP ->
                "Yardım talebiniz kısa süre önce gönderildi. Tekrar göndermek için $remainingSeconds saniye bekleyin."
            SafetyStatusType.SHARE_LOCATION ->
                "Konum bilginiz kısa süre önce paylaşıldı. Tekrar paylaşmak için $remainingSeconds saniye bekleyin."
            SafetyStatusType.SAFE -> null
        }
    }

    private fun cooldownRemaining(
        now: Long,
        lastSentAt: Long?,
        cooldownMs: Long,
    ): Long {
        val last = lastSentAt ?: return 0L
        val elapsed = now - last
        return (cooldownMs - elapsed).coerceAtLeast(0L)
    }

    private fun millisToCeilSeconds(ms: Long): Long = ((ms + 999L) / 1000L).coerceAtLeast(1L)

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

    companion object {
        private const val SAFETY_ACTION_COOLDOWN_MS = 60_000L
    }
}

data class SafetyStatusUiState(
    val currentStatusType: SafetyStatusType? = null,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val isError: Boolean = false,
)
