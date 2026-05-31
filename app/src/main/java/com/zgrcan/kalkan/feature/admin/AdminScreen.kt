package com.zgrcan.kalkan.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.components.KalkanCard
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted

@Composable
fun AdminScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Super Admin", style = MaterialTheme.typography.headlineSmall)
        listOf(
            "Duyuru Gonder",
            "Acil Uyari Gonder",
            "Kullanici Istatistikleri",
            "Bildirim Yonetimi",
            "Sensor Verileri",
            "Sistem Ayarlari",
        ).forEach { title ->
            KalkanCard {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text("Yetkili hesaplar icin aktif olacak.", color = KalkanTextMuted)
            }
        }
    }
}
