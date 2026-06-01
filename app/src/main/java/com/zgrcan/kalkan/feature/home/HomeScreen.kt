package com.zgrcan.kalkan.feature.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanGreen
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.core.design.theme.PlusJakartaSansFamily
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeLastUpdatedLabel
import com.zgrcan.kalkan.feature.earthquakes.formatEarthquakeDate
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.ui.components.AnnouncementCard
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.ui.components.RemoteProfileImage
import com.zgrcan.kalkan.util.EmergencyIntentHelper
import com.zgrcan.kalkan.viewmodel.AnnouncementsUiState
import com.zgrcan.kalkan.viewmodel.SafetyStatusUiState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.South
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast

private const val HOME_ANNOUNCEMENT_PREVIEW_LIMIT = 3

private val SuccessContainer = Color(0xFFC4EED0)
private val OnSuccessContainer = Color(0xFF072711)
private val SurfaceVariant = Color(0xFFE0E3E5)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

@Composable
fun HomeScreen(
    announcementsState: AnnouncementsUiState,
    onAnnouncementClick: (String) -> Unit,
    onRetryAnnouncements: () -> Unit,
    safetyStatusState: SafetyStatusUiState,
    onSubmitSafetyStatus: (SafetyStatusType) -> Unit,
    onSubmitSafetyStatusWithLocation: (SafetyStatusType, Boolean) -> Unit,
    onDismissSafetyMessage: () -> Unit,
    onLoadSafetyStatus: (String?) -> Unit,
    currentUser: com.zgrcan.kalkan.model.AppUser?,
    onSettingsClick: () -> Unit,
    earthquakesState: com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState,
    onSeeAllEarthquakesClick: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
    contacts: List<com.zgrcan.kalkan.model.EmergencyContact>,
    onAddContactClick: () -> Unit,
) {
    val context = LocalContext.current
    var pendingLocationStatusType by remember { mutableStateOf<SafetyStatusType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val statusType = pendingLocationStatusType
        pendingLocationStatusType = null
        if (statusType != null) {
            onSubmitSafetyStatusWithLocation(statusType, granted)
        }
    }

    fun handleSafetyStatusClick(statusType: SafetyStatusType) {
        when {
            statusType.requiresLocationAttempt -> {
                if (hasLocationPermission()) {
                    onSubmitSafetyStatusWithLocation(statusType, true)
                } else {
                    pendingLocationStatusType = statusType
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            }
            else -> onSubmitSafetyStatus(statusType)
        }
    }

    LaunchedEffect(currentUser?.uid) {
        onLoadSafetyStatus(currentUser?.uid)
    }

    LaunchedEffect(safetyStatusState.snackbarMessage) {
        val message = safetyStatusState.snackbarMessage ?: return@LaunchedEffect
        if (safetyStatusState.isError) {
            snackbarHostState.showSnackbar(message)
        } else {
            AppTopNotificationCenter.showSuccess(message)
        }
        onDismissSafetyMessage()
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TopGreetingBar(user = currentUser, onSettingsClick = onSettingsClick)
            StatusCard(statusType = safetyStatusState.currentStatusType)
            EmergencyActionGrid(
                isSubmitting = safetyStatusState.isSubmitting,
                onSafeClick = { handleSafetyStatusClick(SafetyStatusType.SAFE) },
                onNeedHelpClick = { handleSafetyStatusClick(SafetyStatusType.NEED_HELP) },
                onShareLocationClick = { handleSafetyStatusClick(SafetyStatusType.SHARE_LOCATION) },
                onSosClick = { handleSafetyStatusClick(SafetyStatusType.SOS) },
            )
            Emergency112CallBar(
                onClick = {
                    if (!EmergencyIntentHelper.openEmergency112Dialer(context)) {
                        Toast.makeText(context, "Arama başlatılamadı.", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            AnnouncementsSection(
                state = announcementsState,
                onAnnouncementClick = onAnnouncementClick,
                onRetry = onRetryAnnouncements,
            )
            EmergencyContactsCard(
                contacts = contacts,
                onAddContactClick = onAddContactClick,
            )
            RecentEarthquakesCard(
                state = earthquakesState,
                onSeeAllClick = onSeeAllEarthquakesClick,
                onEarthquakeClick = onEarthquakeClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (safetyStatusState.isSubmitting) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = KalkanBlue)
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
private fun TopGreetingBar(
    user: com.zgrcan.kalkan.model.AppUser?,
    onSettingsClick: () -> Unit,
) {
    val greeting = com.zgrcan.kalkan.util.GreetingUtils.getGreeting()
    val welcomeName = when {
        user == null || user.isGuest -> "Merhaba, Misafir"
        else -> {
            val name = user.displayName.trim()
            val firstName = name.substringBefore(" ")
            "Merhaba, $firstName"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RemoteProfileImage(
                photoUrl = user?.photoUrl,
                shape = CircleShape,
                modifier = Modifier.size(44.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KalkanBlue.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = KalkanBlue,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted,
                )
                Text(
                    text = welcomeName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

    }
}

@Composable
private fun AnnouncementsSection(
    state: AnnouncementsUiState,
    onAnnouncementClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    HomeSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Campaign,
                contentDescription = null,
                tint = KalkanBlue,
            )
            Text(
                text = "Duyurular",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        when (state) {
            AnnouncementsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = KalkanBlue,
                        strokeWidth = 2.dp,
                    )
                }
            }
            AnnouncementsUiState.Empty -> {
                Text(
                    text = "Henuz duyuru yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted,
                )
            }
            is AnnouncementsUiState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanRed,
                )
                TextButton(onClick = onRetry) {
                    Text("Tekrar Dene", color = KalkanBlue)
                }
            }
            is AnnouncementsUiState.Success -> {
                state.announcements
                    .take(HOME_ANNOUNCEMENT_PREVIEW_LIMIT)
                    .forEach { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            onClick = { onAnnouncementClick(announcement.id) },
                        )
                    }
            }
        }
    }
}

private val Emergency112Red = Color(0xFFBA1A1A)

private val emergency112TextStyle = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
    color = Color.White,
)

@Composable
private fun Emergency112CallBar(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Emergency112Red),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "112",
                    style = emergency112TextStyle,
                )
                Text(
                    text = "ACİL ÇAĞRI",
                    style = emergency112TextStyle,
                )
            }
        }
    }
}

