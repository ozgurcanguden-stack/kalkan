package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.announcement.AnnouncementRepository
import com.kalkan.app.model.Announcement
import com.kalkan.app.model.AnnouncementPriority
import com.kalkan.app.model.AnnouncementStatus
import com.kalkan.app.model.AnnouncementTargetAudience
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAnnouncementViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateAnnouncementUiState())
    val uiState: StateFlow<CreateAnnouncementUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null, errorMessage = null) }
    }

    fun onMessageChange(value: String) {
        _uiState.update { it.copy(message = value, messageError = null, errorMessage = null) }
    }

    fun onTargetAudienceChange(value: AnnouncementTargetAudience) {
        _uiState.update { it.copy(targetAudience = value, errorMessage = null) }
    }

    fun onPriorityChange(value: AnnouncementPriority) {
        _uiState.update { it.copy(priority = value, errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun createAnnouncement(createdByUid: String, createdByName: String) {
        val validation = validate(_uiState.value)
        if (validation != null) {
            _uiState.update {
                it.copy(
                    titleError = validation.titleError,
                    messageError = validation.messageError,
                    errorMessage = validation.generalError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            val draft = Announcement(
                title = _uiState.value.title.trim(),
                message = _uiState.value.message.trim(),
                targetAudience = _uiState.value.targetAudience,
                priority = _uiState.value.priority,
                createdByUid = createdByUid,
                createdByName = createdByName,
                status = AnnouncementStatus.PUBLISHED,
            )
            announcementRepository.createAnnouncement(draft)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Duyuru basariyla kaydedildi.",
                            title = "",
                            message = "",
                            targetAudience = AnnouncementTargetAudience.ALL,
                            priority = AnnouncementPriority.NORMAL,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Duyuru kaydedilemedi. Lutfen tekrar deneyin.",
                        )
                    }
                }
        }
    }

    private fun validate(state: CreateAnnouncementUiState): ValidationErrors? {
        val title = state.title.trim()
        val message = state.message.trim()
        var titleError: String? = null
        var messageError: String? = null

        when {
            title.isEmpty() -> titleError = "Baslik bos olamaz."
            title.length < 3 -> titleError = "Baslik en az 3 karakter olmalidir."
        }
        when {
            message.isEmpty() -> messageError = "Mesaj bos olamaz."
            message.length < 10 -> messageError = "Mesaj en az 10 karakter olmalidir."
        }

        if (titleError != null || messageError != null) {
            return ValidationErrors(titleError = titleError, messageError = messageError)
        }
        return null
    }

    private data class ValidationErrors(
        val titleError: String? = null,
        val messageError: String? = null,
        val generalError: String? = null,
    )
}

data class CreateAnnouncementUiState(
    val title: String = "",
    val message: String = "",
    val targetAudience: AnnouncementTargetAudience = AnnouncementTargetAudience.ALL,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val messageError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
