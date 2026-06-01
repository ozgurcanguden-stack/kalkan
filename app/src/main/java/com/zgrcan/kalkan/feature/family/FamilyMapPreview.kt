package com.zgrcan.kalkan.feature.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.feature.map.familyMapStatusLabel
import com.zgrcan.kalkan.feature.map.hasValidSharedLocation
import com.zgrcan.kalkan.feature.map.markerHue
import com.zgrcan.kalkan.feature.map.withSharedMapLocations
import com.zgrcan.kalkan.model.FamilyMember
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.util.MapsConfig

private val PreviewGradientTop = Color(0xFFE8EEF7)
private val PreviewGradientBottom = Color(0xFFDDE7F2)
private val DefaultMapCenter = LatLng(39.9334, 32.8597)
private const val PreviewHeight = 190
private const val MiniMapPaddingPx = 72

@Composable
fun FamilyMapPreview(
    members: List<FamilyMember>,
    hasGroup: Boolean,
    isLoading: Boolean,
    onOpenFamilyMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locatableMembers = remember(members) { members.withSharedMapLocations() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Aile Haritası",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Aile üyelerinizin konumlarını harita üzerinde görüntüleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = KalkanTextMuted,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFamilyMap),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, KalkanBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PreviewHeight.dp),
            ) {
            when {
                isLoading && hasGroup -> FamilyMapPreviewLoading()
                MapsConfig.isConfigured && hasGroup && locatableMembers.isNotEmpty() -> {
                    FamilyMapMiniMap(
                        members = locatableMembers,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> FamilyMapPreviewPlaceholder(
                    hasGroup = hasGroup,
                    memberCount = members.size,
                    locatableCount = locatableMembers.size,
                    modifier = Modifier.fillMaxSize(),
                )
            }

                FamilyMapPreviewOverlay(onOpenFamilyMap = onOpenFamilyMap)
            }
        }
    }
}

@Composable
private fun FamilyMapPreviewLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(PreviewGradientTop, PreviewGradientBottom)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = KalkanBlue, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun FamilyMapMiniMap(
    members: List<FamilyMember>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DefaultMapCenter, 5.5f)
    }

    LaunchedEffect(members) {
        val positions = members.map {
            LatLng(it.lastStatusLatitude!!, it.lastStatusLongitude!!)
        }
        val update = when (positions.size) {
            1 -> CameraUpdateFactory.newLatLngZoom(positions.first(), 12f)
            else -> {
                val builder = LatLngBounds.builder()
                positions.forEach { builder.include(it) }
                CameraUpdateFactory.newLatLngBounds(builder.build(), MiniMapPaddingPx)
            }
        }
        cameraPositionState.animate(update)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        ),
    ) {
        members.forEach { member ->
            val lat = member.lastStatusLatitude ?: return@forEach
            val lng = member.lastStatusLongitude ?: return@forEach
            val statusType = SafetyStatusType.from(member.lastStatusType)
            Marker(
                state = MarkerState(position = LatLng(lat, lng)),
                title = member.displayName.ifBlank { "Aile üyesi" },
                snippet = statusType.familyMapStatusLabel(),
                icon = BitmapDescriptorFactory.defaultMarker(statusType.markerHue()),
            )
        }
    }
}

@Composable
private fun FamilyMapPreviewPlaceholder(
    hasGroup: Boolean,
    memberCount: Int,
    locatableCount: Int,
    modifier: Modifier = Modifier,
) {
    val subtitle = when {
        !hasGroup -> "Aile grubu oluşturun veya davet kodu ile katılın."
        locatableCount > 0 -> "$locatableCount üyenin konumu haritada gösterilecek."
        memberCount > 0 -> "Henüz paylaşılan konum yok. Üyeler konum paylaştığında burada görünür."
        else -> "Gruptaki üyeler konum paylaştığında önizleme burada görünür."
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(PreviewGradientTop, PreviewGradientBottom)),
        ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = if (hasGroup) Icons.Rounded.Groups else Icons.Rounded.Map,
                contentDescription = null,
                tint = KalkanBlue.copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
                textAlign = TextAlign.Center,
            )
            if (hasGroup && memberCount > 0) {
                Text(
                    text = "$memberCount üye • $locatableCount konum paylaşıyor",
                    style = MaterialTheme.typography.labelMedium,
                    color = KalkanBlue,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.FamilyMapPreviewOverlay(onOpenFamilyMap: () -> Unit) {
    IconButton(
        onClick = onOpenFamilyMap,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .background(Color.White.copy(alpha = 0.92f), CircleShape),
    ) {
        Icon(Icons.Rounded.Fullscreen, contentDescription = "Haritayı aç")
    }

    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(14.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f), CircleShape)
            .clickable(onClick = onOpenFamilyMap)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.MyLocation,
            contentDescription = null,
            tint = KalkanBlue,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Haritayı Aç",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
