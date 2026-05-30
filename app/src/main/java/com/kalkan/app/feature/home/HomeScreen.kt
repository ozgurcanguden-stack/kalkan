package com.kalkan.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanGreen
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanSurface
import com.kalkan.app.core.design.theme.KalkanTextMuted

private val SuccessContainer = Color(0xFFC4EED0)
private val OnSuccessContainer = Color(0xFF072711)
private val QuietSurface = Color(0xFFFFFFFF)
private val SurfaceVariant = Color(0xFFE0E3E5)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KalkanSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TopGreetingBar()
        StatusCard()
        EmergencyActionGrid()
        RecentEarthquakesCard()
        EmergencyContactsCard()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TopGreetingBar() {
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
                    .background(SurfaceVariant, CircleShape)
                    .border(BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = KalkanTextMuted,
                )
            }
            Column {
                Text(
                    text = "İyi Geceler",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KalkanTextMuted,
                )
                Text(
                    text = "Merhaba, Kullanıcı",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
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
private fun StatusCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, KalkanGreen.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = KalkanGreen,
                )
                Text(
                    text = "DURUM",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.1.sp),
                    color = OnSuccessContainer.copy(alpha = 0.78f),
                )
            }
            Text(
                text = "Güvendesiniz",
                style = MaterialTheme.typography.headlineSmall,
                color = OnSuccessContainer,
            )
            Text(
                text = "Yakınınızda riskli bir olay bulunmuyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSuccessContainer.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun EmergencyActionGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EmergencyTile(
                title = "İyiyim",
                icon = Icons.Rounded.CheckCircle,
                containerColor = Color(0xFF0B5121),
                modifier = Modifier.weight(1f),
            )
            EmergencyTile(
                title = "Yardım\nİstiyorum",
                icon = Icons.Rounded.Warning,
                containerColor = KalkanRed,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationTile(modifier = Modifier.weight(1f))
            EmergencyTile(
                title = "SOS",
                icon = Icons.Rounded.Warning,
                containerColor = KalkanRed,
                modifier = Modifier.weight(1f),
                titleSize = 32,
            )
        }
    }
}

@Composable
private fun EmergencyTile(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    titleSize: Int = 24,
) {
    Button(
        onClick = {},
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (title == "SOS") 48.dp else 40.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = titleSize.sp,
                    lineHeight = (titleSize + 6).sp,
                ),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LocationTile(modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, KalkanBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = QuietSurface),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = KalkanBlue,
            )
            Text(
                text = "Konum Paylaş",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RecentEarthquakesCard() {
    HomeSectionCard {
        Text(
            text = "Son Depremler",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Column {
            EarthquakeItem(
                magnitude = "4.2",
                location = "Göksun, Kahramanmaraş",
                detail = "10 dk önce • Derinlik: 7.2 km",
                badgeColor = ErrorContainer,
                badgeTextColor = OnErrorContainer,
            )
            HorizontalDivider(color = KalkanBorder.copy(alpha = 0.45f))
            EarthquakeItem(
                magnitude = "3.1",
                location = "Pütürge, Malatya",
                detail = "45 dk önce • Derinlik: 5.0 km",
                badgeColor = SurfaceVariant,
                badgeTextColor = MaterialTheme.colorScheme.primary,
            )
        }
        TextButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Tümünü Gör",
                style = MaterialTheme.typography.labelLarge,
                color = KalkanBlue,
            )
        }
    }
}

@Composable
private fun EarthquakeItem(
    magnitude: String,
    location: String,
    detail: String,
    badgeColor: Color,
    badgeTextColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = magnitude,
                style = MaterialTheme.typography.titleMedium,
                color = badgeTextColor,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = location,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun EmergencyContactsCard() {
    HomeSectionCard(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(SurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Groups,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = KalkanTextMuted,
            )
        }
        Text(
            text = "Acil Durum Kişileri",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Henüz kişi eklenmedi. Acil durumlarda hızlıca ulaşmak için kişi ekleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = KalkanTextMuted,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {},
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = KalkanBlue),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Kişi Ekle", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun HomeSectionCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuietSurface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = horizontalAlignment,
            content = content,
        )
    }
}
