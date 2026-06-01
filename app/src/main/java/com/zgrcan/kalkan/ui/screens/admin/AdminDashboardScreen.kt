package com.zgrcan.kalkan.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanNavy
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.model.Announcement
import com.zgrcan.kalkan.model.AnnouncementPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Public Composable
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    hasAdminAccess: Boolean,
    recentAnnouncements: List<Announcement>,
    isLoadingAnnouncements: Boolean,
    isDeletingAnnouncement: Boolean,
    announcementsError: String?,
    snackbarMessage: String?,
    isSnackbarError: Boolean,
    onBackClick: () -> Unit,
    onFeatureClick: (String) -> Unit,
    onRefreshAnnouncements: () -> Unit,
    onDeleteAnnouncement: (String) -> Unit,
    onDismissSnackbar: () -> Unit,
) {
    var announcementToDeleteId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(hasAdminAccess) {
        if (hasAdminAccess) onRefreshAnnouncements()
    }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        if (isSnackbarError) {
            snackbarHostState.showSnackbar(message)
        } else {
            AppTopNotificationCenter.showSuccess(message)
        }
        onDismissSnackbar()
    }

    val isDark = MaterialTheme.colorScheme.background == KalkanNavy ||
        MaterialTheme.colorScheme.background.luminance() < 0.2f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AdminTopBar(onBackClick = onBackClick, onRefresh = onRefreshAnnouncements)
        },
    ) { innerPadding ->
        if (!hasAdminAccess) {
            UnauthorizedAdminContent(onBackClick = onBackClick)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ── Hero header ──────────────────────────────────────────────
                item { AdminHeroCard(isDark = isDark) }

                // ── Stat chips ───────────────────────────────────────────────
                item {
                    StatChipsRow(
                        announcementCount = recentAnnouncements.size,
                        isLoading = isLoadingAnnouncements,
                    )
                }

                // ── Section header ───────────────────────────────────────────
                item {
                    SectionHeader(
                        title = "Yönetim Modülleri",
                        subtitle = "Aşağıdaki modüllerden birini seçin",
                    )
                }

                // ── Feature grid (2 columns via LazyRow pairs) ───────────────
                val visibleFeatures = adminFeatures.filter { it.visible }
                val pairs = visibleFeatures.chunked(2)
                items(pairs) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pair.forEach { feature ->
                            AdminFeatureTile(
                                feature = feature,
                                onClick = { onFeatureClick(feature.route) },
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                            )
                        }
                        // If odd number of items, fill remaining space
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // ── Recent Announcements ─────────────────────────────────────
                item {
                    SectionHeader(
                        title = "Son Duyurular",
                        subtitle = "Son 5 duyuru",
                    )
                }

                item {
                    AnnouncementsSection(
                        announcements = recentAnnouncements,
                        isLoading = isLoadingAnnouncements,
                        errorMessage = announcementsError,
                        isDark = isDark,
                        canDeleteAnnouncements = hasAdminAccess,
                        isDeletingAnnouncement = isDeletingAnnouncement,
                        onDeleteAnnouncement = { announcementToDeleteId = it.id },
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    announcementToDeleteId?.let { announcementId ->
        AlertDialog(
            onDismissRequest = { announcementToDeleteId = null },
            title = {
                Text(
                    text = "Duyuru silinsin mi?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Bu duyuru uygulamadan kaldırılacak.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAnnouncement(announcementId)
                        announcementToDeleteId = null
                    },
                    enabled = !isDeletingAnnouncement,
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed),
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementToDeleteId = null }) {
                    Text("İptal")
                }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(onBackClick: () -> Unit, onRefresh: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Admin Paneli",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "KALKAN Yönetim Merkezi",
                    style = MaterialTheme.typography.bodySmall,
                    color = KalkanTextMuted,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Geri dön",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "Yenile",
                    tint = KalkanBlue,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminHeroCard(isDark: Boolean) {
    val gradientColors = if (isDark) {
        listOf(Color(0xFF1E3A5F), Color(0xFF0F172A), Color(0xFF1A1F3A))
    } else {
        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF1D4ED8))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(24.dp),
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.BottomStart)
                .background(Color.White.copy(alpha = 0.07f), CircleShape),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Süper Admin",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp,
                )
                Text(
                    text = "Yönetim\nMerkezi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 30.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF4ADE80), CircleShape),
                    )
                    Text(
                        text = "Tüm sistemler aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat chips
// ─────────────────────────────────────────────────────────────────────────────

private data class StatChip(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color,
)

@Composable
private fun StatChipsRow(announcementCount: Int, isLoading: Boolean) {
    val chips = listOf(
        StatChip("Duyurular", if (isLoading) "…" else announcementCount.toString(), Icons.Rounded.Campaign, KalkanBlue),
        StatChip("Sistem", "Aktif", Icons.Rounded.MonitorHeart, Color(0xFF10B981)),
        StatChip("Deprem İzleme", "Açık", Icons.Rounded.Public, Color(0xFFF59E0B)),
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(chips) { chip ->
            StatChipCard(chip = chip)
        }
    }
}

@Composable
private fun StatChipCard(chip: StatChip) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = chip.tint.copy(alpha = 0.10f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(1.dp, chip.tint.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(chip.icon, contentDescription = null, tint = chip.tint, modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = chip.value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = chip.tint,
                )
                Text(
                    text = chip.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = KalkanTextMuted,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = KalkanTextMuted,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Feature Tile (2-column grid card)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminFeatureTile(
    feature: AdminFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "tile_scale",
    )

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else KalkanBorder.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                onClick = onClick,
                onClickLabel = feature.title,
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(feature.tint.copy(alpha = if (isDark) 0.18f else 0.10f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = feature.tint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = KalkanTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Arrow row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(feature.tint.copy(alpha = if (isDark) 0.18f else 0.10f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = feature.tint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Announcements section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnnouncementsSection(
    announcements: List<Announcement>,
    isLoading: Boolean,
    errorMessage: String?,
    isDark: Boolean,
    canDeleteAnnouncements: Boolean,
    isDeletingAnnouncement: Boolean,
    onDeleteAnnouncement: (Announcement) -> Unit,
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = KalkanBlue,
                    strokeWidth = 2.5.dp,
                )
            }
        }
        errorMessage != null -> {
            ErrorCard(message = errorMessage, isDark = isDark)
        }
        announcements.isEmpty() -> {
            EmptyAnnouncementsCard(isDark = isDark)
        }
        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                announcements.forEachIndexed { index, announcement ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ),
                    ) {
                        AnnouncementListItem(
                            announcement = announcement,
                            isDark = isDark,
                            canDelete = canDeleteAnnouncements,
                            isDeleting = isDeletingAnnouncement,
                            onDeleteClick = { onDeleteAnnouncement(announcement) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementListItem(
    announcement: Announcement,
    isDark: Boolean,
    canDelete: Boolean,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else KalkanBorder.copy(alpha = 0.6f)
    val priorityColor = priorityColor(announcement.priority)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Priority stripe
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(priorityColor, RoundedCornerShape(2.dp)),
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriorityBadge(priority = announcement.priority)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = KalkanTextMuted, modifier = Modifier.size(11.dp))
                    Text(
                        text = formatAnnouncementDate(announcement.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = KalkanTextMuted,
                    )
                }
            }
        }

        if (canDelete) {
            IconButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Duyuruyu sil",
                    tint = KalkanRed,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = KalkanTextMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PriorityBadge(priority: AnnouncementPriority) {
    val color = priorityColor(priority)
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ErrorCard(message: String, isDark: Boolean) {
    val cardBg = if (isDark) Color(0xFF2D1B1B) else Color(0xFFFFF5F5)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, KalkanRed.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Error, contentDescription = null, tint = KalkanRed, modifier = Modifier.size(20.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = KalkanRed)
    }
}

@Composable
private fun EmptyAnnouncementsCard(isDark: Boolean) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, KalkanBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Rounded.Campaign,
            contentDescription = null,
            tint = KalkanTextMuted,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = "Henüz duyuru oluşturulmadı",
            style = MaterialTheme.typography.bodyMedium,
            color = KalkanTextMuted,
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────────────────────

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
        description = "Sistem duyuruları ve genel açıklamalar yayınlayın.",
        icon = Icons.Rounded.Campaign,
        tint = KalkanBlue,
        route = "create_announcement",
    ),
    AdminFeature(
        title = "Acil Uyarı Yayınla",
        description = "Kritik acil durum bilgilendirmeleri gönderin.",
        icon = Icons.Rounded.Warning,
        tint = KalkanRed,
        route = "admin_emergency_alert",
    ),
    AdminFeature(
        title = "Kullanıcılar",
        description = "Kullanıcı ve aile grubu istatistikleri.",
        icon = Icons.Rounded.Groups,
        tint = Color(0xFF10B981),
        route = "admin_users",
    ),
    AdminFeature(
        title = "Bildirim Merkezi",
        description = "Geçmiş bildirim kayıtlarını listeleyin.",
        icon = Icons.Rounded.Notifications,
        tint = KalkanBlue,
        route = "admin_notifications",
    ),
    AdminFeature(
        title = "Deprem İzleme",
        description = "AFAD otomatik deprem izleme sistemi.",
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
    // ── Sensör Ağı ──────────────────────────────────────────────────────────────
    // İleri faz özelliği. Gerçek telefon sensörü, ivmeölçer veya saha sensör ağı
    // altyapısı kurulmadan kullanıcıya açılmayacaktır. visible = false kalmalıdır.
    AdminFeature(
        title = "Sensör Ağı",
        description = "Saha sensör altyapısı hazır olduğunda aktif edilecek ileri faz modülü.",
        icon = Icons.Rounded.Memory,
        tint = Color(0xFF8B5CF6),
        route = "sensor_network",
        visible = false,
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun priorityColor(priority: AnnouncementPriority): Color = when (priority) {
    AnnouncementPriority.NORMAL -> KalkanBlue
    AnnouncementPriority.IMPORTANT -> Color(0xFFF59E0B)
    AnnouncementPriority.URGENT -> KalkanRed
}

private fun formatAnnouncementDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(epochMillis))
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
