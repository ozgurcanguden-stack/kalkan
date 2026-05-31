package com.zgrcan.kalkan.model

data class EmergencyProfile(
    val fullName: String = "",
    val bloodType: String = EmergencyBloodTypes.UNKNOWN,
    val allergies: String = "",
    val chronicDiseases: String = "",
    val medications: String = "",
    val emergencyNote: String = "",
    val primaryContactName: String = "",
    val primaryContactPhone: String = "",
    val updatedAt: Long = 0L,
) {
    val hasAnyData: Boolean
        get() = fullName.isNotBlank() ||
            bloodType != EmergencyBloodTypes.UNKNOWN ||
            allergies.isNotBlank() ||
            chronicDiseases.isNotBlank() ||
            medications.isNotBlank() ||
            emergencyNote.isNotBlank() ||
            primaryContactName.isNotBlank() ||
            primaryContactPhone.isNotBlank()
}

object EmergencyBloodTypes {
    const val UNKNOWN = "Bilinmiyor"
    const val DOCUMENT_ID = "current"

    val options: List<String> = listOf(
        UNKNOWN,
        "A+",
        "A-",
        "B+",
        "B-",
        "AB+",
        "AB-",
        "0+",
        "0-",
    )
}
