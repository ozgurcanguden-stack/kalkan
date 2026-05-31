package com.zgrcan.kalkan.util

import com.zgrcan.kalkan.domain.model.Earthquake
import java.util.Locale

enum class EarthquakeMagnitudeLevel {
    NORMAL,
    MILD,
    CAUTION,
    CRITICAL,
}

object EarthquakeMagnitudeUtils {

    fun safeMagnitude(value: Double?): Double {
        if (value == null || !value.isFinite() || value < 0) return 0.0
        return value
    }

    fun classify(magnitude: Double): EarthquakeMagnitudeLevel =
        when {
            safeMagnitude(magnitude) >= 5.0 -> EarthquakeMagnitudeLevel.CRITICAL
            safeMagnitude(magnitude) >= 4.0 -> EarthquakeMagnitudeLevel.CAUTION
            safeMagnitude(magnitude) >= 3.0 -> EarthquakeMagnitudeLevel.MILD
            else -> EarthquakeMagnitudeLevel.NORMAL
        }

    fun warningLabel(magnitude: Double): String? =
        when (classify(magnitude)) {
            EarthquakeMagnitudeLevel.CRITICAL -> "Önemli Deprem"
            EarthquakeMagnitudeLevel.CAUTION -> "Dikkat"
            EarthquakeMagnitudeLevel.NORMAL,
            EarthquakeMagnitudeLevel.MILD,
            -> null
        }

    fun formatMagnitude(magnitude: Double): String {
        val safe = safeMagnitude(magnitude)
        return String.format(Locale("tr", "TR"), "%.1f", safe)
    }

    fun filterByMinMagnitude(earthquakes: List<Earthquake>, minMagnitude: Double): List<Earthquake> {
        val safeMin = safeMagnitude(minMagnitude)
        return earthquakes.filter { safeMagnitude(it.magnitude) >= safeMin }
    }
}

fun Earthquake.safeMagnitude(): Double = EarthquakeMagnitudeUtils.safeMagnitude(magnitude)

fun Earthquake.magnitudeLevel(): EarthquakeMagnitudeLevel =
    EarthquakeMagnitudeUtils.classify(safeMagnitude())

fun Earthquake.warningLabel(): String? =
    EarthquakeMagnitudeUtils.warningLabel(safeMagnitude())

fun Earthquake.formattedMagnitude(): String =
    EarthquakeMagnitudeUtils.formatMagnitude(safeMagnitude())
