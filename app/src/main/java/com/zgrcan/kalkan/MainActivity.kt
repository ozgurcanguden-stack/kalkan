package com.zgrcan.kalkan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zgrcan.kalkan.core.design.theme.KalkanTheme
import com.zgrcan.kalkan.core.navigation.KalkanNavHost
import com.zgrcan.kalkan.core.notification.NotificationNavigation
import com.zgrcan.kalkan.core.notification.NotificationNavigationTarget
import com.zgrcan.kalkan.ui.components.AppTopNotificationHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationNavigationTarget by mutableStateOf<NotificationNavigationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.argb(0xFF, 0xF8, 0xFA, 0xFC),
                darkScrim = Color.argb(0xFF, 0x0F, 0x17, 0x2A),
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.argb(0xFF, 0xFF, 0xFF, 0xFF),
                darkScrim = Color.argb(0xFF, 0x0F, 0x17, 0x2A),
            ),
        )
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