private data class StatusCardPresentation(
    val title: String,
    val description: String,
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val iconTint: Color,
)

private fun statusCardPresentation(statusType: SafetyStatusType?): StatusCardPresentation =
    when (statusType) {
        SafetyStatusType.SAFE -> StatusCardPresentation(
            title = "Güvendesiniz",
            description = "Yakınınızda riskli bir olay bulunmuyor.",
            containerColor = SuccessContainer,
            borderColor = KalkanGreen.copy(alpha = 0.16f),
            contentColor = OnSuccessContainer,
            icon = Icons.Rounded.CheckCircle,
            iconTint = KalkanGreen,
        )
        SafetyStatusType.NEED_HELP -> StatusCardPresentation(
            title = "Yardım Talebi Aktif",
            description = "Aile üyelerinize yardım bildirimi gönderildi.",
            containerColor = Color(0xFFFFF7ED),
            borderColor = Color(0xFFFDBA74).copy(alpha = 0.55f),
            contentColor = Color(0xFF9A3412),
            icon = Icons.Rounded.Warning,
            iconTint = Color(0xFFEA580C),
        )
        SafetyStatusType.SOS -> StatusCardPresentation(
            title = "SOS Aktif",
            description = "Acil yardım çağrınız aile üyelerinize iletildi.",
            containerColor = Color(0xFFFEE2E2),
            borderColor = Color(0xFFB91C1C).copy(alpha = 0.35f),
            contentColor = OnErrorContainer,
            icon = Icons.Rounded.HealthAndSafety,
            iconTint = Color(0xFFB91C1C),
        )
        SafetyStatusType.SHARE_LOCATION -> StatusCardPresentation(
            title = "Konum Paylaşılıyor",
            description = "Konum bilginiz aile üyelerinizle paylaşılıyor.",
            containerColor = Color(0xFFE8F0FE),
            borderColor = KalkanBlue.copy(alpha = 0.28f),
            contentColor = Color(0xFF1A73E8),
            icon = Icons.Rounded.LocationOn,
            iconTint = KalkanBlue,
        )
        null -> StatusCardPresentation(
            title = "Durum Belirtilmedi",
            description = "Güvenlik durumunuzu paylaşın.",
            containerColor = SurfaceVariant,
            borderColor = KalkanBorder.copy(alpha = 0.45f),
            contentColor = Color(0xFF475569),
            icon = Icons.Rounded.HelpOutline,
            iconTint = KalkanTextMuted,
        )
    }

