package com.zgrcan.kalkan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.South
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeWarningLabel
import com.zgrcan.kalkan.feature.earthquakes.badgeStyle
import com.zgrcan.kalkan.feature.earthquakes.formatEarthquakeDate
import com.zgrcan.kalkan.feature.earthquakes.hasValidCoordinates
import com.zgrcan.kalkan.util.formattedMagnitude
import java.util.Locale

@Composable
fun EarthquakeMapCard(
    earthquake: Earthquake,
    onOpenMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeStyle = earthquake.badgeStyle()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .size(56.dp)
                        .background(badgeStyle.background, RoundedCornerShape(8.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = earthquake.formattedMagnitude(),
                        style = MaterialTheme.typography.titleLarge,
                        color = badgeStyle.content,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = earthquake.title.ifBlank { "ML" },
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeStyle.content.copy(alpha = 0.78f),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = earthquake.location.ifBlank { "Konum bilgisi yok" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    EarthquakeWarningLabel(earthquake = earthquake)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MapDetailChip(
                            icon = Icons.Rounded.Schedule,
                            text = earthquake.dateTime.formatEarthquakeDate(),
                        )
                        MapDetailChip(
                            icon = Icons.Rounded.South,
                            text = String.format(Locale("tr", "TR"), "%.1f km", earthquake.depth),
                        )
                    }
                }
            }
            if (earthquake.hasValidCoordinates()) {
                OutlinedButton(
                    onClick = onOpenMapClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanBlue),
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Haritada Aç", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun MapDetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = KalkanTextMuted)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = KalkanTextMuted)
    }
}
