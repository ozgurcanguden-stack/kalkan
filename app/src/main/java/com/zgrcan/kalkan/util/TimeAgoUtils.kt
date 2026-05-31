package com.zgrcan.kalkan.util

object TimeAgoUtils {
    private const val MINUTE_MILLIS = 60_000L
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun format(timestamp: Long?, now: Long = System.currentTimeMillis()): String {
        if (timestamp == null || timestamp <= 0L) return ""

        val elapsed = (now - timestamp).coerceAtLeast(0L)
        return when {
            elapsed < MINUTE_MILLIS -> "Az önce"
            elapsed < HOUR_MILLIS -> "${elapsed / MINUTE_MILLIS} dk önce"
            elapsed < DAY_MILLIS -> "${elapsed / HOUR_MILLIS} saat önce"
            else -> "${elapsed / DAY_MILLIS} gün önce"
        }
    }
}
