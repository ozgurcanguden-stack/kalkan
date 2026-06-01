package com.zgrcan.kalkan.data.alert

import android.util.Log

const val EMERGENCY_ALERT_DELETE_SUCCESS_MESSAGE = "Acil uyarı silindi."

const val EMERGENCY_ALERT_DELETE_FAILURE_MESSAGE =
    "Acil uyarı silinemedi.\nLütfen tekrar deneyin."

private const val LOG_TAG = "EmergencyAlertRepository"

fun logEmergencyAlertError(operation: String, error: Throwable) {
    Log.e(LOG_TAG, "Emergency alert operation failed: $operation", error)
}
