package com.kalkan.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.model.EmergencyBloodTypes
import com.kalkan.app.model.EmergencyProfile
import com.kalkan.app.ui.components.AppTopNotificationCenter
import com.kalkan.app.util.EmergencyIntentHelper
import com.kalkan.app.viewmodel.EmergencyProfileUiState

const val EMERGENCY_PROFILE_PRIVACY_NOTICE =
    "Bu bilgiler yalnızca acil durumlarda size yardımcı olmak amacıyla saklanır. " +
        "Aile üyeleri, yöneticiler veya diğer kullanıcılarla paylaşılmaz. " +
        "Dilediğiniz zaman güncelleyebilir veya silebilirsiniz."

@Composable
fun EmergencyProfileViewScreen(
    uiState: EmergencyProfileUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onClearMessages: () -> Unit,
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            AppTopNotificationCenter.showSuccess(it)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onClearMessages()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Acil Durum Kartını Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Acil Durum Kartınızı silmek istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanRed),
                ) { Text("Evet, Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KalkanBlue)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    EmergencyProfileTopBar(
                        title = "Acil Durum Kartı",
                        onBackClick = onBackClick,
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Kan Grubu",
                                style = MaterialTheme.typography.labelLarge,
                                color = KalkanTextMuted,
                            )
                            Text(
                                text = uiState.profile?.bloodType?.takeIf { it.isNotBlank() }
                                    ?: EmergencyBloodTypes.UNKNOWN,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = KalkanRed,
                                textAlign = TextAlign.Center,
                            )
                            if (!uiState.profile?.fullName.isNullOrBlank()) {
                                Text(
                                    text = uiState.profile?.fullName.orEmpty(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    val profile = uiState.profile
                    if (profile == null || !profile.hasAnyData) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Henüz Acil Durum Kartı oluşturmadınız. Düzenle ile bilgilerinizi ekleyebilirsiniz.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = KalkanTextMuted,
                            )
                        }
                    } else {
                        EmergencyProfileInfoCard(profile = profile)
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = KalkanBlue.copy(alpha = 0.08f),
                        ),
                        border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = EMERGENCY_PROFILE_PRIVACY_NOTICE,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                        )
                    }

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Düzenle", fontWeight = FontWeight.Bold)
                    }

                    if (profile?.hasAnyData == true) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isDeleting,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, KalkanRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanRed),
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = KalkanRed,
                                )
                            } else {
                                Text("Acil Durum Kartını Sil", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmergencyProfileInfoCard(profile: EmergencyProfile) {
    val context = LocalContext.current
    val phone = profile.primaryContactPhone

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
            ProfileInfoRow(label = "Alerjiler", value = profile.allergies)
            ProfileInfoRow(label = "Kronik Hastalıklar", value = profile.chronicDiseases)
            ProfileInfoRow(label = "Kullanılan İlaçlar", value = profile.medications)
            ProfileInfoRow(label = "Acil Not", value = profile.emergencyNote)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Acil İletişim", color = KalkanTextMuted, fontSize = 13.sp)
                Text(
                    text = profile.primaryContactName.ifBlank { "Belirtilmedi" },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                )
                Text(
                    text = if (phone.isBlank()) "Belirtilmedi" else formatPhoneDisplay(phone),
                    color = if (phone.isBlank()) KalkanTextMuted else Color(0xFF0F766E),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                if (phone.isNotBlank()) {
                    Button(
                        onClick = {
                            if (!EmergencyIntentHelper.openDialer(context, phone)) {
                                Toast.makeText(context, "Arama başlatılamadı.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Ara", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, color = KalkanTextMuted, fontSize = 13.sp)
        Text(
            text = value.ifBlank { "Belirtilmedi" },
            color = if (value.isBlank()) KalkanTextMuted else MaterialTheme.colorScheme.primary,
            fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}

@Composable
internal fun EmergencyProfileTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun formatPhoneDisplay(phone: String): String {
    if (phone.length != 10) return phone
    return "${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6, 8)} ${phone.substring(8)}"
}
