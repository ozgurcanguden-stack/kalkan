package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.auth.AuthRepository
import com.zgrcan.kalkan.data.admin.AdminStatsRepository
import com.zgrcan.kalkan.data.alert.EMERGENCY_ALERT_DELETE_FAILURE_MESSAGE
import com.zgrcan.kalkan.data.alert.EMERGENCY_ALERT_DELETE_SUCCESS_MESSAGE
import com.zgrcan.kalkan.data.alert.EmergencyAlertRepository
import com.zgrcan.kalkan.data.alert.logEmergencyAlertError
import com.zgrcan.kalkan.data.announcement.ANNOUNCEMENT_DELETE_FAILURE_MESSAGE
import com.zgrcan.kalkan.data.announcement.ANNOUNCEMENT_DELETE_SUCCESS_MESSAGE
import com.zgrcan.kalkan.data.announcement.AnnouncementRepository
import com.zgrcan.kalkan.data.announcement.logAnnouncementError
import com.zgrcan.kalkan.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "Duyuru", "Acil Uyarı", "Deprem Bildirimi"
    val timestamp: Long
)

data class AdminNotificationsState(
    val isLoading: Boolean = false,
    val items: List<AdminNotificationItem> = emptyList(),
    val errorMessage: String? = null,
    val isDeletingItem: Boolean = false,
    val snackbarMessage: String? = null,
    val isSnackbarError: Boolean = false,
)

@HiltViewModel
class AdminNotificationsViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val alertRepository: EmergencyAlertRepository,
    private val statsRepository: AdminStatsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotificationsState())
    val uiState: StateFlow<AdminNotificationsState> = _uiState.asStateFlow()

    val hasAdminAccess: StateFlow<Boolean> = authRepository.currentFirebaseUser
        .flatMapLatest { authUser ->
            if (authUser == null) {
                flowOf(false)
            } else {
                userRepository.observeUser(authUser.uid).combine(flowOf(authUser)) { appUser, _ ->
                    appUser?.isAdmin == true
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val allItems = mutableListOf<AdminNotificationItem>()

            // Load Announcements
            announcementRepository.getRecentAnnouncements(10).onSuccess { list ->
                allItems.addAll(list.map {
                    AdminNotificationItem(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        type = "Duyuru",
                        timestamp = it.createdAt
                    )
                })
            }

            // Load Emergency Alerts
            alertRepository.getRecentAlerts(10).onSuccess { list ->
                allItems.addAll(list.map {
                    AdminNotificationItem(
                        id = it["id"] as? String ?: "",
                        title = it["title"] as? String ?: "",
                        message = it["body"] as? String ?: "",
                        type = "Acil Uyarı",
                        timestamp = it["createdAt"] as? Long ?: 0L
                    )
                })
            }

            // Load Notified Earthquakes
            statsRepository.getRecentNotifiedEarthquakes(10).onSuccess { list ->
                allItems.addAll(list.map {
                    // We expect earthquake_events to have place, magnitude, notificationSentAt
                    val mag = it["magnitude"]?.toString() ?: ""
                    val place = it["place"] as? String ?: ""
                    val timeObj = it["notificationSentAt"] as? com.google.firebase.Timestamp
                    val timestamp = timeObj?.toDate()?.time ?: 0L
                    
                    AdminNotificationItem(
                        id = it["id"] as? String ?: "",
                        title = "Deprem Bildirimi ($mag)",
                        message = place,
                        type = "Deprem Bildirimi",
                        timestamp = timestamp
                    )
                })
            }

            // Sort by descending timestamp
            allItems.sortByDescending { it.timestamp }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    items = allItems
                )
            }
        }
    }

    fun deleteAnnouncement(announcementId: String, canDelete: Boolean) {
        if (!canDelete || announcementId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingItem = true, snackbarMessage = null) }
            announcementRepository.deleteAnnouncement(announcementId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeletingItem = false,
                            snackbarMessage = ANNOUNCEMENT_DELETE_SUCCESS_MESSAGE,
                            isSnackbarError = false,
                        )
                    }
                    loadNotifications()
                }
                .onFailure { error ->
                    logAnnouncementError("deleteAnnouncement", error)
                    _uiState.update {
                        it.copy(
                            isDeletingItem = false,
                            snackbarMessage = ANNOUNCEMENT_DELETE_FAILURE_MESSAGE,
                            isSnackbarError = true,
                        )
                    }
                }
        }
    }

    fun deleteEmergencyAlert(alertId: String, canDelete: Boolean) {
        if (!canDelete || alertId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingItem = true, snackbarMessage = null) }
            alertRepository.deleteAlert(alertId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeletingItem = false,
                            snackbarMessage = EMERGENCY_ALERT_DELETE_SUCCESS_MESSAGE,
                            isSnackbarError = false,
                        )
                    }
                    loadNotifications()
                }
                .onFailure { error ->
                    logEmergencyAlertError("deleteAlert", error)
                    _uiState.update {
                        it.copy(
                            isDeletingItem = false,
                            snackbarMessage = EMERGENCY_ALERT_DELETE_FAILURE_MESSAGE,
                            isSnackbarError = true,
                        )
                    }
                }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
