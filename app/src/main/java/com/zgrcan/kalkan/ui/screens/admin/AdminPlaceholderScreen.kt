package com.zgrcan.kalkan.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted

@Composable
fun AdminPlaceholderScreen(
    route: String,
    onBackClick: () -> Unit,
) {
    val config = when (route) {
        "admin_emergency_alert" -> PlaceholderConfig(
            title = "Acil Uyarı Yayınla",
            description = "Afet, yangın, sel veya tahliye durumlarında tüm kullanıcılara yüksek öncelikli sesli ve görsel bildirim yayınlama paneli.",
            icon = Icons.Rounded.Warning,
            tint = KalkanRed,
            content = { EmergencyAlertMockContent() }
        )
        "admin_users" -> PlaceholderConfig(
            title = "Kullanıcılar",
            description = "Sistem kayıtlı tüm kullanıcıların, aktif SOS durumlarının ve aile gruplarının genel analizi.",
            icon = Icons.Rounded.Groups,
            tint = Color(0xFF10B981),
            content = { UsersMockContent() }
        )
        "admin_notifications" -> PlaceholderConfig(
            title = "Bildirim Merkezi",
            description = "Firestore ve FCM üzerinden gönderilen tüm sistem bildirimlerinin geçmiş kayıtları ve teslimat oranları.",
            icon = Icons.Rounded.Notifications,
            tint = KalkanBlue,
            content = { NotificationsMockContent() }
        )
        "admin_earthquake_monitor" -> PlaceholderConfig(
            title = "Deprem İzleme",
            description = "AFAD veri servisleri ile doğrudan entegrasyon kurarak depremleri saniyeler içinde tespit eden otomatik uyarı mekanizması.",
            icon = Icons.Rounded.Public,
            tint = Color(0xFFF59E0B),
            content = { EarthquakeMockContent() }
        )
        "admin_system_monitor" -> PlaceholderConfig(
            title = "Sistem İzleme",
            description = "Kalkan altyapısını oluşturan Firebase, Google Maps ve AFAD servislerinin anlık çalışma durumu ve yanıt süreleri.",
            icon = Icons.Rounded.MonitorHeart,
            tint = Color(0xFF6B7280),
            content = { SystemMockContent() }
        )
        else -> PlaceholderConfig(
            title = "Yönetim Modülü",
            description = "Bu modül üzerinde çalışmalar devam etmektedir.",
            icon = Icons.Rounded.Campaign,
            tint = KalkanBlue,
            content = { DefaultMockContent() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Geri dön",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Yönetim Paneli",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted,
                    )
                }
            }

            // Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(config.tint.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(config.icon, contentDescription = null, tint = config.tint, modifier = Modifier.size(26.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Modül Açıklaması",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = config.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = KalkanTextMuted,
                        )
                    }
                }
            }

            // Section Divider / Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arayüz Önizlemesi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .background(config.tint.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Geliştirme Aşamasında",
                        style = MaterialTheme.typography.labelSmall,
                        color = config.tint,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mock Content
            config.content()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class PlaceholderConfig(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    val content: @Composable () -> Unit,
)

@Composable
private fun EmergencyAlertMockContent() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Kritik Acil Uyarı Şablonu",
                style = MaterialTheme.typography.titleSmall,
                color = KalkanRed,
                fontWeight = FontWeight.Bold
            )
            
            // Mock form fields
            MockFieldPlaceholder(label = "Uyarı Başlığı", value = "Örn: Sel Riski ve Tahliye Uyarısı")
            MockFieldPlaceholder(label = "Etki Bölgesi / Şehir", value = "Örn: İzmir (Tüm İlçeler)")
            MockFieldPlaceholder(label = "Öncelik Derecesi", value = "Kritik (Kırmızı)")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KalkanRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, KalkanRed.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "⚠️ Bu alandan yayınlanacak uyarılar, kapsama alanındaki tüm kullanıcıların telefonlarında yüksek sesli alarm çalmasını tetikler. Güvenlik protokolleri nedeniyle test aşamasındadır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KalkanRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UsersMockContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MockStatCard(title = "Toplam Üye", value = "1,248", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            MockStatCard(title = "Aile Grubu", value = "412", color = KalkanBlue, modifier = Modifier.weight(1f))
        }
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Aktif Güvenlik Durumu",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "🟢 Güvendeyim Bildiren", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
                    Text(text = "1,248 Kullanıcı", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "🔴 Yardım İsteyen (SOS)", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
                    Text(text = "0 Kullanıcı", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = KalkanRed)
                }
            }
        }
    }
}

@Composable
private fun NotificationsMockContent() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Son Gönderilen Sistem Bildirimleri",
                style = MaterialTheme.typography.titleSmall,
                color = KalkanBlue,
                fontWeight = FontWeight.Bold
            )
            
            MockNotificationItem(
                title = "Planlı Sistem Bakımı Hakkında",
                time = "Dün, 23:00",
                stats = "99.8% İletim (1,246 Cihaz)",
                status = "Gönderildi"
            )
            MockNotificationItem(
                title = "Haftalık Deprem Özet Raporu",
                time = "28.05.2026, 18:15",
                stats = "100.0% İletim (1,248 Cihaz)",
                status = "Gönderildi"
            )
            MockNotificationItem(
                title = "Yeni Deprem Detay Modülü Aktif Edildi",
                time = "25.05.2026, 12:00",
                stats = "99.5% İletim (1,240 Cihaz)",
                status = "Gönderildi"
            )
        }
    }
}

@Composable
private fun EarthquakeMockContent() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AFAD Otomatik Entegrasyon Modülü",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "AFAD API Servis Durumu", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
                Text(text = "Test Aşamasında", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Otomatik SOS Tetikleyici", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
                Text(text = "Pasif (Manuel)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = KalkanTextMuted)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Minimum Uyarı Eşiği", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
                Text(text = "M ≥ 5.0 (Rihter)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SystemMockContent() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sunucu & API Servis Durumları",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            MockServiceItem(name = "Firebase Authentication", latency = "12ms")
            MockServiceItem(name = "Cloud Firestore Database", latency = "18ms")
            MockServiceItem(name = "Firebase Cloud Messaging (FCM)", latency = "24ms")
            MockServiceItem(name = "Google Maps Platform SDK", latency = "15ms")
        }
    }
}

@Composable
private fun DefaultMockContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Önizleme bulunmuyor", style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
    }
}

@Composable
private fun MockFieldPlaceholder(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = KalkanTextMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MockStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = KalkanTextMuted)
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockNotificationItem(
    title: String,
    time: String,
    stats: String,
    status: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stats, style = MaterialTheme.typography.labelMedium, color = KalkanTextMuted)
            Text(text = time, style = MaterialTheme.typography.labelMedium, color = KalkanTextMuted)
        }
    }
}

@Composable
private fun MockServiceItem(name: String, latency: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Text(text = "Gecikme Süresi: $latency", style = MaterialTheme.typography.labelSmall, color = KalkanTextMuted)
        }
        Box(
            modifier = Modifier
                .background(Color(0xFF10B981).copy(alpha = 0.12f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = "Aktif", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
        }
    }
}
