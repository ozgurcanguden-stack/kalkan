package com.kalkan.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kalkan.app.core.design.theme.KalkanTheme
import com.kalkan.app.core.navigation.KalkanNavHost
import com.kalkan.app.core.notification.NotificationNavigation
import com.kalkan.app.core.notification.NotificationNavigationTarget
import com.kalkan.app.ui.components.AppTopNotificationHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationNavigationTarget by mutableStateOf<NotificationNavigationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationNavigationTarget = NotificationNavigation.fromIntent(intent)
        setContent {
            KalkanTheme {
                Box {
                    KalkanNavHost(
                        notificationNavigationTarget = notificationNavigationTarget,
                        onNotificationNavigationHandled = {
                            notificationNavigationTarget = null
                        },
                    )
                    AppTopNotificationHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationNavigationTarget = NotificationNavigation.fromIntent(intent)
    }
}
