package com.kalkan.app.feature.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.model.AppUser
import com.kalkan.app.viewmodel.SettingsUiState
import com.kalkan.app.util.AppVersionUtils
import com.kalkan.app.ui.components.AppTopNotificationCenter
import com.kalkan.app.ui.components.RemoteProfileImage

private val StitchSecondary = Color(0xFF0051D5)
private val StitchPrimaryContainer = Color(0xFF131B2E)
private val StitchError = Color(0xFFBA1A1A)
private val StitchErrorContainer = Color(0xFFFFDAD6)
private val StitchSurfaceVariant = Color(0xFFE0E3E5)
private val StitchOutline = Color(0xFF76777D)
private val StitchSecondaryFixedDim = Color(0xFFB4C5FF)
private val StitchPrimaryFixedDim = Color(0xFFBEC6E0)

enum class ProfileSubScreen {
    MAIN,
    HESAP_AYARLARI,
    GIZLILIK,
    HAKKINDA
}

@Composable
fun ProfileScreen(
    user: AppUser?,
    hasAdminAccess: Boolean,
    notificationPermissionGranted: Boolean,
    uiState: SettingsUiState,
    onAdminPanelClick: () -> Unit,
    onSignOut: () -> Unit,
    onBackupClick: () -> Unit,
    onSetBackupFrequency: (com.kalkan.app.model.BackupFrequency) -> Unit,
    onDeleteAccountClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    onClearMessages: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var activeSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }
    val lastBackupTime = uiState.lastBackupFormatted

    var earthquakeAlertsEnabled by remember { mutableStateOf(true) }
    var familyAlertsEnabled by remember { mutableStateOf(true) }

    var showDeleteAccountDialog1 by remember { mutableStateOf(false) }
    var showDeleteAccountDialog2 by remember { mutableStateOf(false) }
    var showBackupScheduleDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            AppTopNotificationCenter.showSuccess(it)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            onClearMessages()
        }
    }

    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val pageBg = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF7F9FB)
    val borderColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else StitchSurfaceVariant

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = pageBg
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "SubScreenTransition"
            ) { targetScreen ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(pageBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    when (targetScreen) {
                        ProfileSubScreen.MAIN -> {
                            ProfileStitchHeaderCard(
                                user = user,
                                hasAdminAccess = hasAdminAccess,
                                cardBg = cardBg,
                                borderColor = borderColor,
                                isDark = isDark,
                            )

                            StitchSettingsSection(
                                title = "Hesap ve Güvenlik",
                                cardBg = cardBg,
                                borderColor = borderColor,
                            ) {
                                StitchListRow(
                                    icon = Icons.Rounded.Person,
                                    label = "Hesap Bilgileri",
                                    onClick = { activeSubScreen = ProfileSubScreen.HESAP_AYARLARI },
                                    showDivider = true,
                                    isDark = isDark,
                                )
                                StitchListRow(
                                    icon = Icons.Rounded.Shield,
                                    label = "Gizlilik ve Güvenlik",
                                    onClick = { activeSubScreen = ProfileSubScreen.GIZLILIK },
                                    showDivider = true,
                                    isDark = isDark,
                                )
                                StitchListRow(
                                    icon = Icons.AutoMirrored.Rounded.Help,
                                    label = "Yardım ve Destek",
                                    onClick = { activeSubScreen = ProfileSubScreen.HAKKINDA },
                                    showDivider = false,
                                    isDark = isDark,
                                )
                            }

                            StitchSettingsSection(
                                title = "Bildirim Ayarları",
                                cardBg = cardBg,
                                borderColor = borderColor,
                            ) {
                                StitchToggleRow(
                                    icon = Icons.Rounded.Sensors,
                                    iconBg = StitchErrorContainer.copy(alpha = if (isDark) 0.3f else 0.2f),
                                    iconTint = StitchError,
                                    title = "Deprem Uyarıları",
                                    subtitle = "Yakın konumdaki 4.0+ sarsıntılar",
                                    checked = earthquakeAlertsEnabled,
                                    onCheckedChange = {
                                        earthquakeAlertsEnabled = it
                                        AppTopNotificationCenter.showSuccess(
                                            if (it) "Deprem uyarıları etkinleştirildi." else "Deprem uyarıları kapatıldı."
                                        )
                                    },
                                    showDivider = true,
                                    isDark = isDark,
                                )
                                StitchToggleRow(
                                    icon = Icons.Rounded.Group,
                                    iconBg = StitchSecondaryFixedDim.copy(alpha = if (isDark) 0.3f else 0.2f),
                                    iconTint = StitchSecondary,
                                    title = "Aile Bildirimleri",
                                    subtitle = "Güvendeyim durum güncellemeleri",
                                    checked = familyAlertsEnabled,
                                    onCheckedChange = {
                                        familyAlertsEnabled = it
                                        AppTopNotificationCenter.showSuccess(
                                            if (it) "Aile bildirimleri etkinleştirildi." else "Aile bildirimleri kapatıldı."
                                        )
                                    },
                                    showDivider = false,
                                    isDark = isDark,
                                )
                            }

                            StitchLogoutButton(
                                onClick = onSignOut,
                                isDark = isDark,
                            )
                        }

                        ProfileSubScreen.HESAP_AYARLARI -> {
                            SubScreenTopBar(title = "Hesap Bilgileri") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            if (hasAdminAccess) {
                                AdminPanelEntry(onAdminPanelClick = onAdminPanelClick)
                            }

                            AccountDetailsCard(
                                user = user,
                                uiState = uiState,
                                lastBackupTime = lastBackupTime,
                                onBackupScheduleClick = {
                                    if (user?.isGuest == false) showBackupScheduleDialog = true
                                },
                            )

                            if (user?.isGuest == false) {
                                Button(
                                    onClick = onBackupClick,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E3F5)),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    if (uiState.isBackupLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = KalkanBlue
                                        )
                                    } else {
                                        Text("Şimdi Yedekle", color = KalkanBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = onSignOut,
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanBlue),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Text("Çıkış Yap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            OutlinedButton(
                                onClick = { showDeleteAccountDialog1 = true },
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, KalkanRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanRed),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Text("Hesabımı Sil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        ProfileSubScreen.GIZLILIK -> {
                            SubScreenTopBar(title = "Gizlilik ve Güvenlik") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            Text(
                                text = "Güncel gizlilik politikasının tam metni web sayfamızda yayınlanır. Aşağıdaki düğme ile tarayıcıda açabilirsiniz.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Button(
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://kalkan-afet.web.app/privacy")
                                    )
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Tarayıcı açılamadı.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E3F5)),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = KalkanBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Gizlilik politikasını aç", color = KalkanBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        ProfileSubScreen.HAKKINDA -> {
                            SubScreenTopBar(title = "Yardım ve Destek") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(Color(0xFFE6F0FA), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Help,
                                        contentDescription = null,
                                        tint = KalkanBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = "Kalkan",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KalkanBlue
                                )

                                Text(
                                    text = "v${AppVersionUtils.getAppVersion(context)} (Build 1)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KalkanTextMuted
                                )

                                Text(
                                    text = "Kalkan deprem güvenliği, canlı sismik takip ve acil durum paylaşım platformudur.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = KalkanTextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = "Geliştirici", color = KalkanTextMuted, fontSize = 13.sp)
                                        Text(
                                            text = "ZG Mobile Apps",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.clickable {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                data = android.net.Uri.parse("mailto:zgmobileapps@gmail.com")
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Kalkan Destek Talebi")
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "E-posta istemcisi bulunamadı.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Text(text = "İletişim", color = KalkanTextMuted, fontSize = 13.sp)
                                        Text(
                                            text = "zgmobileapps@gmail.com",
                                            color = Color(0xFF0F766E),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onTestNotificationClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue)
                            ) {
                                Text("Test Bildirimi Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBackupScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showBackupScheduleDialog = false },
            title = {
                Text(
                    text = "Otomatik yedeklemeler",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    com.kalkan.app.model.BackupFrequency.entries.forEach { freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetBackupFrequency(freq)
                                    showBackupScheduleDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.backupFrequency == freq,
                                onClick = {
                                    onSetBackupFrequency(freq)
                                    showBackupScheduleDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0D9488))
                            )
                            Text(
                                text = freq.dialogLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupScheduleDialog = false }) {
                    Text("İptal", color = Color(0xFF0D9488), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showDeleteAccountDialog1) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog1 = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed) },
            title = { Text("Hesabı Tamamen Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Hesabınızı silmek; tüm profil bilgilerinizi, acil durum rehberinizi ve aile üyeliğinizi kalıcı olarak yok edecektir. Aile grubu sahibiyseniz grubu silmeden işlem yapmanız gruptaki diğer üyeleri de etkileyecektir."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog1 = false
                        showDeleteAccountDialog2 = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                ) { Text("Devam Et") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog1 = false }) { Text("İptal") }
            }
        )
    }

    if (showDeleteAccountDialog2) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog2 = false },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = KalkanRed) },
            title = { Text("Üyeliğinizi Sonlandırın", fontWeight = FontWeight.Bold) },
            text = {
                Text("Kalkan hesabınızı ve verilerinizi tamamen kapatmak istediğinize emin misiniz? Bu işlem geri döndürülemez.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog2 = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                ) { Text("Evet, Hesabımı Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog2 = false }) { Text("Vazgeç") }
            }
        )
    }
}

