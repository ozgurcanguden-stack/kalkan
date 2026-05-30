package com.kalkan.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.components.KalkanButton
import com.kalkan.app.core.design.components.KalkanCard
import com.kalkan.app.core.design.components.KalkanEmergencyButton
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanGreen
import com.kalkan.app.core.design.theme.KalkanRed
import com.kalkan.app.core.design.theme.KalkanTextMuted

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Kalkan",
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = "Hazirlikli Ol. Guvende Kal.",
            style = MaterialTheme.typography.bodyLarge,
            color = KalkanTextMuted,
        )

        KalkanCard {
            Text("Durum Kartı", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Acil durumda tek dokunusla durumunu bildir, yardim iste veya konumunu paylas.",
                style = MaterialTheme.typography.bodyLarge,
                color = KalkanTextMuted,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KalkanEmergencyButton(
                title = "Iyiyim",
                subtitle = "Durum bildir",
                color = KalkanGreen,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            KalkanEmergencyButton(
                title = "Yardim",
                subtitle = "Destek iste",
                color = KalkanBlue,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }

        KalkanButton(text = "Konum Paylas", onClick = {}, containerColor = KalkanBlue)
        KalkanEmergencyButton(
            title = "SOS",
            subtitle = "Acil yardim sinyali gonder",
            color = KalkanRed,
            onClick = {},
        )

        KalkanCard {
            Text("Son Depremler", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            EarthquakePreview("Marmara Denizi", "4.1", "12 dk once")
            EarthquakePreview("Malatya", "3.7", "34 dk once")
        }

        KalkanCard {
            Text("Acil Kisiler", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Henuz acil kisi eklenmedi. Ilk kurulumda en az iki kisi eklenmesi onerilir.",
                style = MaterialTheme.typography.bodyLarge,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun EarthquakePreview(location: String, magnitude: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(location, style = MaterialTheme.typography.titleMedium)
            Text(time, style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
        }
        Text(
            text = magnitude,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
