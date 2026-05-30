package com.kalkan.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.components.KalkanButton
import com.kalkan.app.core.design.components.KalkanCard
import com.kalkan.app.core.design.theme.KalkanTextMuted

@Composable
fun ProfileScreen(onAdminClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Profil", style = MaterialTheme.typography.headlineSmall)
        KalkanCard {
            Text("Misafir Kullanici", style = MaterialTheme.typography.titleLarge)
            Text("Google ile giris, bildirim tercihleri ve guvenlik ayarlari burada yonetilecek.", color = KalkanTextMuted)
        }
        KalkanButton(text = "Admin Paneli", onClick = onAdminClick)
    }
}
