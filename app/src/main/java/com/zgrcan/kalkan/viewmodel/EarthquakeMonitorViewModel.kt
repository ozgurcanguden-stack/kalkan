package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakeMonitorViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarthquakeMonitorUiState())
    val uiState: StateFlow<EarthquakeMonitorUiState> = _uiState.asStateFlow()

    private val settingsRef = firestore.collection("system_settings").document("earthquake_monitor")

    init {
        loadSettings()
    }

    fun loadSettings() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        settingsRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val enabled = snapshot.getBoolean("enabled") ?: true
                    val interval = snapshot.getLong("intervalMinutes")?.toInt() ?: 5
                    val minMag = snapshot.getDouble("minSystemMagnitude") ?: 2.0
                    val lastChecked = snapshot.getTimestamp("lastCheckedAt")?.toDate()?.time
                    val lastId = snapshot.getString("lastProcessedEarthquakeId")

                    _uiState.update {
                        it.copy(
                            enabled = enabled,
                            intervalMinutes = interval,
                            minSystemMagnitude = minMag,
                            lastCheckedAt = lastChecked,
                            lastProcessedEarthquakeId = lastId,
                            isLoading = false
                        )
                    }
                } else {
                    // Create defaults
                    saveSettings(enabled = true, intervalMinutes = 5, minSystemMagnitude = 2.0)
                }
            }
            .addOnFailureListener { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = toFriendlyMessage(err, "Sistem ayarları yüklenemedi. Lütfen tekrar deneyin.")
                    )
                }
            }
    }

    fun saveSettings(enabled: Boolean, intervalMinutes: Int, minSystemMagnitude: Double) {
        _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
        val data = mapOf(
            "enabled" to enabled,
            "intervalMinutes" to intervalMinutes,
            "minSystemMagnitude" to minSystemMagnitude
        )
        settingsRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        enabled = enabled,
                        intervalMinutes = intervalMinutes,
                        minSystemMagnitude = minSystemMagnitude,
                        successMessage = "Deprem izleme ayarları başarıyla güncellendi."
                    )
                }
            }
            .addOnFailureListener { err ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = toFriendlyMessage(err, "Ayarlar kaydedilemedi. Lütfen tekrar deneyin.")
                    )
                }
            }
    }

    private fun toFriendlyMessage(err: Throwable, defaultMessage: String): String {
        val msg = err.localizedMessage.orEmpty().lowercase()
        return if (msg.contains("permission denied") || msg.contains("permission_denied") || msg.contains("yetki")) {
            "Sistem ayarlarına erişim veya değiştirme yetkiniz bulunmamaktadır."
        } else {
            defaultMessage
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

data class EarthquakeMonitorUiState(
    val enabled: Boolean = true,
    val intervalMinutes: Int = 5,
    val minSystemMagnitude: Double = 2.0,
    val lastCheckedAt: Long? = null,
    val lastProcessedEarthquakeId: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
