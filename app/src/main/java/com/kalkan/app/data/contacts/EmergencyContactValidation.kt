package com.kalkan.app.data.contacts

import com.kalkan.app.model.EmergencyContact
import com.kalkan.app.model.EmergencyContactRelations

const val MAX_EMERGENCY_CONTACTS = 10

fun String.normalizedPhoneDigits(): String = filter { it.isDigit() }

fun validateEmergencyContact(
    name: String,
    phone: String,
    relation: String,
    existingContacts: List<EmergencyContact>,
    editingContactId: String? = null,
): String? {
    val trimmedName = name.trim()
    val phoneDigits = phone.normalizedPhoneDigits()

    if (trimmedName.isEmpty()) {
        return "Ad Soyad boş olamaz."
    }
    if (phoneDigits.isEmpty()) {
        return "Telefon boş olamaz."
    }
    if (phoneDigits.length < 10) {
        return "Telefon numarası 10 hane olmalıdır."
    }
    if (phoneDigits.length > 10) {
        return "Telefon numarası 10 haneden fazla olamaz."
    }
    if (!phoneDigits.startsWith("5")) {
        return "Telefon numarası 5 ile başlamalıdır."
    }
    if (relation.isBlank()) {
        return "Yakınlık seçilmelidir."
    }
    if (relation !in EmergencyContactRelations.options) {
        return "Geçerli bir yakınlık seçin."
    }

    val duplicate = existingContacts.any {
        it.id != editingContactId && it.phone.normalizedPhoneDigits() == phoneDigits
    }
    if (duplicate) {
        return "Bu telefon numarası zaten kayıtlı."
    }

    if (editingContactId == null && existingContacts.size >= MAX_EMERGENCY_CONTACTS) {
        return "En fazla $MAX_EMERGENCY_CONTACTS acil kişi ekleyebilirsiniz."
    }

    return null
}
