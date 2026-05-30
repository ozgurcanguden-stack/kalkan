package com.kalkan.app.feature.earthquakes

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.domain.model.Earthquake
import com.kalkan.app.model.SafetyStatusType
import com.kalkan.app.ui.components.AppTopNotificationCenter
import com.kalkan.app.ui.screens.EmergencyProfileTopBar
import com.kalkan.app.util.MapIntentHelper
import com.kalkan.app.viewmodel.SafetyStatusUiState
import java.util.Locale

private val SurfaceVariant = Color(0xFFE0E3E5)
private val TertiaryFixed = Color(0xFFFCDEB5)
private val OnTertiaryFixed = Color(0xFF271901)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

@Composable
fun EarthquakeDetailScreen(
    earthquake: Earthquake,
    safetyStatusState: SafetyStatusUiState,
    onBackClick: () -> Unit,
    onSubmitSafetyStatus: (SafetyStatusType) -> Unit,
    onSubmitSafetyStatusWithLocation: (SafetyStatusType, Boolean) -> Unit,
    onDismissSafetyMessage: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            EmergencyProfileTopBar(title = "Deprem Detayı", onBackClick = onBackClick)

            EarthquakeMagnitudeCard(earthquake = earthquake)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DetailRow(
                        label = "Merkez Üssü",
                        value = earthquake.location.ifBlank { "Konum bilgisi yok" },
                    )
                    DetailRow(
                        label = "Derinlik",
                        value = String.format(Locale("tr", "TR"), "%.2f km", earthquake.depth),
                    )
                    DetailRow(
                        label = "Tarih-Saat",
                        value = earthquake.dateTime.formatEarthquakeDetailDate(),
                    )
                    DetailRow(label = "Kaynak", value = earthquake.source.ifBlank { "AFAD" })
                    DetailRow(
                        label = "Koordinat",
                        value = if (earthquake.hasValidCoordinates()) {
                            String.format(
                                Locale.US,
                                "%.5f, %.5f",
                                earthquake.latitude,
                                earthquake.longitude,
                            )
                        } else {
                            "Belirtilmedi"
                        },
                    )
                }
            }

            if (earthquake.hasValidCoordinates()) {
                OutlinedButton(
                    onClick = {
                        if (!MapIntentHelper.openLocation(context, earthquake.latitude, earthquake.longitude)) {
                            Toast.makeText(context, "Harita açılamadı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanBlue),
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Haritada Aç", fontWeight = FontWeight.Bold)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Güvende misiniz?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Durumunuz aile grubunuza yansır ve mevcut güvenlik akışı üzerinden kaydedilir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CheckInButton(
                            title = "İyiyim",
                            icon = Icons.Rounded.CheckCircle,
                            containerColor = Color(0xFF0B5121),
                            modifier = Modifier.weight(1f),
                            enabled = !safetyStatusState.isSubmitting,
                            onClick = { handleSafetyStatusClick(SafetyStatusType.SAFE) },
                        )
                        CheckInButton(
                            title = "Yardıma\nİhtiyacım Var",
                            icon = Icons.Rounded.Warning,
                            containerColor = KalkanRed,
                            modifier = Modifier.weight(1f),
                            enabled = !safetyStatusState.isSubmitting,
                            onClick = { handleSafetyStatusClick(SafetyStatusType.NEED_HELP) },
                        )
                    }
                    OutlinedButton(
                        onClick = { handleSafetyStatusClick(SafetyStatusType.SHARE_LOCATION) },
                        enabled = !safetyStatusState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = KalkanBlue)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Konumumu Paylaş",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

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
private fun EarthquakeMagnitudeCard(earthquake: Earthquake) {
    val badgeColors = earthquake.badgeColors()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .size(88.dp)
                    .background(badgeColors.first, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = String.format(Locale("tr", "TR"), "%.1f", earthquake.magnitude),
                    style = MaterialTheme.typography.displaySmall,
                    color = badgeColors.second,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = earthquake.title.ifBlank { "ML" },
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeColors.second.copy(alpha = 0.78f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Büyüklük",
                    style = MaterialTheme.typography.labelLarge,
                    color = KalkanTextMuted,
                )
                Text(
                    text = String.format(Locale("tr", "TR"), "%.1f ML", earthquake.magnitude),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, color = KalkanTextMuted, fontSize = 13.sp)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun CheckInButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun Earthquake.badgeColors(): Pair<Color, Color> =
    when {
        magnitude >= 5.0 -> ErrorContainer to OnErrorContainer
        magnitude >= 4.0 -> TertiaryFixed to OnTertiaryFixed
        else -> SurfaceVariant to Color(0xFF0F172A)
    }
