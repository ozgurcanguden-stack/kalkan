package com.zgrcan.kalkan.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.zgrcan.kalkan.model.UserLocation

object EmergencyIntentHelper {
    const val EMERGENCY_NUMBER_112 = "112"

    const val DEFAULT_EMERGENCY_MESSAGE =
        "Kalkan - Afet ve Acil Durum Platformu \nüzerinden ACİL DURUM BİLDİRİMİ gönderiyorum. Lütfen benimle iletişime geç."

    fun buildEmergencyMessage(location: UserLocation?): String {
        val latitude = location?.latitude
        val longitude = location?.longitude
        if (latitude == null || longitude == null) {
            return DEFAULT_EMERGENCY_MESSAGE
        }
        return "$DEFAULT_EMERGENCY_MESSAGE\n\nKonumum:\nhttps://maps.google.com/?q=$latitude,$longitude"
    }

    fun openEmergency112Dialer(context: Context): Boolean = openDialer(context, EMERGENCY_NUMBER_112)

    fun openDialer(context: Context, phone: String): Boolean {
        val formatted = PhoneNumberUtils.cleanForIntent(phone)
        if (formatted.isBlank()) return false

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formatted"))
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openSms(context: Context, phone: String, message: String): Boolean {
        val formatted = PhoneNumberUtils.cleanForIntent(phone)
        if (formatted.isBlank()) return false

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$formatted")).apply {
            putExtra("sms_body", message)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openWhatsApp(context: Context, phone: String, message: String): WhatsAppOpenResult {
        val formatted = PhoneNumberUtils.cleanForWhatsApp(phone)
        if (formatted.isBlank()) return WhatsAppOpenResult.InvalidPhone

        val uri = Uri.parse(
            "https://wa.me/$formatted?text=${Uri.encode(message)}",
        )

        // 1. Önce com.whatsapp paketiyle dene.
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
            return WhatsAppOpenResult.Opened
        } catch (_: Exception) {
            // Devam et
        }

        // 2. Olmazsa com.whatsapp.w4b ile dene.
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp.w4b")
            }
            context.startActivity(intent)
            return WhatsAppOpenResult.Opened
        } catch (_: Exception) {
            // Devam et
        }

        // 3. İkisi de olmazsa packagesız ACTION_VIEW ile dene.
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            WhatsAppOpenResult.Opened
        } catch (_: Exception) {
            // Yine açılmazsa “WhatsApp açılamadı.” mesajı göster
            WhatsAppOpenResult.Failed
        }
    }
}

sealed interface WhatsAppOpenResult {
    data object Opened : WhatsAppOpenResult

    data object NotInstalled : WhatsAppOpenResult

    data object InvalidPhone : WhatsAppOpenResult

    data object Failed : WhatsAppOpenResult
}
