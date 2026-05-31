package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.admin.AdminStatsRepository
import com.zgrcan.kalkan.data.alert.EmergencyAlertRepository
import com.zgrcan.kalkan.data.announcement.AnnouncementRepository
import com.zgrcan.kalkan.model.Announcement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val errorMessage: String? = null
)

@HiltViewModel
class AdminNotificationsViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val alertRepository: EmergencyAlertRepository,
    private val statsRepository: AdminStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotificationsState())
    val uiState: StateFlow<AdminNotificationsState> = _uiState.asStateFlow()

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
}
