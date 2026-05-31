package com.zgrcan.kalkan.feature.earthquakes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.util.EarthquakeMagnitudeLevel
import com.zgrcan.kalkan.util.magnitudeLevel
import com.zgrcan.kalkan.util.warningLabel

data class EarthquakeBadgeStyle(
    val background: Color,
    val content: Color,
)

fun Earthquake.badgeStyle(): EarthquakeBadgeStyle = magnitudeLevel().toBadgeStyle()

private fun EarthquakeMagnitudeLevel.toBadgeStyle(): EarthquakeBadgeStyle =
    when (this) {
        EarthquakeMagnitudeLevel.CRITICAL -> EarthquakeBadgeStyle(
            background = Color(0xFFFFDAD6),
            content = Color(0xFF93000A),
        )
        EarthquakeMagnitudeLevel.CAUTION -> EarthquakeBadgeStyle(
            background = Color(0xFFFCDEB5),
            content = Color(0xFF271901),
        )
        EarthquakeMagnitudeLevel.MILD -> EarthquakeBadgeStyle(
            background = Color(0xFFE8F5E9),
            content = Color(0xFF2E7D32),
        )
        EarthquakeMagnitudeLevel.NORMAL -> EarthquakeBadgeStyle(
            background = Color(0xFFE0E3E5),
            content = Color(0xFF0F172A),
        )
    }

@Composable
fun EarthquakeWarningLabel(
    earthquake: Earthquake,
    modifier: Modifier = Modifier,
) {
    val label = earthquake.warningLabel() ?: return
    val level = earthquake.magnitudeLevel()
    val colors = level.toWarningChipColors()

    Text(
        text = label,
        modifier = modifier
            .background(colors.first, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = colors.second,
        fontWeight = FontWeight.Bold,
    )
}

private fun EarthquakeMagnitudeLevel.toWarningChipColors(): Pair<Color, Color> =
    when (this) {
        EarthquakeMagnitudeLevel.CRITICAL -> Color(0xFFFCE8E6) to Color(0xFFC5221F)
        EarthquakeMagnitudeLevel.CAUTION -> Color(0xFFFFF4E5) to Color(0xFFB45309)
        EarthquakeMagnitudeLevel.MILD,
        EarthquakeMagnitudeLevel.NORMAL,
        -> Color(0xFFF1F3F4) to Color(0xFF5F6368)
    }
