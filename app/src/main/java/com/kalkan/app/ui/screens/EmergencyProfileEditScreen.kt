package com.kalkan.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.util.TurkishPhoneVisualTransformation
import com.kalkan.app.model.EmergencyBloodTypes
import com.kalkan.app.viewmodel.EmergencyProfileFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmergencyProfileEditScreen(
    form: EmergencyProfileFormState,
    formError: String?,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onBloodTypeChange: (String) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onChronicDiseasesChange: (String) -> Unit,
    onMedicationsChange: (String) -> Unit,
    onEmergencyNoteChange: (String) -> Unit,
    onPrimaryContactNameChange: (String) -> Unit,
    onPrimaryContactPhoneChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val pageBackground = if (isDarkTheme) Color(0xFF0F172A) else MaterialTheme.colorScheme.background
    val inputBackground = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    val inputBorder = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else KalkanBorder
    val mutedText = if (isDarkTheme) Color(0xFFCBD5E1) else KalkanTextMuted
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KalkanBlue,
        unfocusedBorderColor = inputBorder,
        focusedContainerColor = inputBackground,
        unfocusedContainerColor = inputBackground,
        focusedLeadingIconColor = mutedText,
        unfocusedLeadingIconColor = mutedText,
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
            .imePadding()
            .imeNestedScroll()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        EmergencyProfileTopBar(title = "Kartı Düzenle", onBackClick = onBackClick)

        Text(
            text = EMERGENCY_PROFILE_PRIVACY_NOTICE,
            style = MaterialTheme.typography.bodySmall,
            color = mutedText,
        )

        FormLabel("Ad Soyad") {
            OutlinedTextField(
                value = form.fullName,
                onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Adınız ve soyadınız") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Kan Grubu") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EmergencyBloodTypes.options.forEach { option ->
                    FilterChip(
                        selected = form.bloodType == option,
                        onClick = { onBloodTypeChange(option) },
                        label = { Text(option, fontWeight = FontWeight.SemiBold) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KalkanBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }

        FormLabel("Alerjiler") {
            OutlinedTextField(
                value = form.allergies,
                onValueChange = onAllergiesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Örn: Penisilin, fıstık") },
                minLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Kronik Hastalıklar") {
            OutlinedTextField(
                value = form.chronicDiseases,
                onValueChange = onChronicDiseasesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Örn: Diyabet, astım") },
                minLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Kullanılan İlaçlar") {
            OutlinedTextField(
                value = form.medications,
                onValueChange = onMedicationsChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Düzenli kullandığınız ilaçlar") },
                minLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Acil Not") {
            OutlinedTextField(
                value = form.emergencyNote,
                onValueChange = onEmergencyNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Acil ekipler için ek bilgi") },
                minLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Acil İletişim Kişisi") {
            OutlinedTextField(
                value = form.primaryContactName,
                onValueChange = onPrimaryContactNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Kişi adı") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
            )
        }

        FormLabel("Acil İletişim Telefonu") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .width(88.dp)
                        .height(56.dp)
                        .background(inputBackground, RoundedCornerShape(10.dp))
                        .border(1.dp, inputBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("+90", color = MaterialTheme.colorScheme.onBackground)
                }
                OutlinedTextField(
                    value = form.primaryContactPhone,
                    onValueChange = onPrimaryContactPhoneChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("5XX XXX XX XX") },
                    leadingIcon = { Icon(Icons.Rounded.Call, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = TurkishPhoneVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                )
            }
        }

        if (formError != null) {
            Text(
                text = formError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FormLabel(
    text: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}