@Composable
private fun StatusCard(statusType: SafetyStatusType?) {
    val presentation = statusCardPresentation(statusType)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = presentation.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, presentation.borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.iconTint,
                )
                Text(
                    text = "DURUM",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.1.sp),
                    color = presentation.contentColor.copy(alpha = 0.78f),
                )
            }
            Text(
                text = presentation.title,
                style = MaterialTheme.typography.headlineSmall,
                color = presentation.contentColor,
            )
            Text(
                text = presentation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = presentation.contentColor.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun EmergencyActionGrid(
    isSubmitting: Boolean,
    onSafeClick: () -> Unit,
    onNeedHelpClick: () -> Unit,
    onShareLocationClick: () -> Unit,
    onSosClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EmergencyTile(
                title = "İyiyim",
                icon = Icons.Rounded.CheckCircle,
                containerColor = Color(0xFF0B5121),
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
                onClick = onSafeClick,
            )
            EmergencyTile(
                title = "Yardım\nİstiyorum",
                icon = Icons.Rounded.Warning,
                containerColor = KalkanRed,
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
                onClick = onNeedHelpClick,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationTile(
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
                onClick = onShareLocationClick,
            )
            SosEmergencyTile(
                modifier = Modifier.weight(1f),
                enabled = !isSubmitting,
                onClick = onSosClick,
            )
        }
    }
}

private val SosTileRed = Color(0xFFDC2626)
private val SosTileBorder = Color(0xFFFFE4E1)

@Composable
private fun SosEmergencyTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sosScale",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 140.dp)
            .border(width = 2.dp, color = SosTileBorder, shape = RoundedCornerShape(16.dp))
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SosTileRed),
        contentPadding = PaddingValues(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        BlockySosText(
            color = Color.White,
            letterHeight = 50.dp,
            letterSpacing = 8.dp,
        )
    }
}

