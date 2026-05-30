package com.kalkan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementDetailViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val announcementId: String = checkNotNull(savedStateHandle["announcementId"]) {
        "announcementId gerekli."
    }
    private val _uiState = MutableStateFlow<AnnouncementDetailUiState>(AnnouncementDetailUiState.Loading)
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    fun loadAnnouncement(isGuest: Boolean, isRegistered: Boolean) {
        viewModelScope.launch {
            _uiState.value = AnnouncementDetailUiState.Loading
            announcementRepository.getAnnouncementById(
                id = announcementId,
                isGuest = isGuest,
                isRegistered = isRegistered,
            )
                .onSuccess { announcement ->
                    _uiState.value = AnnouncementDetailUiState.Success(announcement)
                }
                .onFailure { error ->
                    logAnnouncementError("getAnnouncementById", error)
                    val userMessage = when (error) {
                        is IllegalArgumentException, is IllegalStateException -> error.message
                            ?: ANNOUNCEMENT_LOAD_USER_MESSAGE
                        else -> ANNOUNCEMENT_LOAD_USER_MESSAGE
                    }
                    _uiState.value = AnnouncementDetailUiState.Error(userMessage)
                }
        }
    }
}

sealed interface AnnouncementDetailUiState {
    data object Loading : AnnouncementDetailUiState

    data class Success(val announcement: Announcement) : AnnouncementDetailUiState

    data class Error(val message: String) : AnnouncementDetailUiState
}
