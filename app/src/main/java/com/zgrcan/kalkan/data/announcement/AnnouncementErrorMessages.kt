package com.zgrcan.kalkan.data.announcement

import android.util.Log

const val ANNOUNCEMENT_LOAD_USER_MESSAGE =
    "Duyurular şu anda yüklenemiyor. Lütfen daha sonra tekrar deneyin."

const val ANNOUNCEMENT_DELETE_SUCCESS_MESSAGE = "Duyuru silindi."

const val ANNOUNCEMENT_DELETE_FAILURE_MESSAGE =
    "Duyuru silinemedi. Lütfen tekrar deneyin."

private const val LOG_TAG = "AnnouncementRepository"

fun logAnnouncementError(operation: String, error: Throwable) {
    Log.e(LOG_TAG, "Announcement operation failed: $operation", error)
}