@Composable
private fun ProfileStitchHeaderCard(
    user: AppUser?,
    hasAdminAccess: Boolean,
    cardBg: Color,
    borderColor: Color,
    isDark: Boolean,
) {
    val badgeText = when {
        hasAdminAccess -> "Süper Admin"
        user?.isGuest == true -> "Misafir Hesap"
        else -> "Doğrulanmış Hesap"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                StitchPrimaryFixedDim.copy(alpha = if (isDark) 0.18f else 0.28f),
                                StitchPrimaryFixedDim.copy(alpha = if (isDark) 0.06f else 0.1f),
                                cardBg,
                            ),
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RemoteProfileImage(
                    photoUrl = user?.photoUrl,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            if (isDark) MaterialTheme.colorScheme.surfaceVariant else StitchSurfaceVariant,
                            CircleShape,
                        )
                        .border(4.dp, cardBg, CircleShape),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Text(
                    text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Kullanıcı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = user?.email?.takeIf { it.isNotBlank() } ?: "Misafir hesabı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .background(
                            if (isDark) MaterialTheme.colorScheme.surfaceVariant else StitchSurfaceVariant,
                            CircleShape,
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = if (isDark) MaterialTheme.colorScheme.primary else StitchSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchSettingsSection(
    title: String,
    cardBg: Color,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = StitchOutline,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun StitchListRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    isDark: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        StitchPrimaryContainer.copy(alpha = if (isDark) 0.2f else 0.1f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = StitchSurfaceVariant.copy(alpha = if (isDark) 0.3f else 0.5f),
            )
        }
    }
}

