package com.kalkan.app.model

enum class AnnouncementPriority(val value: String, val label: String) {
    NORMAL("normal", "Normal"),
    IMPORTANT("important", "Onemli"),
    URGENT("urgent", "Acil"),
    ;

    companion object {
        fun from(value: String?): AnnouncementPriority =
            entries.firstOrNull { it.value == value } ?: NORMAL
    }
}
