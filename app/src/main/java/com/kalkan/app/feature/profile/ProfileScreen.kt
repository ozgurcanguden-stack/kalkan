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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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

private val ProfileNavy = Color(0xFF131B2E)
private val KalkanLightBlue = Color(0xFFF3F8FC) // Sleek soft blue card background for Kalkan theme
private val KalkanSoftRed = Color(0xFFFEF2F2)
private val BadgeBgColor = Color(0xFFE6F0FA) // Light blue badge background to match Kalkan theme
private val BadgeIconColor = Color(0xFF1D4ED8) // Deep blue tint for Kalkan branding icons

enum class ProfileSubScreen {
    MAIN,
    HESAP_AYARLARI,
    BILDIRIM_AYARLARI,
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

    // Screen State
    var activeSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

    // Backup State — driven from ViewModel/Firestore
    val lastBackupTime = uiState.lastBackupFormatted

    // Notification State
    var vibrationEnabled by remember { mutableStateOf(true) }
    var alertMode by remember { mutableStateOf("Sesli uyarı") }
    var selectedSound by remember { mutableStateOf("Deprem Alarm Sesi (Varsayılan)") }

    // Dialogs
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Respect navigation bar height
        ) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "SubScreenTransition"
            ) { targetScreen ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp), // Exactly matching HomeScreen & FamilyScreen paddings!
                    verticalArrangement = Arrangement.spacedBy(18.dp) // Exactly matching FamilyScreen vertical spacings!
                ) {
                    when (targetScreen) {
                        ProfileSubScreen.MAIN -> {
                            // Custom Symmetrical Header (Matches FamilyTopBar exactly)
                            ProfileTopBar()

                            // 1. User Header Info Card
                            ProfileHeaderCard(user = user, hasAdminAccess = hasAdminAccess)

                            // Admin Access row if authorized
                            if (hasAdminAccess) {
                                AdminPanelEntry(onAdminPanelClick = onAdminPanelClick)
                            }

                            // Section 1: UYGULAMA (KalkanBlue Header)
                            SectionHeader(title = "Uygulama")

                            // Hesap Ayarları (Opens sub-screen)
                            SettingItemCard(
                                icon = Icons.Rounded.AccountCircle,
                                title = "Hesap Ayarları",
                                subtitle = "Eşitleme, oturumu kapat ve hesap sil",
                                onClick = { activeSubScreen = ProfileSubScreen.HESAP_AYARLARI }
                            )

                            // Bildirim Ayarları (Opens sub-screen)
                            SettingItemCard(
                                icon = Icons.Rounded.NotificationsActive,
                                title = "Bildirim Ayarları",
                                subtitle = "Titreşim, uyarı modu ve alarm sesi",
                                onClick = { activeSubScreen = ProfileSubScreen.BILDIRIM_AYARLARI }
                            )

                            // Section 2: BİLGİ
                            SectionHeader(title = "Bilgi")

                            // Gizlilik (Opens sub-screen)
                            SettingItemCard(
                                icon = Icons.Rounded.Shield,
                                title = "Gizlilik",
                                subtitle = "Veri kullanımı ve gizlilik politikası",
                                onClick = { activeSubScreen = ProfileSubScreen.GIZLILIK }
                            )

                            // Hakkında (Opens sub-screen)
                            SettingItemCard(
                                icon = Icons.Rounded.Info,
                                title = "Hakkında",
                                subtitle = "Sürüm, geliştirici ve destek",
                                onClick = { activeSubScreen = ProfileSubScreen.HAKKINDA }
                            )
                        }

                        ProfileSubScreen.HESAP_AYARLARI -> {
                            // Custom Symmetrical Subpage Header
                            SubScreenTopBar(title = "Hesap Ayarları") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            // 1. Details Container Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KalkanLightBlue),
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

                                    // Inner card: Otomatik yedekleme
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (user?.isGuest == false) {
                                                    showBackupScheduleDialog = true
                                                }
                                            }
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

                            Spacer(modifier = Modifier.height(2.dp))

                            // Action Button 1: Şimdi Yedekle / Eşitle
                            if (user?.isGuest == false) {
                                Button(
                                    onClick = {
                                        onBackupClick()
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E3F5)),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    if (uiState.isBackupLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = KalkanBlue)
                                    } else {
                                        Text("Şimdi Yedekle", color = KalkanBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            // Action Button 2: Çıkış Yap
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

                            // Action Button 3: Hesabımı Sil
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

                        ProfileSubScreen.BILDIRIM_AYARLARI -> {
                            // Custom Symmetrical Subpage Header
                            SubScreenTopBar(title = "Bildirim Ayarları") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            // 1. Titreşim Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KalkanLightBlue),
                                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Titreşim",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Bildirimlerde titreşim kullan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KalkanTextMuted
                                        )
                                    }
                                    Switch(
                                        checked = vibrationEnabled,
                                        onCheckedChange = {
                                            vibrationEnabled = it
                                            AppTopNotificationCenter.showSuccess(
                                                if (it) "Titreşim etkinleştirildi." else "Titreşim kapatıldı."
                                            )
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = KalkanBlue
                                        )
                                    )
                                }
                            }

                            // 2. Uyarı Modu Segmented Buttons
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KalkanLightBlue),
                                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Uyarı modu",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    SegmentedButtons(
                                        options = listOf("Sesli uyarı", "Titreşim", "Sadece bildirim"),
                                        selectedOption = alertMode,
                                        onOptionSelected = {
                                            alertMode = it
                                            AppTopNotificationCenter.showSuccess("Uyarı modu güncellendi: $it")
                                        }
                                    )
                                }
                            }

                            // 3. Alarm Sesi & Warning Section
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KalkanLightBlue),
                                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Alarm Sesi",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Bildirim sesini seçin: $selectedSound",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KalkanTextMuted
                                        )
                                    }

                                    // Button: Bildirim sesi değiştir.
                                    Button(
                                        onClick = {
                                            AppTopNotificationCenter.showSuccess("Mevcut deprem alarm sesi seçildi.")
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E3F5)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Bildirim sesi değiştir.", color = KalkanBlue, fontWeight = FontWeight.Bold)
                                    }

                                    // Warning Box
                                    WarningBox(
                                        text = "Kritik afet ve acil deprem uyarılarında sistem ses seviyesi kapalı olsa dahi sesli siren çalacaktır. Telefon izinleri kontrol edilmelidir."
                                    )

                                    // Button: Sesi varsayılan ayara getir
                                    OutlinedButton(
                                        onClick = {
                                            selectedSound = "Deprem Alarm Sesi (Varsayılan)"
                                            AppTopNotificationCenter.showSuccess("Alarm sesi varsayılana döndürüldü.")
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Sesi varsayılan ayara getir", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Local Test notification trigger
                            Button(
                                onClick = onTestNotificationClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue)
                            ) {
                                Text("Test Bildirimi Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        ProfileSubScreen.GIZLILIK -> {
                            // Custom Symmetrical Subpage Header
                            SubScreenTopBar(title = "Gizlilik") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            Text(
                                text = "Güncel gizlilik politikasının tam metni web sayfamızda yayınlanır. Aşağıdaki düğme ile tarayıcıda açabilirsiniz.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Button: Gizlilik politikasını aç
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
                            // Custom Symmetrical Subpage Header
                            SubScreenTopBar(title = "Hakkında") {
                                activeSubScreen = ProfileSubScreen.MAIN
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Center content
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Circular info badge
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(Color(0xFFE6F0FA), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Info Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KalkanLightBlue),
                                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Geliştirici",
                                            color = KalkanTextMuted,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "ZG Mobile Apps",
                                            color = MaterialTheme.colorScheme.primary,
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
                                        Text(
                                            text = "İletişim",
                                            color = KalkanTextMuted,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "zgmobileapps@gmail.com",
                                            color = Color(0xFF0F766E),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BACKUP SCHEDULE DIALOG ---
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
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF0D9488)
                                )
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

    // --- DOUBLE CONFIRMATION DIALOGS ---

    // 1. Delete Account Confirmation 1
    if (showDeleteAccountDialog1) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog1 = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed) },
            title = { Text("Hesabı Tamamen Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Hesabınızı silmek; tüm profil bilgilerinizi, acil durum rehberinizi ve aile üyeliğinizi kalıcı olarak yok edecektir. Aile grubu sahibiyseniz grubu silmeden işlem yapmanız gruptaki diğer üyeleri de etkileyecektir.") },
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

    // 2. Delete Account Confirmation 2
    if (showDeleteAccountDialog2) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog2 = false },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = KalkanRed) },
            title = { Text("Üyeliğinizi Sonlandırın", fontWeight = FontWeight.Bold) },
            text = { Text("Kalkan hesabınızı ve verilerinizi tamamen kapatmak istediğinize emin misiniz? Bu işlem geri döndürülemez.") },
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