@Composable
private fun StitchToggleRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean,
    isDark: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = StitchSecondary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = StitchOutline.copy(alpha = 0.4f),
                ),
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = StitchSurfaceVariant.copy(alpha = if (isDark) 0.3f else 0.5f),
            )
        }
    }
}

@Composable
private fun StitchLogoutButton(onClick: () -> Unit, isDark: Boolean) {
    val bgColor = StitchErrorContainer.copy(alpha = if (isDark) 0.3f else 0.2f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, StitchError.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = null,
                tint = StitchError,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Çıkış Yap",
                color = StitchError,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
private fun SubScreenTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdminPanelEntry(onAdminPanelClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdminPanelClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Admin Paneli", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Kalkan deprem yönetim merkezi", color = KalkanTextMuted, fontSize = 13.sp)
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun AccountDetailsCard(
    user: AppUser?,
    uiState: SettingsUiState,
    lastBackupTime: String,
    onBackupScheduleClick: () -> Unit,
) {
    val cardBg = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color(0xFFF3F8FC)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hesap ve Senkronizasyon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KalkanBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Giriş yöntemi: ${if (user?.isGuest == true) "Misafir" else "Google"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "E-posta: ${user?.email?.takeIf { it.isNotBlank() } ?: "Yok (Misafir Girişi)"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (user?.isGuest == true) "Bulut yedekleme pasif" else "Bulut yedekleme aktif",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (user?.isGuest == true) KalkanRed else Color(0xFF0F766E),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bulut Senkronizasyonu: ${if (user?.isGuest == true) "Pasif" else "Aktif"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Otomatik Yedekleme: ${if (user?.isGuest == true) "Kapalı" else uiState.backupFrequency.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Son Yedekleme: ${if (user?.isGuest == true) "-" else lastBackupTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackupScheduleClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Otomatik yedekleme",
                            color = Color(0xFF0D9488),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (user?.isGuest == true) "Misafir modunda kapalı" else uiState.backupFrequency.label,
                            color = KalkanTextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = KalkanTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
