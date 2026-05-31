package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.export.UserDataExportRepository
import com.zgrcan.kalkan.data.settings.SettingsRepository
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.model.BackupFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val exportRepository: UserDataExportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ── Backup Settings ────────────────────────────────────────────────────────

    fun loadBackupSettings(uid: String) {
        if (uid.isBlank()) return
        viewModelScope.launch {
            // Load frequency
            settingsRepository.getBackupFrequency(uid)
                .onSuccess { freq ->
                    _uiState.update { it.copy(backupFrequency = freq) }
                }
            // Load timestamps
            settingsRepository.getBackupTimestamps(uid)
                .onSuccess { (lastManual, lastSync) ->
                    _uiState.update { it.copy(
                        lastManualBackupAt = lastManual,
                        lastSyncAt = lastSync
                    ) }
                }
        }
    }

    fun setBackupFrequency(uid: String, frequency: BackupFrequency) {
        if (uid.isBlank()) return
        viewModelScope.launch {
            settingsRepository.setBackupFrequency(uid, frequency)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            backupFrequency = frequency,
                            lastSyncAt = System.currentTimeMillis(),
                            successMessage = "Otomatik yedekleme ayarı güncellendi.",
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(error = err.message ?: "Yedekleme ayarı kaydedilemedi.") }
                }
        }
    }

    // ── Manual Backup ──────────────────────────────────────────────────────────

    fun runManualBackup(user: AppUser?) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isBackupLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.manualBackup(user)
                .onSuccess {
                    val now = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(
                            isBackupLoading = false,
                            successMessage = "Verileriniz bulut yedeğine başarıyla eşitlendi.",
                            lastManualBackupAt = now,
                            lastSyncAt = now,
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isBackupLoading = false,
                            error = err.message ?: "Yedekleme sırasında hata oluştu.",
                        )
                    }
                }
        }
    }

    // ── Other Operations ───────────────────────────────────────────────────────

    fun clearUserData(user: AppUser?, onFinished: () -> Unit) {
        if (user == null) {
            _uiState.update { it.copy(error = "Geçersiz kullanıcı oturumu.") }
            return
        }

        _uiState.update { it.copy(isClearLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.clearUserData(user)
                .onSuccess {
                    _uiState.update { it.copy(isClearLoading = false, successMessage = "Verileriniz başarıyla temizlendi.") }
                    onFinished()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isClearLoading = false, error = err.message ?: "Veri temizleme sırasında hata oluştu.") }
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
                    _uiState.update { it.copy(isDeleteLoading = false, successMessage = "Hesabınız başarıyla silindi.") }
                    onFinished()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isDeleteLoading = false, error = err.message ?: "Hesap silinirken hata oluştu.") }
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
                    _uiState.update { it.copy(isExportLoading = false, error = err.message ?: "Veriler dışa aktarılırken hata oluştu.") }
                }
        }
    }

    fun updateEarthquakeNotificationSettings(uid: String, enabled: Boolean, minMagnitude: Double?) {
        if (uid.isBlank()) return
        _uiState.update { it.copy(isNotificationSettingsLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            settingsRepository.updateEarthquakeNotificationSettings(uid, enabled, minMagnitude)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isNotificationSettingsLoading = false,
                            successMessage = "Deprem bildirim tercihleri güncellendi."
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isNotificationSettingsLoading = false,
                            error = err.message ?: "Tercihler kaydedilemedi."
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
    val isNotificationSettingsLoading: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val lastManualBackupAt: Long? = null,
    val lastSyncAt: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
) {
    /** Formatlanmış son yedekleme zamanı */
    val lastBackupFormatted: String
        get() {
            val ts = lastManualBackupAt ?: return "-"
            return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(ts))
        }
}
