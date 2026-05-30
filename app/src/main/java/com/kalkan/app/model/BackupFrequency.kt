package com.kalkan.app.model

enum class BackupFrequency(
    val key: String,
    val label: String,
    val dialogLabel: String = label,
) {
    DAILY("daily", "Günlük"),
    WEEKLY("weekly", "Haftalık"),
    MONTHLY("monthly", "Aylık"),
    MANUAL("manual", "Manuel", "Yalnızca \"Şimdi Yedekle\"ye dokunduğunda"),
    DISABLED("disabled", "Kapalı");

    companion object {
        fun fromKey(key: String?): BackupFrequency =
            entries.firstOrNull { it.key == key } ?: DAILY
    }
}
