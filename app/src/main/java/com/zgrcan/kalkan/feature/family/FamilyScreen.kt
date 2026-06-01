package com.zgrcan.kalkan.feature.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanGreen
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import com.zgrcan.kalkan.model.EmergencyContact
import com.zgrcan.kalkan.model.EmergencyContactRelations
import com.zgrcan.kalkan.model.FamilyGroup
import com.zgrcan.kalkan.model.FamilyMember
import com.zgrcan.kalkan.model.FamilyRole
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.ui.components.EmergencyContactCard
import com.zgrcan.kalkan.ui.components.RemoteProfileImage
import com.zgrcan.kalkan.ui.components.AppTopNotificationCenter
import com.zgrcan.kalkan.util.EmergencyIntentHelper
import com.zgrcan.kalkan.util.MapIntentHelper
import com.zgrcan.kalkan.util.TimeAgoUtils
import com.zgrcan.kalkan.util.GuestFeatureMessages
import com.zgrcan.kalkan.util.WhatsAppOpenResult
import com.zgrcan.kalkan.viewmodel.AddEmergencyContactFormState
import com.zgrcan.kalkan.viewmodel.EmergencyContactsUiState
import com.zgrcan.kalkan.viewmodel.FamilyGroupUiState

private val PrimaryContainer = Color(0xFF131B2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    contactsState: EmergencyContactsUiState,
    familyGroupState: FamilyGroupUiState,
    isGuest: Boolean,
    currentUserUid: String,
    onAddContactClick: () -> Unit,
    onDeleteContact: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onShowActionMessage: (String) -> Unit,
    onCreateFamilyGroup: (String) -> Unit,
    onJoinFamilyGroup: (String) -> Unit,
    onClearFamilyError: () -> Unit,
    onClearFamilySuccessMessage: () -> Unit,
    onLeaveFamilyGroup: (String) -> Unit,
    onDeleteFamilyGroup: (String) -> Unit,
    onRequestFamilyStatusCheck: () -> Unit,
    onOpenFamilyMap: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val emergencyMessage = contactsState.emergencyMessage

    var contactToDeleteId by remember { mutableStateOf<String?>(null) }
    var familyGroupToDeleteId by remember { mutableStateOf<String?>(null) }
    var familyGroupToLeaveId by remember { mutableStateOf<String?>(null) }
    var showFamilyCheckDialog by remember { mutableStateOf(false) }

    fun openFamilyCheckDialog() {
        when {
            isGuest -> onShowActionMessage(GuestFeatureMessages.SIGN_IN_REQUIRED)
            !familyGroupState.hasGroup -> {
                onShowActionMessage("Durum kontrolü için önce bir aile grubuna katılın.")
            }
            familyGroupState.members.none { it.uid != currentUserUid && it.uid.isNotBlank() } -> {
                onShowActionMessage("Aile grubunuzda bildirim gönderilecek başka üye bulunmuyor.")
            }
            else -> showFamilyCheckDialog = true
        }
    }

    fun handleCall(contact: EmergencyContact) {
        if (!EmergencyIntentHelper.openDialer(context, contact.phone)) {
            onShowActionMessage("Arama başlatılamadı.")
        }
    }

    fun handleSms(contact: EmergencyContact) {
        if (!EmergencyIntentHelper.openSms(context, contact.phone, emergencyMessage)) {
            onShowActionMessage("SMS uygulaması açılamadı.")
        }
    }

    fun handleWhatsApp(contact: EmergencyContact) {
        when (EmergencyIntentHelper.openWhatsApp(context, contact.phone, emergencyMessage)) {
            WhatsAppOpenResult.Opened -> Unit
            WhatsAppOpenResult.NotInstalled -> onShowActionMessage("WhatsApp açılamadı. Lütfen telefonunuzda WhatsApp kurulu ve güncel olduğundan emin olun.")
            WhatsAppOpenResult.InvalidPhone -> onShowActionMessage("Geçerli bir telefon numarası yok.")
            WhatsAppOpenResult.Failed -> onShowActionMessage("WhatsApp açılamadı.")
        }
    }

    LaunchedEffect(contactsState.snackbarMessage) {
        val message = contactsState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onDismissMessage()
    }

    LaunchedEffect(familyGroupState.actionSuccessMessage) {
        val message = familyGroupState.actionSuccessMessage ?: return@LaunchedEffect
        AppTopNotificationCenter.showSuccess(message)
        onClearFamilySuccessMessage()
    }

    LaunchedEffect(familyGroupState.error) {
        val errorMsg = familyGroupState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(errorMsg)
        onClearFamilyError()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FamilyTopBar()
            SafetyCheckCard(
                onClick = ::openFamilyCheckDialog,
                isLoading = familyGroupState.isActionLoading,
            )

            when {
                familyGroupState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = KalkanBlue, modifier = Modifier.size(28.dp))
                    }
                }
                familyGroupState.familyGroup != null -> {
                    FamilyMembersSection(
                        familyGroup = familyGroupState.familyGroup,
                        members = familyGroupState.members,
                        currentUserUid = currentUserUid,
                        isActionLoading = familyGroupState.isActionLoading,
                        onLeaveGroup = { groupId ->
                            familyGroupToLeaveId = groupId
                        },
                        onDeleteGroup = { groupId ->
                            familyGroupToDeleteId = groupId
                        },
                        onOpenLocation = { latitude, longitude ->
                            if (!MapIntentHelper.openLocation(context, latitude, longitude)) {
                                onShowActionMessage("Konum açılamadı.")
                            }
                        }
                    )
                }
                else -> {
                    FamilyCreateJoinInfoCard()
                    if (isGuest) {
                        GuestSignInHintCard()
                    }
                    CreateOrJoinGroupSection(
                        onCreateGroup = onCreateFamilyGroup,
                        onJoinGroup = onJoinFamilyGroup,
                        isActionLoading = familyGroupState.isActionLoading,
                        actionError = familyGroupState.error,
                        onClearError = onClearFamilyError,
                        formsEnabled = !isGuest,
                    )
                }
            }

            EmergencyContactsSection(
                state = contactsState,
                onAddContactClick = onAddContactClick,
                onDeleteContact = { contactId ->
                    contactToDeleteId = contactId
                },
                onCallContact = { handleCall(it) },
                onSmsContact = { handleSms(it) },
                onWhatsAppContact = { handleWhatsApp(it) },
            )

            FamilyMapPreview(
                members = familyGroupState.members,
                hasGroup = familyGroupState.hasGroup,
                isLoading = familyGroupState.isLoading,
                onOpenFamilyMap = onOpenFamilyMap,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showFamilyCheckDialog) {
            AlertDialog(
                onDismissRequest = { showFamilyCheckDialog = false },
                title = { Text("Durum kontrolü başlatılsın mı?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Aile üyelerinize güvenlik durumlarını paylaşmaları için bildirim gönderilecek.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showFamilyCheckDialog = false
                            onRequestFamilyStatusCheck()
                        },
                        enabled = !familyGroupState.isActionLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
                    ) {
                        if (familyGroupState.isActionLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Gönder")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFamilyCheckDialog = false }) {
                        Text("İptal")
                    }
                },
            )
        }

        if (contactToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { contactToDeleteId = null },
                icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed) },
                title = { Text("Kişiyi Sil", fontWeight = FontWeight.Bold) },
                text = { Text("Seçilen acil durum kişisini rehberinizden silmek istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            contactToDeleteId?.let { onDeleteContact(it) }
                            contactToDeleteId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                    ) {
                        Text("Evet, Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { contactToDeleteId = null }) {
                        Text("İptal")
                    }
                }
            )
        }

        if (familyGroupToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { familyGroupToDeleteId = null },
                icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed) },
                title = { Text("Grubu Sil", fontWeight = FontWeight.Bold) },
                text = { Text("Aile grubunu silmek istediğinize emin misiniz? Bu işlem gruptaki tüm üyeleri gruptan çıkaracaktır ve geri alınamaz.") },
                confirmButton = {
                    Button(
                        onClick = {
                            familyGroupToDeleteId?.let { onDeleteFamilyGroup(it) }
                            familyGroupToDeleteId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                    ) {
                        Text("Evet, Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { familyGroupToDeleteId = null }) {
                        Text("İptal")
                    }
                }
            )
        }

        if (familyGroupToLeaveId != null) {
            AlertDialog(
                onDismissRequest = { familyGroupToLeaveId = null },
                icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = KalkanRed) },
                title = { Text("Gruptan Ayrıl", fontWeight = FontWeight.Bold) },
                text = { Text("Bu aile grubundan ayrılmak istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            familyGroupToLeaveId?.let { onLeaveFamilyGroup(it) }
                            familyGroupToLeaveId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KalkanRed)
                    ) {
                        Text("Evet, Ayrıl")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { familyGroupToLeaveId = null }) {
                        Text("İptal")
                    }
                }
            )
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
private fun EmergencyContactsSection(
    state: EmergencyContactsUiState,
    onAddContactClick: () -> Unit,
    onDeleteContact: (String) -> Unit,
    onCallContact: (EmergencyContact) -> Unit,
    onSmsContact: (EmergencyContact) -> Unit,
    onWhatsAppContact: (EmergencyContact) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Acil Kişiler",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onAddContactClick) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = KalkanBlue,
                    modifier = Modifier.size(18.dp),
                )
                Text("Kişi Ekle", color = KalkanBlue)
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = KalkanBlue, modifier = Modifier.size(28.dp))
                }
            }
            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            state.contacts.isEmpty() -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Henüz kişi eklenmedi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Acil durumlarda ulaşılacak kişileri buradan ekleyebilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KalkanTextMuted,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onAddContactClick,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Kişi Ekle")
                        }
                    }
                }
            }
            else -> {
                state.contacts.forEach { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onDeleteClick = { onDeleteContact(contact.id) },
                        onCallClick = { onCallContact(contact) },
                        onSmsClick = { onSmsContact(contact) },
                        onWhatsAppClick = { onWhatsAppContact(contact) },
                    )
                }
            }
        }
    }
}


