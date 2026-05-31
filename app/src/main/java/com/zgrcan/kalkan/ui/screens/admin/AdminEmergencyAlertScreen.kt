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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.viewmodel.AdminEmergencyAlertViewModel

@Composable
fun AdminEmergencyAlertScreen(
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: AdminEmergencyAlertViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasAdminAccess by viewModel.hasAdminAccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        val message = uiState.successMessage ?: return@LaunchedEffect
        AppTopNotificationCenter.showSuccess(message)
        viewModel.clearMessages()
        onPublishSuccess()
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessages()
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
                AdminEmergencyAlertTopBar(onBackClick = onBackClick)
                
                Text(
                    text = "Bu uyarı tüm kullanıcılara yüksek öncelikli push bildirimi olarak gönderilecektir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanRed,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Uyarı Başlığı") },
                    placeholder = { Text("Sel Riski ve Tahliye Uyarısı") },
                    supportingText = { Text("${uiState.title.length}/80") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.body,
                    onValueChange = viewModel::onBodyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Uyarı Açıklaması") },
                    placeholder = { Text("Bölgede yoğun yağış nedeniyle risk oluşmuştur. Yetkililerin açıklamalarını takip edin.") },
                    supportingText = { Text("${uiState.body.length}/250") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.region,
                    onValueChange = viewModel::onRegionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Etki Bölgesi / Şehir") },
                    supportingText = { Text("${uiState.region.length}/80") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Öncelik Seviyesi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val priorities = listOf("Bilgilendirme", "Önemli", "Kritik")
                    priorities.forEach { priority ->
                        FilterChip(
                            selected = uiState.priority == priority,
                            onClick = { viewModel.onPrioritySelect(priority) },
                            label = { Text(priority) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (priority == "Kritik") KalkanRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = if (priority == "Kritik") KalkanRed else MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = BorderStroke(1.dp, if (uiState.priority == priority) MaterialTheme.colorScheme.primary else KalkanBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::onPublishClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Yayınla",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = viewModel::onConfirmDismiss,
                title = { Text("Acil uyarı yayınlansın mı?", fontWeight = FontWeight.Bold) },
                text = { Text("Bu uyarı tüm kullanıcılara push bildirimi olarak gönderilecek. Devam etmek istiyor musunuz?") },
                confirmButton = {
                    Button(
                        onClick = viewModel::onConfirmPublish,
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                    ) {
                        Text("Yayınla")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onConfirmDismiss) {
                        Text("İptal", color = KalkanTextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminEmergencyAlertTopBar(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Text(
                text = "Acil Uyarı Yayınla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
