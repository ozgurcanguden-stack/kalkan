package com.zgrcan.kalkan.core.notification

import android.content.Intent

sealed interface NotificationNavigationTarget {
    data object Home : NotificationNavigationTarget
    data object Family : NotificationNavigationTarget
    data class AnnouncementDetail(val announcementId: String) : NotificationNavigationTarget
}

object NotificationNavigation {
    const val ACTION_NOTIFICATION_CLICK = "com.zgrcan.kalkan.NOTIFICATION_CLICK"

    fun fromIntent(intent: Intent?): NotificationNavigationTarget? {
        if (intent?.action != ACTION_NOTIFICATION_CLICK) return null
        return fromData(intent.extras?.keySet()?.associateWith { intent.extras?.getString(it) }.orEmpty())
    }

    fun fromData(data: Map<String, String?>): NotificationNavigationTarget {
        return when (data["type"]) {
            "announcement" -> {
                val announcementId = data["announcementId"].orEmpty().trim()
                if (announcementId.isValidDocumentId()) {
                    NotificationNavigationTarget.AnnouncementDetail(announcementId)
                } else {
                    NotificationNavigationTarget.Home
                }
            }
            "family_safety_alert" -> {
                val statusType = data["statusType"].orEmpty().trim()
                val senderUid = (data["senderUid"] ?: data["sourceUid"]).orEmpty().trim()
                if ((statusType == "sos" || statusType == "need_help") && senderUid.isNotBlank()) {
                    NotificationNavigationTarget.Family
                } else {
                    NotificationNavigationTarget.Home
                }
            }
            else -> NotificationNavigationTarget.Home
        }
    }

    private fun String.isValidDocumentId(): Boolean =
        isNotBlank() && length <= 1_500 && !contains('/')
}
