package com.kalkan.app.data.safety

import android.util.Log

const val SAFETY_STATUS_SAVE_USER_MESSAGE =
    "Durum kaydedilemedi. Lütfen tekrar deneyin."

const val SAFETY_STATUS_AUTH_USER_MESSAGE =
    "Durum kaydetmek için giriş yapmalısınız."

private const val LOG_TAG = "SafetyStatusRepository"

fun logSafetyStatusError(operation: String, error: Throwable) {
    Log.e(LOG_TAG, "Safety status operation failed: $operation", error)
}
