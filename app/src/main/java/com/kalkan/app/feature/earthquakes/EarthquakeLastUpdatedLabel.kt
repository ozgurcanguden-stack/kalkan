package com.kalkan.app.feature.earthquakes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanTextMuted

@Composable
fun EarthquakeLastUpdatedLabel(
    lastUpdatedAt: Long?,
    modifier: Modifier = Modifier,
) {
    if (lastUpdatedAt == null) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = null,
            tint = KalkanTextMuted,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Son güncelleme: ${lastUpdatedAt.formatLastUpdatedAt()}",
            style = MaterialTheme.typography.bodySmall,
            color = KalkanTextMuted,
        )
    }
}
