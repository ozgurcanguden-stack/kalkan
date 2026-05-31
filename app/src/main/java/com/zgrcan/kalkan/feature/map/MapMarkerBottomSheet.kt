package com.zgrcan.kalkan.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.South
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeWarningLabel
import com.zgrcan.kalkan.feature.earthquakes.badgeStyle
import com.zgrcan.kalkan.feature.earthquakes.formatEarthquakeDate
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.ui.components.MapLocationCard
import com.zgrcan.kalkan.util.TimeAgoUtils
import com.zgrcan.kalkan.util.formattedMagnitude
import java.util.Locale

@Composable
fun MapMarkerBottomSheet(
    selectedItem: MapSelectedItem,
    onDismiss: () -> Unit,
    onOpenExternalMap: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 48.dp, height = 4.dp)
                    .background(Color(0xFFC6C6CD).copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                    .clickable(onClick = onDismiss),
            )

            when (selectedItem) {
                is MapSelectedItem.EarthquakeItem -> EarthquakeBottomSheetContent(
                    item = selectedItem,
                    onDismiss = onDismiss,
                    onOpenExternalMap = onOpenExternalMap,
                )
                is MapSelectedItem.FamilyItem -> FamilyBottomSheetContent(
                    item = selectedItem,
                    onOpenExternalMap = onOpenExternalMap,
                )
            }
        }
    }
}

@Composable
private fun EarthquakeBottomSheetContent(
    item: MapSelectedItem.EarthquakeItem,
    onDismiss: () -> Unit,
    onOpenExternalMap: (Double, Double) -> Unit,
) {
    val earthquake = item.earthquake
    val badgeStyle = earthquake.badgeStyle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Public, contentDescription = null, tint = KalkanBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = earthquake.dateTime.formatEarthquakeDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = KalkanTextMuted,
                )
            }
            Text(
                text = earthquake.location.ifBlank { "Konum bilgisi yok" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            EarthquakeWarningLabel(earthquake = earthquake)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = "Kapat")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MapDetailTile(
            label = "Büyüklük",
            value = earthquake.formattedMagnitude(),
            modifier = Modifier.weight(1f),
            valueColor = badgeStyle.content,
            valueBackground = badgeStyle.background,
        )
        MapDetailTile(
            label = "Derinlik",
            value = String.format(Locale("tr", "TR"), "%.1f km", earthquake.depth),
            modifier = Modifier.weight(1f),
            leadingIcon = Icons.Rounded.South,
        )
    }

    OpenInGoogleMapsButton(
        onClick = { onOpenExternalMap(item.latitude, item.longitude) },
    )
}

@Composable
private fun FamilyBottomSheetContent(
    item: MapSelectedItem.FamilyItem,
    onOpenExternalMap: (Double, Double) -> Unit,
) {
    val member = item.member
    val statusType = SafetyStatusType.from(member.lastStatusType)

    MapLocationCard(
        member = member,
        onOpenLocationClick = { onOpenExternalMap(item.latitude, item.longitude) },
        openMapButtonLabel = "Google Maps'te Aç",
    )

    if (member.lastStatusAt != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = KalkanTextMuted, modifier = Modifier.size(16.dp))
            Text(
                text = "Son güncelleme: ${TimeAgoUtils.format(member.lastStatusAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
            )
        }
    }

    if (statusType != null) {
        Text(
            text = statusType.defaultMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun MapDetailTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    valueBackground: Color = Color(0xFFF2F4F6),
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(
        modifier = modifier
            .background(valueBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = KalkanTextMuted, modifier = Modifier.size(16.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = KalkanTextMuted)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OpenInGoogleMapsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
    ) {
        Icon(Icons.Rounded.Map, contentDescription = null)
        Text(
            text = "Google Maps'te Aç",
            modifier = Modifier.padding(start = 8.dp),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun MapApiKeyPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF2F4F6)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Map, contentDescription = null, tint = KalkanBlue, modifier = Modifier.size(48.dp))
            Text(
                text = "Harita yapılandırması gerekli",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Google Maps görünümü için local.properties dosyasına MAPS_API_KEY ekleyin.",
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
            )
        }
    }
}