@Composable
private fun FamilyTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Ailem", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SafetyCheckCard(
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable(enabled = !isLoading, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Aile Durum Kontrolü",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Yakınlarınızdan güvenlik durumlarını paylaşmalarını isteyin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFBEC6E0),
                )
                Text(
                    text = "Kontrol Başlat",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(KalkanBlue.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(48.dp).background(KalkanBlue, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FamilyCreateJoinInfoCard() {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF3F8FC)
    val borderColor = if (isDark) KalkanBorder.copy(alpha = 0.35f) else KalkanBlue.copy(alpha = 0.12f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Aile grubunuzla afet anında durumunuzu paylaşabilir, yakınlarınızın güvende olup olmadığını hızlıca görebilirsiniz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Grup oluşturabilir veya size verilen davet koduyla mevcut bir gruba katılabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun GuestSignInHintCard() {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) KalkanBlue.copy(alpha = 0.12f) else Color(0xFFE8F0F8)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = GuestFeatureMessages.SIGN_IN_REQUIRED,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = GuestFeatureMessages.FAMILY_SIGN_IN_HINT,
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun CreateOrJoinGroupSection(
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    isActionLoading: Boolean,
    actionError: String?,
    onClearError: () -> Unit,
    formsEnabled: Boolean = true,
) {
    var groupName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Aile Takip ve Durum Merkezi",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Yeni Aile Grubu Oluştur",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ailenizin durumunu takip etmek için yeni bir grup oluşturun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted
                )
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it; onClearError() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formsEnabled,
                    label = { Text("Grup Adı") },
                    placeholder = { Text("Örn: Özgür Ailesi") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { onCreateGroup(groupName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formsEnabled && !isActionLoading && groupName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue)
                ) {
                    Text("Oluştur", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Davet Kodu ile Katıl",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Mevcut bir aile grubuna katılmak için davet kodunu girin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted
                )
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it; onClearError() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formsEnabled,
                    label = { Text("Davet Kodu") },
                    placeholder = { Text("Örn: KAL482") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { onJoinGroup(inviteCode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = formsEnabled && !isActionLoading && inviteCode.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue)
                ) {
                    Text("Katıl", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (actionError != null) {
            Text(
                text = actionError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun FamilyMembersSection(
    familyGroup: FamilyGroup?,
    members: List<FamilyMember>,
    currentUserUid: String,
    isActionLoading: Boolean,
    onLeaveGroup: (String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onOpenLocation: (Double, Double) -> Unit,
) {
    val isOwner = familyGroup?.ownerUid == currentUserUid

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = familyGroup?.name ?: "Aile Üyeleri",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (familyGroup != null) {
                    Text(
                        text = "Davet Kodu: ${familyGroup.inviteCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (familyGroup != null) {
                TextButton(
                    onClick = {
                        if (isOwner) {
                            onDeleteGroup(familyGroup.id)
                        } else {
                            onLeaveGroup(familyGroup.id)
                        }
                    },
                    enabled = !isActionLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC5221F))
                ) {
                    if (isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFFC5221F),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isOwner) "Grubu Sil" else "Gruptan Ayrıl",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (members.isEmpty()) {
            Text(
                text = "Grupta henüz üye bulunmuyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted
            )
        } else {
            members.forEach { member ->
                FamilyMemberCard(member = member, onOpenLocation = onOpenLocation)
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(
    member: FamilyMember,
    onOpenLocation: (Double, Double) -> Unit,
) {
    val relativeTime = TimeAgoUtils.format(member.lastStatusAt)

    val statusType = SafetyStatusType.from(member.lastStatusType)

    val statusText = when (statusType) {
        SafetyStatusType.SAFE -> "İYİYİM"
        SafetyStatusType.NEED_HELP -> "YARDIM İSTİYOR"
        SafetyStatusType.SHARE_LOCATION -> "Konum Paylaştı"
        SafetyStatusType.SOS -> "ACİL SOS"
        null -> "Belirsiz"
    }

    val statusColor = when (statusType) {
        SafetyStatusType.SAFE -> Color(0xFFE6F4EA)
        SafetyStatusType.NEED_HELP -> Color(0xFFFEF7E0)
        SafetyStatusType.SHARE_LOCATION -> Color(0xFFE8F0FE)
        SafetyStatusType.SOS -> Color(0xFFFCE8E6)
        null -> Color(0xFFF1F3F4)
    }

    val statusTextColor = when (statusType) {
        SafetyStatusType.SAFE -> Color(0xFF137333)
        SafetyStatusType.NEED_HELP -> Color(0xFFB06000)
        SafetyStatusType.SHARE_LOCATION -> Color(0xFF1A73E8)
        SafetyStatusType.SOS -> Color(0xFFC5221F)
        null -> Color(0xFF5F6368)
    }

    val accentColor = statusTextColor
    val cardContainerColor = when (statusType) {
        SafetyStatusType.SOS -> Color(0xFFFFF1F1)
        SafetyStatusType.NEED_HELP -> Color(0xFFFFF7ED)
        SafetyStatusType.SAFE -> Color(0xFFF3FAF5)
        SafetyStatusType.SHARE_LOCATION -> Color(0xFFF5F9FF)
        null -> MaterialTheme.colorScheme.surface
    }

    val statusIcon = when (statusType) {
        SafetyStatusType.SAFE -> Icons.Rounded.CheckCircle
        SafetyStatusType.NEED_HELP -> Icons.AutoMirrored.Rounded.Help
        SafetyStatusType.SHARE_LOCATION -> Icons.Rounded.MyLocation
        SafetyStatusType.SOS -> Icons.Rounded.HealthAndSafety
        null -> Icons.Rounded.LocationOff
    }

    val locationText = if (member.lastStatusLatitude != null && member.lastStatusLongitude != null) {
        "Konum: ${member.lastStatusLatitude.toString().take(8)}, ${member.lastStatusLongitude.toString().take(8)}"
    } else {
        "Konum paylaşılmadı"
    }

    val subtitleText = if (member.email != null) {
        "${member.email} • ${if (member.role == FamilyRole.OWNER) "Yönetici" else "Üye"}"
    } else {
        if (member.role == FamilyRole.OWNER) "Yönetici" else "Üye"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Accent Bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Row: Avatar + Name/Details + Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(
                        initial = member.displayName.firstOrNull()?.toString() ?: "U",
                        photoUrl = member.photoUrl,
                        size = 48,
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = KalkanTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Badge
                    Row(
                        modifier = Modifier
                            .background(statusColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status Message (if any)
                val statusMessageText = if (!member.lastStatusMessage.isNullOrBlank()) {
                    member.lastStatusMessage
                } else if (statusType == null) {
                    "Henüz durum paylaşmadı."
                } else {
                    null
                }

                if (statusMessageText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = statusMessageText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom Info: Location, time and map action
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (member.lastStatusLatitude != null && member.lastStatusLongitude != null) Icons.Rounded.LocationOn else Icons.Rounded.LocationOff,
                            contentDescription = null,
                            tint = KalkanTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = KalkanTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (relativeTime.isNotBlank()) {
                            Text(
                                text = "Son güncelleme: $relativeTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = KalkanTextMuted,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        val latitude = member.lastStatusLatitude
                        val longitude = member.lastStatusLongitude
                        if (latitude != null && longitude != null) {
                            TextButton(onClick = { onOpenLocation(latitude, longitude) }) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Konumu Aç")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(initial: String, photoUrl: String? = null, size: Int = 40) {
    RemoteProfileImage(
        photoUrl = photoUrl,
        shape = CircleShape,
        modifier = Modifier.size(size.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFE6E8EA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (initial == "K") {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = KalkanTextMuted)
            } else {
                Text(initial, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

