package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.admin.AdminStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUsersState(
    val isLoading: Boolean = false,
    val totalUsers: String = "-",
    val earthquakeEnabledUsers: String = "-",
    val totalFamilies: String = "-",
    val activeSosCount: String = "-",
    val errorMessage: String? = null
)

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val adminStatsRepository: AdminStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersState())
    val uiState: StateFlow<AdminUsersState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Loading concurrently would be ideal, but for simplicity we run sequentially or just catch individual errors
            val totalUsersResult = adminStatsRepository.getTotalUsersCount()
            val earthquakeEnabledResult = adminStatsRepository.getEarthquakeEnabledUsersCount()
            val totalFamiliesResult = adminStatsRepository.getTotalFamilyGroupsCount()
            val activeSosResult = adminStatsRepository.getActiveSosCount()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    totalUsers = totalUsersResult.getOrNull()?.toString() ?: "N/A",
                    earthquakeEnabledUsers = earthquakeEnabledResult.getOrNull()?.toString() ?: "N/A",
                    totalFamilies = totalFamiliesResult.getOrNull()?.toString() ?: "N/A",
                    activeSosCount = activeSosResult.getOrNull()?.toString() ?: "N/A"
                )
            }
        }
    }
}
