package com.kalkan.app.feature.earthquakes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.South
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanTextMuted

private val SurfaceVariant = Color(0xFFE0E3E5)
private val TertiaryFixed = Color(0xFFFCDEB5)
private val OnTertiaryFixed = Color(0xFF271901)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

@Composable
fun EarthquakesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        EarthquakesTopBar()
        SearchAndFilter()
        QuickFilters()
        EarthquakeList()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EarthquakesTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = KalkanTextMuted,
                )
            }
            Text(
                text = "Son Depremler",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Ayarlar",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchAndFilter() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = {},
            shape = CircleShape,
            border = BorderStroke(0.dp, Color.Transparent),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Filtrele",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchField(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = KalkanTextMuted,
        )
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
            ),
            singleLine = true,
            cursorBrush = SolidColor(KalkanBlue),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Bölge veya şehir ara...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KalkanTextMuted.copy(alpha = 0.62f),
                    )
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun QuickFilters() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(text = "Tümü", selected = true)
        FilterChip(text = "M 4.0+")
        FilterChip(text = "Son 24 Saat")
        FilterChip(text = "Yakınımdakiler")
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean = false,
) {
    val background = if (selected) KalkanBlue else MaterialTheme.colorScheme.surface
    val content = if (selected) Color.White else KalkanTextMuted

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .then(Modifier)
            .padding(0.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .background(background, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}

@Composable
private fun EarthquakeList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EarthquakeCard(
            magnitude = "5.2",
            type = "Mw",
            location = "Kahramanmaraş, Pazarcık",
            time = "14:23 • Bugün",
            depth = "7.0 km",
            badgeColor = ErrorContainer,
            badgeTextColor = OnErrorContainer,
        )
        EarthquakeCard(
            magnitude = "4.1",
            type = "Mw",
            location = "Malatya, Yeşilyurt",
            time = "10:05 • Bugün",
            depth = "5.2 km",
            badgeColor = TertiaryFixed,
            badgeTextColor = OnTertiaryFixed,
        )
        EarthquakeCard(
            magnitude = "3.4",
            type = "Ml",
            location = "Hatay, Antakya",
            time = "08:42 • Bugün",
            depth = "11.4 km",
            badgeColor = SurfaceVariant,
            badgeTextColor = MaterialTheme.colorScheme.primary,
        )
        EarthquakeCard(
            magnitude = "2.8",
            type = "Ml",
            location = "İzmir, Buca",
            time = "22:15 • Dün",
            depth = "8.1 km",
            badgeColor = SurfaceVariant,
            badgeTextColor = MaterialTheme.colorScheme.primary,
        )
        TextButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Daha Fazla Göster",
                style = MaterialTheme.typography.labelLarge,
                color = KalkanBlue,
            )
        }
    }
}

@Composable
private fun EarthquakeCard(
    magnitude: String,
    type: String,
    location: String,
    time: String,
    depth: String,
    badgeColor: Color,
    badgeTextColor: Color,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .size(64.dp)
                    .background(badgeColor, RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = magnitude,
                    style = MaterialTheme.typography.headlineSmall,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeTextColor.copy(alpha = 0.78f),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailText(icon = Icons.Rounded.Schedule, text = time)
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(KalkanBorder, CircleShape),
                    )
                    DetailText(icon = Icons.Rounded.South, text = depth)
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = KalkanBorder,
            )
        }
    }
}

@Composable
private fun DetailText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = KalkanTextMuted,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = KalkanTextMuted,
            maxLines = 1,
        )
    }
}
