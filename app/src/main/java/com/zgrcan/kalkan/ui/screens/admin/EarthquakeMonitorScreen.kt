package com.zgrcan.kalkan.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.*
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.viewmodel.EarthquakeMonitorUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EarthquakeMonitorScreen(
    uiState: EarthquakeMonitorUiState,
    onBackClick: () -> Unit,
    onSaveSettings: (Boolean, Int, Double) -> Unit,
    onClearMessages: () -> Unit,
) {
    val context = LocalContext.current
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showMagnitudeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            AppTopNotificationCenter.showSuccess(it)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            AppTopNotificationCenter.showSuccess("Hata: $it")
            onClearMessages()
        }
    }

    val systemStatusText = if (uiState.enabled) "Aktif (İzleniyor)" else "Pasif (Devre Dışı)"
    val systemStatusColor = if (uiState.enabled) Color(0xFF0F766E) else KalkanRed

    val lastCheckedText = if (uiState.lastCheckedAt != null) {
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(uiState.lastCheckedAt))
    } else {
        "Sorgulama yapılmadı"
    }

    val lastProcessedIdText = uiState.lastProcessedEarthquakeId ?: "Deprem işlenmedi"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
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
                        text = "Deprem İzleme Ayarları",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Yönetim Paneli",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted
                    )
                }
            }

            // Quick Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(systemStatusColor.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.enabled) Icons.Rounded.RssFeed else Icons.Rounded.PortableWifiOff,
                                contentDescription = null,
                                tint = systemStatusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Sistem Durumu",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = systemStatusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = systemStatusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Switch(
                        checked = uiState.enabled,
                        onCheckedChange = { isChecked ->
                            onSaveSettings(isChecked, uiState.intervalMinutes, uiState.minSystemMagnitude)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0F766E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
            }

            // Configurations Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Parametre Tercihleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Interval Option Row
                StitchConfigClickableRow(
                    icon = Icons.Rounded.HourglassEmpty,
                    title = "Sorgulama Sıklığı",
                    subtitle = if (!uiState.enabled) "Kapalı" else "${uiState.intervalMinutes} dakikada bir",
                    onClick = { showIntervalDialog = true }
                )

                // Minimum Magnitude Option Row
                StitchConfigClickableRow(
                    icon = Icons.AutoMirrored.Rounded.ShowChart,
                    title = "Minimum Sistem Büyüklük Eşiği",
                    subtitle = "${uiState.minSystemMagnitude} ve üzeri",
                    onClick = { showMagnitudeDialog = true }
                )
            }

            // Status Details Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "İzleme İstatistikleri",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Son AFAD Sorgulaması",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KalkanTextMuted
                        )
                        Text(
                            text = lastCheckedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = KalkanBorder.copy(alpha = 0.35f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Son İşlenen Deprem ID'si",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KalkanTextMuted
                        )
                        Text(
                            text = lastProcessedIdText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading Overlay
        if (uiState.isLoading || uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF0F766E)
                )
            }
        }
    }

    // Interval Dialog
    if (showIntervalDialog) {
        val options = listOf("Kapalı", "1 dakika", "5 dakika", "10 dakika", "15 dakika")
        val currentLabel = if (!uiState.enabled) "Kapalı" else "${uiState.intervalMinutes} dakika"

        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = {
                Text(
                    text = "Sorgulama Sıklığı Seçin",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (option == "Kapalı") {
                                        onSaveSettings(false, uiState.intervalMinutes, uiState.minSystemMagnitude)
                                    } else {
                                        val minutes = option.split(" ")[0].toIntOrNull() ?: 5
                                        onSaveSettings(true, minutes, uiState.minSystemMagnitude)
                                    }
                                    showIntervalDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = if (option == "Kapalı") !uiState.enabled else (uiState.enabled && option.startsWith("${uiState.intervalMinutes} ")),
                                onClick = {
                                    if (option == "Kapalı") {
                                        onSaveSettings(false, uiState.intervalMinutes, uiState.minSystemMagnitude)
                                    } else {
                                        val minutes = option.split(" ")[0].toIntOrNull() ?: 5
                                        onSaveSettings(true, minutes, uiState.minSystemMagnitude)
                                    }
                                    showIntervalDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0F766E))
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("İptal", color = Color(0xFF0F766E), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Magnitude Dialog
    if (showMagnitudeDialog) {
        val options = listOf(2.0, 3.0, 4.0, 5.0)

        AlertDialog(
            onDismissRequest = { showMagnitudeDialog = false },
            title = {
                Text(
                    text = "Minimum Eşik Değeri Seçin",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSaveSettings(uiState.enabled, uiState.intervalMinutes, option)
                                    showMagnitudeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.minSystemMagnitude == option,
                                onClick = {
                                    onSaveSettings(uiState.enabled, uiState.intervalMinutes, option)
                                    showMagnitudeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0F766E))
                            )
                            Text(
                                text = "$option ve üzeri",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMagnitudeDialog = false }) {
                    Text("İptal", color = Color(0xFF0F766E), fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun StitchConfigClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = KalkanTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
