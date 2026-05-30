package com.kalkan.app.feature.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.model.EmergencyContactRelations
import com.kalkan.app.viewmodel.AddEmergencyContactFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEmergencyContactScreen(
    form: AddEmergencyContactFormState,
    formError: String?,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onPrimaryChange: (Boolean) -> Unit,
    onSaveContact: () -> Unit,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AddContactTopBar(onBackClick = onBackClick)

                Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(inputBackground, CircleShape)
                            .border(1.dp, inputBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(42.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .background(KalkanBlue, CircleShape)
                            .border(1.dp, pageBackground, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
                Text(
                    text = "Fotoğraf Ekle",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            EmergencyContactFormLabel(text = "Ad Soyad") {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Örn: Ayşe Yılmaz") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                )
            }

            EmergencyContactFormLabel(text = "Telefon Numarası") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .width(88.dp)
                            .height(56.dp)
                            .background(inputBackground, RoundedCornerShape(10.dp))
                            .border(1.dp, inputBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("+90", color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = mutedText,
                        )
                    }
                    OutlinedTextField(
                        value = form.phone,
                        onValueChange = onPhoneChange,
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

            EmergencyContactFormLabel(text = "Yakınlık Derecesi") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EmergencyContactRelations.options.forEach { option ->
                        FilterChip(
                            selected = form.relation == option,
                            onClick = { onRelationChange(option) },
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = inputBackground,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, inputBorder),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Öncelikli Kişi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Acil durumlarda ilk ulaşılacak kişi",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedText,
                        )
                    }
                    Switch(checked = form.isPrimary, onCheckedChange = onPrimaryChange)
                }
            }
        }

        Surface(
            color = pageBackground,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                if (formError != null) {
                    Text(
                        text = formError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                Button(
                    onClick = onSaveContact,
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
    }
    }
}

@Composable
private fun AddContactTopBar(onBackClick: () -> Unit) {
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
            text = "Kişi Ekle",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmergencyContactFormLabel(
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

private class TurkishPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                append(c)
                if (i == 2 || i == 5 || i == 7) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 5 -> offset + 1
                offset <= 7 -> offset + 2
                offset <= 10 -> offset + 3
                else -> formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 7 -> offset - 1
                offset <= 10 -> offset - 2
                offset <= 14 -> offset - 3
                else -> digits.length
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
