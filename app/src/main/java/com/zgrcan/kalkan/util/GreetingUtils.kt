package com.zgrcan.kalkan.util

import java.util.Calendar

object GreetingUtils {
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi Günler"
            in 18..23 -> "İyi Akşamlar"
            else -> "İyi Geceler"
        }
    }
}
