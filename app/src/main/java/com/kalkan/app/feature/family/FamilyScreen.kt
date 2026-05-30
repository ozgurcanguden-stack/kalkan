package com.kalkan.app.feature.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanGreen
import com.kalkan.app.core.design.theme.KalkanTextMuted
import androidx.compose.ui.platform.LocalContext
import com.kalkan.app.model.EmergencyContact
import com.kalkan.app.model.EmergencyContactRelations
import com.kalkan.app.model.FamilyGroup
import com.kalkan.app.model.FamilyMember
import com.kalkan.app.model.FamilyRole
import com.kalkan.app.model.SafetyStatusType
import com.kalkan.app.ui.components.EmergencyContactCard
import com.kalkan.app.util.EmergencyIntentHelper
import com.kalkan.app.util.WhatsAppOpenResult
import com.kalkan.app.viewmodel.AddEmergencyContactFormState
import com.kalkan.app.viewmodel.EmergencyContactsUiState
import com.kalkan.app.viewmodel.FamilyGroupUiState

private val PrimaryContainer = Color(0xFF131B2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    contactsState: EmergencyContactsUiState,
    familyGroupState: FamilyGroupUiState,
    currentUserUid: String,
    onAddContactClick: () -> Unit,
    onDeleteContact: (String) -> Unit,
    onDismissAddSheet: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onPrimaryChange: (Boolean) -> Unit,
    onSaveContact: () -> Unit,
    onDismissMessage: () -> Unit,
    onShowActionMessage: (String) -> Unit,
    onCreateFamilyGroup: (String) -> Unit,
    onJoinFamilyGroup: (String) -> Unit,
    onClearFamilyError: () -> Unit,
    onClearFamilySuccessMessage: () -> Unit,
    onLeaveFamilyGroup: (String) -> Unit,
    onDeleteFamilyGroup: (String) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val emergencyMessage = contactsState.emergencyMessage

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
        snackbarHostState.showSnackbar(message)
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
            SafetyCheckCard()
            EmergencyContactsSection(
                state = contactsState,
                onAddContactClick = onAddContactClick,
                onDeleteContact = onDeleteContact,
                onCallContact = { handleCall(it) },
                onSmsContact = { handleSms(it) },
                onWhatsAppContact = { handleWhatsApp(it) },
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
                familyGroupState.hasGroup -> {
                    FamilyMembersSection(
                        familyGroup = familyGroupState.familyGroup,
                        members = familyGroupState.members,
                        currentUserUid = currentUserUid,
                        isActionLoading = familyGroupState.isActionLoading,
                        onLeaveGroup = onLeaveFamilyGroup,
                        onDeleteGroup = onDeleteFamilyGroup
                    )
                }
                else -> {
                    CreateOrJoinGroupSection(
                        onCreateGroup = onCreateFamilyGroup,
                        onJoinGroup = onJoinFamilyGroup,
                        isActionLoading = familyGroupState.isActionLoading,
                        actionError = familyGroupState.error,
                        onClearError = onClearFamilyError
                    )
                }
            }

            FamilyMapPreview()
            Spacer(modifier = Modifier.height(12.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    if (contactsState.showAddSheet) {
        AddEmergencyContactSheet(
            form = contactsState.form,
            formError = contactsState.formError,
            isSaving = contactsState.isSaving,
            onDismiss = onDismissAddSheet,
            onNameChange = onNameChange,
            onPhoneChange = onPhoneChange,
            onRelationChange = onRelationChange,
            onPrimaryChange = onPrimaryChange,
            onSaveContact = onSaveContact,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEmergencyContactSheet(
    form: AddEmergencyContactFormState,
    formError: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onPrimaryChange: (Boolean) -> Unit,
    onSaveContact: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var relationExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Acil Kişi Ekle",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ad Soyad") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = form.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Telefon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenuBox(
                expanded = relationExpanded,
                onExpandedChange = { relationExpanded = !relationExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = form.relation,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Yakınlık") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationExpanded) },
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(
                    expanded = relationExpanded,
                    onDismissRequest = { relationExpanded = false },
                ) {
                    EmergencyContactRelations.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onRelationChange(option)
                                relationExpanded = false
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Birincil kişi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Switch(checked = form.isPrimary, onCheckedChange = onPrimaryChange)
            }
            if (formError != null) {
                Text(
                    text = formError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onSaveContact,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Kaydet", fontWeight = FontWeight.Bold)
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(initial = "K")
            Text("Ailem", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Rounded.Settings, contentDescription = "Ayarlar", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SafetyCheckCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Herkes Güvende mi?", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("Hızlı durum kontrolü başlat", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBEC6E0))
            }
            Box(modifier = Modifier.size(48.dp).background(KalkanBlue, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = Color.White)
            }
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
                    label = { Text("Grup Adı") },
                    placeholder = { Text("Örn: Özgür Ailesi") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { onCreateGroup(groupName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActionLoading && groupName.isNotBlank(),
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
                    label = { Text("Davet Kodu") },
                    placeholder = { Text("Örn: KAL482") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { onJoinGroup(inviteCode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActionLoading && inviteCode.isNotBlank(),
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
                FamilyMemberCard(member = member)
            }
        }
    }
}

@Composable
private fun FamilyMemberCard(member: FamilyMember) {
    val relativeTime = if (member.lastStatusAt != null && member.lastStatusAt > 0) {
        android.text.format.DateUtils.getRelativeTimeSpanString(
            member.lastStatusAt,
            System.currentTimeMillis(),
            android.text.format.DateUtils.MINUTE_IN_MILLIS
        ).toString()
    } else {
        ""
    }

    val statusType = SafetyStatusType.from(member.lastStatusType)

    val statusText = when (statusType) {
        SafetyStatusType.SAFE -> "Güvende"
        SafetyStatusType.NEED_HELP -> "Yardım İstiyor"
        SafetyStatusType.SHARE_LOCATION -> "Konum Paylaştı"
        SafetyStatusType.SOS -> "SOS Çağrısı"
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
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
                    Avatar(initial = member.displayName.firstOrNull()?.toString() ?: "U", size = 48)
                    
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

                // Bottom Info: Location & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
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

                    if (relativeTime.isNotBlank()) {
                        Text(
                            text = relativeTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = KalkanTextMuted,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyMapPreview() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFE8EEF7), Color(0xFFDDE7F2)))),
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(Color.White.copy(alpha = 0.9f), CircleShape),
            ) {
                Icon(Icons.Rounded.Fullscreen, contentDescription = null)
            }
            Box(
                modifier = Modifier.align(Alignment.Center).offset(x = (-36).dp, y = 8.dp).size(34.dp).background(Color(0xFF137333), CircleShape).border(BorderStroke(2.dp, Color.White), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("E", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(14.dp).background(Color.White.copy(alpha = 0.92f), CircleShape).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = KalkanBlue, modifier = Modifier.size(16.dp))
                Text("Haritada Gör", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Avatar(initial: String, size: Int = 40) {
    Box(
        modifier = Modifier.size(size.dp).background(Color(0xFFE6E8EA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (initial == "K") {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = KalkanTextMuted)
        } else {
            Text(initial, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
