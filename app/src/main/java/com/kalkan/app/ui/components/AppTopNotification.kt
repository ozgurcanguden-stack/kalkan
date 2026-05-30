package com.kalkan.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

object AppTopNotificationCenter {
    private val notifications = MutableSharedFlow<AppTopNotification>(extraBufferCapacity = 16)
    private val nextId = AtomicLong()

    fun showSuccess(message: String) {
        notifications.tryEmit(
            AppTopNotification(
                id = nextId.incrementAndGet(),
                message = message,
            )
        )
    }

    internal fun notificationFlow() = notifications
}

internal data class AppTopNotification(
    val id: Long,
    val message: String,
)

@Composable
fun AppTopNotificationHost() {
    val visibleNotifications = remember { mutableStateListOf<AppTopNotification>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AppTopNotificationCenter.notificationFlow().collect { notification ->
            visibleNotifications.add(notification)
            coroutineScope.launch {
                delay(3_000)
                visibleNotifications.removeAll { it.id == notification.id }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visibleNotifications.forEach { notification ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
            ) {
                Surface(
                    color = Color(0xFF15803D),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                        )
                        Text(
                            text = notification.message,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
