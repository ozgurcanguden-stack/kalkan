package com.zgrcan.kalkan.ui.screens.admin

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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.model.Announcement
import com.zgrcan.kalkan.model.AnnouncementPriority
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    hasAdminAccess: Boolean,
    recentAnnouncements: List<Announcement>,
    isLoadingAnnouncements: Boolean,
    announcementsError: String?,
    onBackClick: () -> Unit,
    onFeatureClick: (String) -> Unit,
    onRefreshAnnouncements: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasAdminAccess) {
        if (hasAdminAccess) {
            onRefreshAnnouncements()
        }
    }

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
                RecentAnnouncementsSection(
                    announcements = recentAnnouncements,
                    isLoading = isLoadingAnnouncements,
                    errorMessage = announcementsError,
                )
                adminFeatures.filter { it.visible }.forEach { feature ->
                    AdminFeatureCard(
                        feature = feature,
                        onClick = {
                            onFeatureClick(feature.route)
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
private fun RecentAnnouncementsSection(
    announcements: List<Announcement>,
    isLoading: Boolean,
    errorMessage: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Son Duyurular",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = KalkanBlue)
                }
            }
            errorMessage != null -> {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium, color = KalkanRed)
            }
            announcements.isEmpty() -> {
                Text(
                    text = "Henuz duyuru olusturulmadi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted,
                )
            }
            else -> {
                announcements.forEach { announcement ->
                    RecentAnnouncementCard(announcement = announcement)
                }
            }
        }
    }
}

@Composable
private fun RecentAnnouncementCard(announcement: Announcement) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${announcement.priority.label} · ${announcement.targetAudience.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor(announcement.priority),
                )
                Text(
                    text = formatAnnouncementDate(announcement.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = KalkanTextMuted,
                )
            }
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
                    text = "Duyuru olusturma aktif; diger alanlar iskelet olarak hazirlandi.",
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
    val route: String,
    val visible: Boolean = true,
)

private val adminFeatures = listOf(
    AdminFeature(
        title = "Duyuru Oluştur",
        description = "Sistem duyuruları, bakım bilgilendirmeleri ve genel açıklamalar yayınlayın.",
        icon = Icons.Rounded.Campaign,
        tint = KalkanBlue,
        route = "create_announcement",
    ),
    AdminFeature(
        title = "Acil Uyarı Yayınla",
        description = "Yangın, sel riski, tahliye çağrıları ve kritik acil durum bilgilendirmeleri gönderin.",
        icon = Icons.Rounded.Warning,
        tint = KalkanRed,
        route = "admin_emergency_alert",
    ),
    AdminFeature(
        title = "Kullanıcılar",
        description = "Kullanıcı ve aile grubu istatistiklerini görüntüleyin.",
        icon = Icons.Rounded.Groups,
        tint = Color(0xFF10B981),
        route = "admin_users",
    ),
    AdminFeature(
        title = "Bildirim Merkezi",
        description = "Geçmiş gönderilen bildirim kayıtlarını listeleyin.",
        icon = Icons.Rounded.Notifications,
        tint = KalkanBlue,
        route = "admin_notifications",
    ),
    AdminFeature(
        title = "Deprem İzleme",
        description = "Gelecekte kurulacak AFAD otomatik deprem izleme sisteminin yönetim ekranı.",
        icon = Icons.Rounded.Public,
        tint = Color(0xFFF59E0B),
        route = "admin_earthquake_monitor",
    ),
    AdminFeature(
        title = "Sistem İzleme",
        description = "Teknik servislerin durumunu görüntüleyin.",
        icon = Icons.Rounded.MonitorHeart,
        tint = Color(0xFF6B7280),
        route = "admin_system_monitor",
    ),
    AdminFeature(
        title = "Sensör Ağı",
        description = "Ileride eklenecek sarsinti algilama verilerini goruntuleyin.",
        icon = Icons.Rounded.Memory,
        tint = Color(0xFF8B5CF6),
        route = "sensor_network",
        visible = false,
    ),
)

private fun priorityColor(priority: AnnouncementPriority): Color = when (priority) {
    AnnouncementPriority.NORMAL -> KalkanBlue
    AnnouncementPriority.IMPORTANT -> Color(0xFFF59E0B)
    AnnouncementPriority.URGENT -> KalkanRed
}

private fun formatAnnouncementDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(epochMillis))
}
