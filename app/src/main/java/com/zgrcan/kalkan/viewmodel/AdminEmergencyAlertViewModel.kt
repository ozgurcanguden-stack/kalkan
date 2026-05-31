package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.alert.EmergencyAlertRepository
import com.zgrcan.kalkan.data.auth.AuthRepository
import com.zgrcan.kalkan.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminEmergencyAlertState(
    val title: String = "",
    val body: String = "",
    val region: String = "Tüm Türkiye",
    val priority: String = "Önemli", // Bilgilendirme, Önemli, Kritik
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showConfirmDialog: Boolean = false,
)

@HiltViewModel
class AdminEmergencyAlertViewModel @Inject constructor(
    private val alertRepository: EmergencyAlertRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminEmergencyAlertState())
    val uiState: StateFlow<AdminEmergencyAlertState> = _uiState.asStateFlow()

    val hasAdminAccess: StateFlow<Boolean> = authRepository.currentFirebaseUser
        .flatMapLatest { authUser ->
            if (authUser == null) {
                flowOf(false)
            } else {
                userRepository.observeUser(authUser.uid).combine(flowOf(authUser)) { appUser, _ ->
                    appUser?.isAdmin == true
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= 80) {
            _uiState.update { it.copy(title = newTitle, errorMessage = null) }
        }
    }

    fun onBodyChange(newBody: String) {
        if (newBody.length <= 250) {
            _uiState.update { it.copy(body = newBody, errorMessage = null) }
        }
    }

    fun onRegionChange(newRegion: String) {
        if (newRegion.length <= 80) {
            _uiState.update { it.copy(region = newRegion, errorMessage = null) }
        }
    }

    fun onPrioritySelect(priority: String) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onPublishClick() {
        val state = _uiState.value
        if (state.title.isBlank() || state.body.isBlank() || state.region.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lütfen tüm zorunlu alanları doldurun.") }
            return
        }
        _uiState.update { it.copy(showConfirmDialog = true, errorMessage = null) }
    }

    fun onConfirmDismiss() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onConfirmPublish() {
        val state = _uiState.value
        _uiState.update { it.copy(showConfirmDialog = false, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Kullanıcı oturumu bulunamadı.") }
                return@launch
            }

            // Important: Use "TEST" logic. If the user hasn't added "TEST", enforce it for safety since this goes to all users.
            // Wait, the prompt said: "test ederken başlık/gövdeye mutlaka "TEST" ibaresi eklemeden deneme gönderimi yapma."
            // This is a manual test rule for me, the developer. I don't need to hardcode "TEST" in the app, I just need to use "TEST" when I submit the form.

            val result = alertRepository.createAlert(
                title = state.title.trim(),
                body = state.body.trim(),
                region = state.region.trim(),
                priority = state.priority,
                target = "all_users",
                createdByUid = authUser.uid,
                createdByName = authUser.displayName
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Acil uyarı yayınlandı."
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Acil uyarı yayınlanamadı. Lütfen tekrar deneyin."
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
