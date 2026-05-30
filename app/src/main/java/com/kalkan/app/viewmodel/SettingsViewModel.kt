package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.export.UserDataExportRepository
import com.kalkan.app.data.settings.SettingsRepository
import com.kalkan.app.model.AppUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val exportRepository: UserDataExportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun runManualBackup(user: AppUser?, deviceName: String, appVersion: String) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isBackupLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.manualBackup(user, deviceName, appVersion)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isBackupLoading = false,
                            successMessage = "Verileriniz bulut yedeğine başarıyla eşitlendi."
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isBackupLoading = false,
                            error = err.message ?: "Yedekleme sırasında hata oluştu."
                        )
                    }
                }
        }
    }

    fun clearUserData(user: AppUser?, onFinished: () -> Unit) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isClearLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.clearUserData(user)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isClearLoading = false,
                            successMessage = "Verileriniz başarıyla temizlendi."
                        )
                    }
                    onFinished()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isClearLoading = false,
                            error = err.message ?: "Veri temizleme sırasında hata oluştu."
                        )
                    }
                }
        }
    }

    fun deleteAccount(user: AppUser?, onFinished: () -> Unit) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isDeleteLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.deleteAccount(user)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleteLoading = false,
                            successMessage = "Hesabınız başarıyla silindi."
                        )
                    }
                    onFinished()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isDeleteLoading = false,
                            error = err.message ?: "Hesap silinirken hata oluştu."
                        )
                    }
                }
        }
    }

    fun exportUserData(user: AppUser?, onExportReady: (String) -> Unit) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isExportLoading = true, error = null) }
        viewModelScope.launch {
            exportRepository.exportUserData(user)
                .onSuccess { jsonString ->
                    _uiState.update { it.copy(isExportLoading = false) }
                    onExportReady(jsonString)
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isExportLoading = false,
                            error = err.message ?: "Veriler dışa aktarılırken hata oluştu."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

data class SettingsUiState(
    val isBackupLoading: Boolean = false,
    val isExportLoading: Boolean = false,
    val isClearLoading: Boolean = false,
    val isDeleteLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
