package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.kalkan.app.data.auth.AuthRepository
import com.kalkan.app.data.user.UserRepository
import com.kalkan.app.model.AppUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var userObserverJob: Job? = null

    init {
        observeAuthState()
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signInAsGuest()
                .onSuccess { syncUser(it) }
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
                    } else {
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
        launchSignIn {
            authRepository.signInWithGoogleIdToken(idToken)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState(isLoading = false)
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
                    syncUser(firebaseUser)
                }
            }
        }
    }

    private fun launchSignIn(block: suspend () -> Result<FirebaseUser>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            block()
                .onSuccess { syncUser(it) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toAuthMessage(),
                        )
                    }
                }
        }
    }

    private suspend fun syncUser(firebaseUser: FirebaseUser) {
        userRepository.ensureUser(firebaseUser)
            .onSuccess { appUser ->
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isAuthenticated = true,
                    user = appUser,
                    hasAdminAccess = appUser.isAdmin,
                )
                observeUserRole(appUser.uid)
            }
            .onFailure { error ->
                if (error.isOfflineFirestoreError()) {
                    val fallbackUser = firebaseUser.toFallbackAppUser()
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isAuthenticated = true,
                        user = fallbackUser,
                        hasAdminAccess = false,
                    )
                } else {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.toAuthMessage(),
                    )
                }
            }
    }

    private fun observeUserRole(uid: String) {
        userObserverJob?.cancel()
        userObserverJob = viewModelScope.launch {
            userRepository.observeUser(uid).collect { appUser ->
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
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val user: AppUser? = null,
    val hasAdminAccess: Boolean = false,
    val errorMessage: String? = null,
)