@Composable
private fun EmergencyTile(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleSize: Int = 24,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = titleSize.sp,
                    lineHeight = (titleSize + 6).sp,
                ),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LocationTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceVariant),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = KalkanBlue,
            )
            Text(
                text = "Konum Paylaş",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private val TertiaryFixed = Color(0xFFFCDEB5)
private val OnTertiaryFixed = Color(0xFF271901)

@Composable
private fun RecentEarthquakesCard(
    state: com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState,
    onSeeAllClick: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
) {
    HomeSectionCard {
        Text(
            text = "Son Depremler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        EarthquakeLastUpdatedLabel(
            lastUpdatedAt = state.lastUpdatedAt,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
        )
        when (state) {
            is com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KalkanBlue)
                }
            }
            is com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState.Empty -> {
                Text(
                    text = "Aktif deprem verisi bulunamadı.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState.Error -> {
                val cached = state.cachedEarthquakes
                if (cached.isNotEmpty()) {
                    Column {
                        cached.take(3).forEachIndexed { index, eq ->
                            EarthquakeItem(
                                magnitude = String.format(Locale("tr", "TR"), "%.1f", eq.magnitude),
                                location = eq.location.ifBlank { "Konum bilgisi yok" },
                                detail = "${eq.dateTime.formatEarthquakeDate()} • Derinlik: ${eq.depth} km",
                                badgeColor = if (eq.magnitude >= 5.0) ErrorContainer else if (eq.magnitude >= 4.0) TertiaryFixed else SurfaceVariant,
                                badgeTextColor = if (eq.magnitude >= 5.0) OnErrorContainer else if (eq.magnitude >= 4.0) OnTertiaryFixed else Color(0xFF0F172A),
                                onClick = {
                                    if (eq.id.isNotBlank()) onEarthquakeClick(eq.id)
                                },
                            )
                            if (index < cached.take(3).lastIndex) {
                                HorizontalDivider(color = KalkanBorder.copy(alpha = 0.45f))
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Deprem verileri alınamadı.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            is com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState.Success -> {
                val list = state.earthquakes
                Column {
                    list.take(3).forEachIndexed { index, eq ->
                        EarthquakeItem(
                            magnitude = String.format(Locale("tr", "TR"), "%.1f", eq.magnitude),
                            location = eq.location.ifBlank { "Konum bilgisi yok" },
                            detail = "${eq.dateTime.formatEarthquakeDate()} • Derinlik: ${eq.depth} km",
                            badgeColor = if (eq.magnitude >= 5.0) ErrorContainer else if (eq.magnitude >= 4.0) TertiaryFixed else SurfaceVariant,
                            badgeTextColor = if (eq.magnitude >= 5.0) OnErrorContainer else if (eq.magnitude >= 4.0) OnTertiaryFixed else Color(0xFF0F172A),
                            onClick = {
                                if (eq.id.isNotBlank()) onEarthquakeClick(eq.id)
                            },
                        )
                        if (index < list.take(3).lastIndex) {
                            HorizontalDivider(color = KalkanBorder.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }
        TextButton(
            onClick = onSeeAllClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Tümünü Gör",
                style = MaterialTheme.typography.labelLarge,
                color = KalkanBlue,
            )
        }
    }
}

@Composable
private fun EarthquakeItem(
    magnitude: String,
    location: String,
    detail: String,
    badgeColor: Color,
    badgeTextColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .size(48.dp)
                .background(badgeColor, RoundedCornerShape(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = magnitude,
                style = MaterialTheme.typography.titleMedium,
                color = badgeTextColor,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun EmergencyContactsCard(
    contacts: List<com.zgrcan.kalkan.model.EmergencyContact>,
    onAddContactClick: () -> Unit,
) {
    if (contacts.isEmpty()) {
        HomeSectionCard(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = KalkanTextMuted,
                )
            }
            Text(
                text = "Acil Durum Kişileri",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Henüz kişi eklenmedi. Acil durumlarda hızlıca ulaşmak için kişi ekleyin.",
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onAddContactClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Kişi Ekle", style = MaterialTheme.typography.labelLarge)
            }
        }
        return
    }

    HomeSectionCard {
        Text(
            text = "Acil Durum Kişileri",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contacts.take(3).forEach { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(KalkanBlue.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = KalkanBlue,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${contact.relation} • ${contact.phone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KalkanTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            if (!com.zgrcan.kalkan.util.EmergencyIntentHelper.openDialer(context, contact.phone)) {
                                Toast.makeText(context, "Arama başlatılamadı.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(KalkanBlue.copy(alpha = 0.1f), CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = "Ara",
                            tint = KalkanBlue,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        if (contacts.size > 3) {
            Text(
                text = "+${contacts.size - 3} kişi daha",
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
            )
        }
        TextButton(
            onClick = onAddContactClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Rehberi Yönet",
                style = MaterialTheme.typography.labelLarge,
                color = KalkanBlue,
            )
        }
    }
}

@Composable
private fun HomeSectionCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
