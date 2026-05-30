package com.kalkan.app.model

enum class SafetyStatusType(
    val value: String,
    val defaultMessage: String,
    val successMessage: String,
) {
    SAFE(
        value = "safe",
        defaultMessage = "İyiyim, güvendeyim.",
        successMessage = "İyiyim durumunuz kaydedildi.",
    ),
    NEED_HELP(
        value = "need_help",
        defaultMessage = "Yardıma ihtiyacım var.",
        successMessage = "Yardım isteğiniz iletildi.",
    ),
    SHARE_LOCATION(
        value = "share_location",
        defaultMessage = "Konumumu paylaşmak istiyorum.",
        successMessage = "Konum paylaşım isteğiniz kaydedildi.",
    ),
    SOS(
        value = "sos",
        defaultMessage = "Acil yardım çağrısı oluşturuldu.",
        successMessage = "SOS çağrınız oluşturuldu.",
    ),
    ;

    val requiresLocationAttempt: Boolean
        get() = this == SHARE_LOCATION || this == SOS

    companion object {
        fun from(value: String?): SafetyStatusType? =
            entries.firstOrNull { it.value == value }
    }
}
