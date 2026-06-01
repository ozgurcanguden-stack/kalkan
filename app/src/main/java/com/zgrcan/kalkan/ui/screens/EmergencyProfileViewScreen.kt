package com.zgrcan.kalkan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.model.EmergencyBloodTypes
import com.zgrcan.kalkan.model.EmergencyProfile
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.util.EmergencyIntentHelper
import com.zgrcan.kalkan.util.GuestFeatureMessages
import com.zgrcan.kalkan.viewmodel.EmergencyProfileUiState

const val EMERGENCY_PROFILE_PRIVACY_NOTICE =
    "Bu bilgiler yalnızca acil durumlarda size yardımcı olmak amacıyla saklanır. " +
        "Aile üyeleri, yöneticiler veya diğer kullanıcılarla paylaşılmaz. " +
        "Dilediğiniz zaman güncelleyebilir veya silebilirsiniz."

@Composable
fun EmergencyProfileViewScreen(
    uiState: EmergencyProfileUiState,
    isGuest: Boolean = false,
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
                        actions = {
                            val hasCardData = uiState.profile?.hasAnyData == true
                            if (hasCardData) {
                                IconButton(
                                    onClick = {
                                        if (isGuest) {
                                            AppTopNotificationCenter.showSuccess(
                                                GuestFeatureMessages.SIGN_IN_REQUIRED,
                                            )
                                        } else {
                                            onEditClick()
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Düzenle",
                                        tint = KalkanBlue,
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = !uiState.isDeleting,
                                ) {
                                    if (uiState.isDeleting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = KalkanRed,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Sil",
                                            tint = KalkanRed,
                                        )
                                    }
                                }
                            }
                        },
                    )

                    when (val profile = uiState.profile?.takeIf { it.hasAnyData }) {
                        null -> EmergencyProfileEmptyCard(
                            isGuest = isGuest,
                            onCreateClick = onEditClick,
                        )
                        else -> {
                            EmergencyProfileHeroCard(profile = profile)
                            EmergencyProfileInfoCard(profile = profile)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = KalkanBlue.copy(alpha = 0.08f),
                        ),
                        border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = KalkanBlue,
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.Top),
                            )
                            Text(
                                text = EMERGENCY_PROFILE_PRIVACY_NOTICE,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyProfileHeroCard(profile: EmergencyProfile) {
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
            val bloodTypeDisplay = when {
                profile.bloodType.isBlank() || profile.bloodType == EmergencyBloodTypes.UNKNOWN -> "Belirtilmedi"
                else -> profile.bloodType
            }
            Text(
                text = bloodTypeDisplay,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KalkanRed,
                textAlign = TextAlign.Center,
            )
            if (profile.fullName.isNotBlank()) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EmergencyProfileEmptyCard(
    isGuest: Boolean,
    onCreateClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(KalkanBlue.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MedicalServices,
                    contentDescription = null,
                    tint = KalkanBlue,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = "Henüz acil durum kartınız yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Kan grubu, alerji ve acil iletişim bilgilerinizi ekleyerek kartınızı oluşturun.",
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
            )
            OutlinedButton(
                onClick = {
                    if (isGuest) {
                        AppTopNotificationCenter.showSuccess(GuestFeatureMessages.SIGN_IN_REQUIRED)
                    } else {
                        onCreateClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanBlue),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "KART OLUŞTUR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
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
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

private fun formatPhoneDisplay(phone: String): String {
    if (phone.length != 10) return phone
    return "${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6, 8)} ${phone.substring(8)}"
}
