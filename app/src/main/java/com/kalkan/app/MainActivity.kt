package com.kalkan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import com.kalkan.app.core.design.theme.KalkanTheme
import com.kalkan.app.core.navigation.KalkanNavHost
import com.kalkan.app.ui.components.AppTopNotificationHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KalkanTheme {
                Box {
                    KalkanNavHost()
                    AppTopNotificationHost()
                }
            }
        }
    }
}
