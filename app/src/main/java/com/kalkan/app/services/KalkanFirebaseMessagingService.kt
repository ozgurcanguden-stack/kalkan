package com.kalkan.app.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kalkan.app.core.notification.NotificationHelper
import com.kalkan.app.data.fcm.FcmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KalkanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var fcmRepository: FcmRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            fcmRepository.saveTokenForCurrentUser(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Kalkan"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Test bildirimi başarıyla alındı."

        NotificationHelper.showKalkanNotification(
            context = this,
            title = title,
            body = body,
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
