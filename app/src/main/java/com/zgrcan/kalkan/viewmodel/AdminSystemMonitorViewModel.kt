package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AdminSystemMonitorState(
    val isLoading: Boolean = false,
    val isEarthquakeMonitorEnabled: Boolean = false,
    val lastCheckedAt: Long? = null,
    val lastProcessedEarthquakeId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminSystemMonitorViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSystemMonitorState())
    val uiState: StateFlow<AdminSystemMonitorState> = _uiState.asStateFlow()

    init {
        loadSystemStatus()
    }

    fun loadSystemStatus() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        firestore.collection("system_settings").document("earthquake_monitor").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val enabled = snapshot.getBoolean("enabled") ?: true
                    val lastChecked = snapshot.getTimestamp("lastCheckedAt")?.toDate()?.time
                    val lastId = snapshot.getString("lastProcessedEarthquakeId")
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEarthquakeMonitorEnabled = enabled,
                            lastCheckedAt = lastChecked,
                            lastProcessedEarthquakeId = lastId
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Sistem ayarları henüz oluşturulmamış."
                        )
                    }
                }
            }
            .addOnFailureListener {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Sistem durumu okunamadı: ${it.localizedMessage}"
                    )
                }
            }
    }
}