// Symmetrical Main Top Greeting Bar matching FamilyTopBar
@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Profil ve Ayarlar",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

// Symmetrical Subpage Header matching FamilyTopBar alignment
@Composable
private fun SubScreenTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall, // Symmetrical typography
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = KalkanBlue, // Custom KalkanBlue header color
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun SettingItemCard(
    icon: ImageVector,
    iconBgColor: Color = BadgeBgColor,
    iconTintColor: Color = BadgeIconColor,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left round badge with background matching theme
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(20.dp))
            }

            // Title & Subtitle column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing item
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = KalkanTextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminPanelEntry(onAdminPanelClick: () -> Unit) {
    SettingItemCard(
        icon = Icons.Rounded.AdminPanelSettings,
        iconBgColor = Color(0xFFFEF3C7),
        iconTintColor = Color(0xFFD97706),
        title = "Admin Paneli",
        subtitle = "Kalkan deprem yönetim merkezi",
        onClick = onAdminPanelClick
    )
}

@Composable
private fun ProfileHeaderCard(
    user: AppUser?,
    hasAdminAccess: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(ProfileNavy, KalkanBlue)))
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape)
                        .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.26f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Merhaba, Kullanıcı",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email?.takeIf { it.isNotBlank() } ?: "Misafir hesabı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    Text(
                        text = if (hasAdminAccess) "Süper Admin" else "Kullanıcı",
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.16f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Segmented Buttons for Notification Alerts
@Composable
private fun SegmentedButtons(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isSelected) Color(0xFFD0E3F5) else Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) KalkanBlue else KalkanBorder.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) KalkanBlue else KalkanTextMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Red warning alert box
@Composable
private fun WarningBox(text: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KalkanSoftRed),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFF1F2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Uyarı",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    fontSize = 13.sp
                )
                Text(
                    text = text,
                    color = Color(0xFF7F1D1D),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
