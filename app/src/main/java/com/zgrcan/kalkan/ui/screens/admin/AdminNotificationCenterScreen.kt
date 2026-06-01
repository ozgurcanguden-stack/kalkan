package com.zgrcan.kalkan.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zgrcan.kalkan.core.design.components.KalkanCard
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.viewmodel.AdminNotificationItem
import com.zgrcan.kalkan.viewmodel.AdminNotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminNotificationCenterScreen(
    hasAdminAccess: Boolean,
    onBackClick: () -> Unit,
    viewModel: AdminNotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSuperAdmin by viewModel.hasAdminAccess.collectAsState()
    val canManageDeletes = hasAdminAccess || isSuperAdmin
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<PendingNotificationDelete?>(null) }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        if (uiState.isSnackbarError) {
            snackbarHostState.showSnackbar(message)
        } else {
            AppTopNotificationCenter.showSuccess(message)
        }
        viewModel.clearSnackbarMessage()
    }

    if (!canManageDeletes) {
        UnauthorizedAdminContent(onBackClick = onBackClick)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AdminNotificationsTopBar(onBackClick = onBackClick)

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KalkanBlue)
                }
            } else if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Henüz bildirim kaydı yok",
                        color = KalkanTextMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.items) { item ->
                        val canDelete = canManageDeletes && item.type in DELETABLE_NOTIFICATION_TYPES
                        NotificationItemCard(
                            item = item,
                            canDelete = canDelete,
                            isDeleting = uiState.isDeletingItem,
                            onDeleteClick = {
                                pendingDelete = when (item.type) {
                                    "Duyuru" -> PendingNotificationDelete.Announcement(item.id)
                                    "Acil Uyarı" -> PendingNotificationDelete.EmergencyAlert(item.id)
                                    else -> null
                                }
                            },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    pendingDelete?.let { deleteTarget ->
        val dialogContent = when (deleteTarget) {
            is PendingNotificationDelete.Announcement -> DeleteDialogContent(
                title = "Duyuru silinsin mi?",
                message = "Bu duyuru uygulamadan kaldırılacak.",
            )
            is PendingNotificationDelete.EmergencyAlert -> DeleteDialogContent(
                title = "Acil uyarı silinsin mi?",
                message = "Bu acil uyarı uygulamadan kaldırılacaktır.\n\nBu işlem geri alınamaz.",
            )
        }

        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(
                    text = dialogContent.title,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = dialogContent.message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (deleteTarget) {
                            is PendingNotificationDelete.Announcement ->
                                viewModel.deleteAnnouncement(deleteTarget.id, canDelete = canManageDeletes)
                            is PendingNotificationDelete.EmergencyAlert ->
                                viewModel.deleteEmergencyAlert(deleteTarget.id, canDelete = canManageDeletes)
                        }
                        pendingDelete = null
                    },
                    enabled = !uiState.isDeletingItem,
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed),
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("İptal")
                }
            },
        )
    }
}

private val DELETABLE_NOTIFICATION_TYPES = setOf("Duyuru", "Acil Uyarı")

private sealed interface PendingNotificationDelete {
    val id: String

    data class Announcement(override val id: String) : PendingNotificationDelete
    data class EmergencyAlert(override val id: String) : PendingNotificationDelete
}

private data class DeleteDialogContent(
    val title: String,
    val message: String,
)

@Composable
private fun AdminNotificationsTopBar(onBackClick: () -> Unit) {
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
                text = "Bildirim Merkezi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Gönderilen tüm push bildirim geçmişi",
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: AdminNotificationItem,
    canDelete: Boolean,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    val icon = when (item.type) {
        "Duyuru" -> Icons.Rounded.Campaign
        "Acil Uyarı" -> Icons.Rounded.Warning
        "Deprem Bildirimi" -> Icons.Rounded.Public
        else -> Icons.Rounded.Notifications
    }

    val tint = when (item.type) {
        "Duyuru" -> KalkanBlue
        "Acil Uyarı" -> KalkanRed
        "Deprem Bildirimi" -> Color(0xFFF59E0B)
        else -> KalkanTextMuted
    }

    val timeString = if (item.timestamp > 0) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(item.timestamp))
    } else {
        ""
    }

    KalkanCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = KalkanTextMuted
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.message.isNotBlank()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = item.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (canDelete) {
                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = when (item.type) {
                            "Acil Uyarı" -> "Acil uyarıyı sil"
                            else -> "Duyuruyu sil"
                        },
                        tint = KalkanRed,
                    )
                }
            }
        }
    }
}
