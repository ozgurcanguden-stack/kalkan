package com.kalkan.app.feature.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted

private val MapDark = Color(0xFF0F172A)
private val MapPanel = Color(0xFF111827)
private val MapLine = Color(0xFF334155)

@Composable
fun MapScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MapDark),
    ) {
        MapCanvas()
        MapTopControls()
        MapMarkers()
        FloatingMapActions()
        MapBottomSheet(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun MapCanvas() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF172033), Color(0xFF0F172A)),
                ),
            ),
    ) {
        repeat(8) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = (90 * index).dp)
                    .background(MapLine.copy(alpha = 0.28f)),
            )
        }
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxSize()
                    .offset(x = (80 * index).dp)
                    .background(MapLine.copy(alpha = 0.18f)),
            )
        }
    }
}

@Composable
private fun MapTopControls() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE6111827), CircleShape)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Konum, deprem veya toplanma alanı ara...",
                modifier = Modifier.weight(1f),
                color = Color(0xFFCBD5E1),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KalkanBlue, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, tint = Color.White)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MapChip("Depremler", Icons.Rounded.LocationOn, selected = true)
            MapChip("Toplanma Alanları", Icons.Rounded.Groups)
            MapChip("Hastaneler", Icons.Rounded.Map)
        }
    }
}

@Composable
private fun MapChip(
    text: String,
    icon: ImageVector,
    selected: Boolean = false,
) {
    Row(
        modifier = Modifier
            .background(if (selected) Color.White else Color(0xE61F2937), CircleShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = if (selected) 0f else 0.1f)), CircleShape)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (selected) Color.Black else Color.White,
        )
        Text(text = text, color = if (selected) Color.Black else Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MapMarkers() {
    Box(modifier = Modifier.fillMaxSize()) {
        EarthquakeMarker("5.8", KalkanRed, modifier = Modifier.align(Alignment.Center).offset(x = 24.dp, y = -42.dp))
        EarthquakeMarker("3.2", Color(0xFF64748B), modifier = Modifier.align(Alignment.CenterStart).offset(x = 92.dp, y = 70.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-72).dp, y = -24.dp)
                .size(48.dp)
                .background(KalkanBlue, CircleShape)
                .border(BorderStroke(4.dp, Color.White), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun EarthquakeMarker(
    magnitude: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .background(color, CircleShape)
            .border(BorderStroke(2.dp, Color.White), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = magnitude, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FloatingMapActions() {
    Column(
        modifier = Modifier
            .padding(end = 24.dp, bottom = 190.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        FloatingMapButton(Icons.Rounded.Layers)
        Spacer(modifier = Modifier.height(12.dp))
        FloatingMapButton(Icons.Rounded.MyLocation, tint = Color(0xFFBFDBFE))
    }
}

@Composable
private fun FloatingMapButton(
    icon: ImageVector,
    tint: Color = Color.White,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xE6111827), CircleShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun MapBottomSheet(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 48.dp, height = 5.dp)
                    .background(Color(0xFFD1D5DB), CircleShape),
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("YENİ", color = Color.White, modifier = Modifier.background(KalkanRed, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                        Text("12 dk önce", color = KalkanTextMuted)
                    }
                    Text("Marmara Denizi - Silivri Açıkları", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Column(
                    modifier = Modifier
                        .background(KalkanRed, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("5.8", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Büyüklük", color = Color.White.copy(alpha = 0.82f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MapInfoBox("Derinlik", "12.4 km", modifier = Modifier.weight(1f))
                MapInfoBox("Uzaklık", "45 km", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Paylaş")
                }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = CircleShape, border = BorderStroke(2.dp, KalkanBlue), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Icon(Icons.Rounded.Directions, contentDescription = null, tint = KalkanBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Yol Tarifi", color = KalkanBlue)
                }
            }
        }
    }
}

@Composable
private fun MapInfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(label, color = KalkanTextMuted)
        Text(value, color = Color(0xFF111827), fontWeight = FontWeight.Bold)
    }
}
