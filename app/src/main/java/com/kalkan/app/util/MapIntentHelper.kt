package com.kalkan.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MapIntentHelper {
    fun openLocation(context: Context, latitude: Double, longitude: Double): Boolean {
        val uri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            false
        }
    }
}
