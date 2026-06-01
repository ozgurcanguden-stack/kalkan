package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.zgrcan.kalkan.data.auth.AuthRepository
import com.zgrcan.kalkan.data.fcm.FcmRepository
import com.zgrcan.kalkan.data.user.UserRepository
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import android.util.Log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val fcmRepository: FcmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var userObserverJob: Job? = null
    private var pendingLoginSuccessMessage: String? = null

    init {
        observeAuthState()
    }

    fun signInAsGuest() {
        pendingLoginSuccessMessage = "Misafir olarak giriş başarılı."
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signInAsGuest()
                .onSuccess { syncUser(it, fromUserAction = true) }
                .onFailure { error ->
                    if (error.isFirebaseConfigurationError()) {
                        val fallbackUser = AppUser(
                            uid = "local_guest",
                            displayName = "Misafir Kullanici",
                            createdAt = System.currentTimeMillis(),
                            lastLoginAt = System.currentTimeMillis(),
                        )
                        _uiState.value = AuthUiState(
                            isLoading = false,
                            isAuthenticated = true,
                            user = fallbackUser,
                            hasAdminAccess = false,
                        )
                        showPendingLoginSuccess()
                    } else {
                        pendingLoginSuccessMessage = null
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.toAuthMessage(),
                            )
                        }
                    }
                }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        pendingLoginSuccessMessage = "Google ile giriş başarılı."
        val previousAnonymousUid = authRepository.getCurrentUser()
            ?.takeIf { it.isAnonymous }
            ?.uid
        launchSignIn {
            authRepository.signInWithGoogleIdToken(idToken)
                .onSuccess { firebaseUser ->
                    if (previousAnonymousUid != null && previousAnonymousUid != firebaseUser.uid) {
                        userRepository.markUserInactive(previousAnonymousUid)
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            pendingLoginSuccessMessage = null
            _uiState.value = AuthUiState(isLoading = false)
        }
    }

    fun syncFcmToken(notificationPermissionGranted: Boolean) {
        viewModelScope.launch {
            fcmRepository.syncTokenForCurrentUser(notificationPermissionGranted)
        }
    }

    fun updateNotificationPermission(granted: Boolean) {
        viewModelScope.launch {
            fcmRepository.updateNotificationPermissionForCurrentUser(granted)
            fcmRepository.syncTokenForCurrentUser(granted)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun showGoogleSetupError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Google girisi icin Firebase Console'da Web Client ID ve SHA ayarlari tamamlanmali.",
            )
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentFirebaseUser.collect { firebaseUser ->
                if (firebaseUser == null) {
                    userObserverJob?.cancel()
                    _uiState.value = AuthUiState(isLoading = false)
                } else {
                    syncUser(firebaseUser, fromUserAction = false)
                }
            }
        }
    }

    private fun launchSignIn(block: suspend () -> Result<FirebaseUser>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            block()
                .onSuccess { syncUser(it, fromUserAction = true) }
                .onFailure { error ->
                    pendingLoginSuccessMessage = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toAuthMessage(),
                        )
                    }
                }
        }
    }

    private suspend fun syncUser(firebaseUser: FirebaseUser, fromUserAction: Boolean) {
        val fallbackUser = firebaseUser.toFallbackAppUser()

        if (fromUserAction) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else if (!_uiState.value.isAuthenticated) {
            _uiState.value = AuthUiState(
                isLoading = false,
                isAuthenticated = true,
                user = fallbackUser,
                hasAdminAccess = false,
            )
        }

        val ensureResult = try {
            withTimeout(SYNC_USER_TIMEOUT_MS) {
                userRepository.ensureUser(firebaseUser)
            }
        } catch (_: TimeoutCancellationException) {
            Log.w(TAG, "ensureUser timed out; using fallback session")
            Result.success(fallbackUser)
        }

        ensureResult
            .onSuccess { appUser ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    user = appUser,
                    hasAdminAccess = appUser.isAdmin,
                )
                if (fromUserAction) {
                    showPendingLoginSuccess()
                }
                observeUserRole(appUser.uid)
                viewModelScope.launch {
                    fcmRepository.syncTokenForCurrentUser(appUser.notificationPermissionGranted)
                }
            }
            .onFailure { error ->
                if (error.isOfflineFirestoreError()) {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isAuthenticated = true,
                        user = fallbackUser,
                        hasAdminAccess = false,
                    )
                    if (fromUserAction) {
                        showPendingLoginSuccess()
                    }
                    observeUserRole(fallbackUser.uid)
                } else {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isAuthenticated = if (fromUserAction) false else true,
                        user = if (fromUserAction) null else fallbackUser,
                        hasAdminAccess = false,
                        errorMessage = error.toAuthMessage(),
                    )
                }
            }
    }

    private fun showPendingLoginSuccess() {
        pendingLoginSuccessMessage?.let(AppTopNotificationCenter::showSuccess)
        pendingLoginSuccessMessage = null
    }

    private fun observeUserRole(uid: String) {
        userObserverJob?.cancel()
        userObserverJob = viewModelScope.launch {
            userRepository.observeUser(uid)
                .catch { error ->
                    Log.e(TAG, "observeUser flow failed", error)
                }
                .collect { appUser ->
                    if (appUser != null) {
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                user = appUser,
                                hasAdminAccess = appUser.isAdmin,
                            )
                        }
                    }
                }
        }
    }
}

private const val TAG = "AuthViewModel"
private const val SYNC_USER_TIMEOUT_MS = 12_000L

private fun FirebaseUser.toFallbackAppUser(): AppUser =
    AppUser(
        uid = uid,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: "Misafir Kullanici",
        email = email,
        photoUrl = photoUrl?.toString(),
        createdAt = System.currentTimeMillis(),
        lastLoginAt = System.currentTimeMillis(),
    )

private fun Throwable.toAuthMessage(): String {
    val authCode = (this as? FirebaseAuthException)?.errorCode
    val rawMessage = localizedMessage.orEmpty()
    val mappedByCode = when (authCode) {
        "ERROR_CONFIGURATION_NOT_FOUND" ->
            "Misafir girisi icin Firebase Console'da Anonymous oturum acma yontemi etkinlestirilmeli."
        "ERROR_NETWORK_REQUEST_FAILED" ->
            "Internet baglantisi kurulamadigi icin giris tamamlanamadi."
        "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_IDP_RESPONSE" ->
            "Google girisi icin Firebase SHA-1 / SHA-256 ve OAuth ayarlarini kontrol edin."
        else -> null
    }

    return when {
        mappedByCode != null -> mappedByCode
        rawMessage.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
            "Misafir girisi icin Firebase Console'da Anonymous oturum acma yontemi etkinlestirilmeli."
        rawMessage.contains("network", ignoreCase = true) ->
            "Internet baglantisi kurulamadigi icin giris tamamlanamadi."
        isOfflineFirestoreError() ->
            "Kullanici profili su an senkronize edilemedi. Internet baglantisini kontrol edin."
        else -> rawMessage.takeIf { it.isNotBlank() } ?: "Giris islemi tamamlanamadi."
    }
}

private fun Throwable.isFirebaseConfigurationError(): Boolean {
    val authCode = (this as? FirebaseAuthException)?.errorCode
    val rawMessage = localizedMessage.orEmpty()
    return authCode == "ERROR_CONFIGURATION_NOT_FOUND" ||
        rawMessage.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)
}

private fun Throwable.isOfflineFirestoreError(): Boolean =
    localizedMessage.orEmpty().contains("client is offline", ignoreCase = true)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: AppUser? = null,
    val hasAdminAccess: Boolean = false,
    val errorMessage: String? = null,
)
