package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.announcement.ANNOUNCEMENT_LOAD_USER_MESSAGE
import com.zgrcan.kalkan.data.announcement.AnnouncementRepository
import com.zgrcan.kalkan.data.announcement.logAnnouncementError
import com.zgrcan.kalkan.model.Announcement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun loadRecentAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnouncements = true, announcementsError = null) }
            announcementRepository.getRecentAnnouncements(limit = 5)
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            recentAnnouncements = items,
                            isLoadingAnnouncements = false,
                        )
                    }
                }
                .onFailure { error ->
                    logAnnouncementError("getRecentAnnouncements", error)
                    _uiState.update {
                        it.copy(
                            isLoadingAnnouncements = false,
                            announcementsError = ANNOUNCEMENT_LOAD_USER_MESSAGE,
                        )
                    }
                }
        }
    }
}

data class AdminDashboardUiState(
    val recentAnnouncements: List<Announcement> = emptyList(),
    val isLoadingAnnouncements: Boolean = false,
    val announcementsError: String? = null,
)
