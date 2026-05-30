package com.kalkan.app.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    hasAdminAccess: Boolean,
    onBackClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (!hasAdminAccess) {
            UnauthorizedAdminContent(onBackClick = onBackClick)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AdminTopBar(onBackClick = onBackClick)
                AdminHeaderCard()
                adminFeatures.forEach { feature ->
                    AdminFeatureCard(
                        feature = feature,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("${feature.title} hazirlaniyor.")
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun UnauthorizedAdminContent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(KalkanRed.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Bu alana erisim yetkiniz yok.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Admin paneli yalnizca super_admin rolune sahip kullanicilar icindir.",
            style = MaterialTheme.typography.bodyMedium,
            color = KalkanTextMuted,
        )
        Spacer(modifier = Modifier.height(18.dp))
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri don", tint = KalkanBlue)
        }
    }
}

@Composable
private fun AdminTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri don", tint = MaterialTheme.colorScheme.primary)
        }
        Column {
            Text(
                text = "Admin Paneli",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Kalkan yonetim merkezi",
                style = MaterialTheme.typography.bodyLarge,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun AdminHeaderCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MonitorHeart, contentDescription = null, tint = Color.White)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Yonetim modulleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Bu ekrandaki alanlar su an iskelet olarak hazirlandi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun AdminFeatureCard(
    feature: AdminFeature,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(feature.tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(feature.icon, contentDescription = null, tint = feature.tint)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(feature.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(feature.description, style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = KalkanTextMuted)
        }
    }
}

private data class AdminFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
)

private val adminFeatures = listOf(
    AdminFeature(
        title = "Duyuru Gonder",
        description = "Tum kullanicilara veya belirli bolgelere duyuru hazirlayin.",
        icon = Icons.Rounded.Campaign,
        tint = KalkanBlue,
    ),
    AdminFeature(
        title = "Acil Uyari Gonder",
        description = "Afet ve acil durumlar icin kritik uyari hazirlayin.",
        icon = Icons.Rounded.Warning,
        tint = KalkanRed,
    ),
    AdminFeature(
        title = "Kullanici Istatistikleri",
        description = "Toplam kullanici, aktif kullanici ve rol dagilimini goruntuleyin.",
        icon = Icons.Rounded.Analytics,
        tint = Color(0xFF22C55E),
    ),
    AdminFeature(
        title = "Bildirim Yonetimi",
        description = "Gonderilecek push bildirimlerini yonetin.",
        icon = Icons.Rounded.Notifications,
        tint = KalkanBlue,
    ),
    AdminFeature(
        title = "Sistem Izleme",
        description = "Uygulama servis durumlarini ve hata kayitlarini takip edin.",
        icon = Icons.Rounded.MonitorHeart,
        tint = Color(0xFF64748B),
    ),
    AdminFeature(
        title = "Sensor Agi",
        description = "Ileride eklenecek sarsinti algilama verilerini goruntuleyin.",
        icon = Icons.Rounded.Memory,
        tint = Color(0xFF8B5CF6),
    ),
)
