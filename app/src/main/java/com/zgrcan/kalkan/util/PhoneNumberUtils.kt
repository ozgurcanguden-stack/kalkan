package com.zgrcan.kalkan.util

object PhoneNumberUtils {
    fun cleanForIntent(raw: String): String {
        val filtered = raw.filter { it.isDigit() || it == '+' }
        if (filtered.isBlank()) return filtered

        if (filtered.startsWith("+")) {
            return "+${filtered.drop(1).filter { it.isDigit() }}"
        }

        val digits = filtered.filter { it.isDigit() }
        return when {
            digits.startsWith("90") && digits.length >= 12 -> "+$digits"
            digits.startsWith("0") && digits.length == 11 -> "+90${digits.drop(1)}"
            digits.length == 10 -> "+90$digits"
            else -> digits
        }
    }

    fun cleanForWhatsApp(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return ""

        return when {
            digits.startsWith("90") && digits.length >= 12 -> digits
            digits.startsWith("0") && digits.length == 11 -> "90${digits.drop(1)}"
            digits.length == 10 && digits.startsWith("5") -> "90$digits"
            else -> digits
        }
    }
}
