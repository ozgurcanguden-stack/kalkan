package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.announcement.ANNOUNCEMENT_LOAD_USER_MESSAGE
import com.kalkan.app.data.announcement.AnnouncementRepository
import com.kalkan.app.data.announcement.logAnnouncementError
import com.kalkan.app.model.Announcement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnnouncementsUiState>(AnnouncementsUiState.Loading)
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()

    fun loadAnnouncements(isGuest: Boolean, isRegistered: Boolean) {
        viewModelScope.launch {
            _uiState.value = AnnouncementsUiState.Loading
            announcementRepository.getPublishedAnnouncementsForUser(
                isGuest = isGuest,
                isRegistered = isRegistered,
            ).onSuccess { announcements ->
                _uiState.value = if (announcements.isEmpty()) {
                    AnnouncementsUiState.Empty
                } else {
                    AnnouncementsUiState.Success(announcements)
                }
            }.onFailure { error ->
                logAnnouncementError("getPublishedAnnouncementsForUser", error)
                _uiState.value = AnnouncementsUiState.Error(ANNOUNCEMENT_LOAD_USER_MESSAGE)
            }
        }
    }
}

sealed interface AnnouncementsUiState {
    data object Loading : AnnouncementsUiState

    data object Empty : AnnouncementsUiState

    data class Success(val announcements: List<Announcement>) : AnnouncementsUiState

    data class Error(val message: String) : AnnouncementsUiState
}
