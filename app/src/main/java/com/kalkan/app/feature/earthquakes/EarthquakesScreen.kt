package com.kalkan.app.feature.earthquakes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kalkan.app.core.design.components.KalkanCard
import com.kalkan.app.core.design.theme.KalkanTextMuted

@Composable
fun EarthquakesScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Depremler", style = MaterialTheme.typography.headlineSmall)
        KalkanCard {
            Text("AFAD verileri", style = MaterialTheme.typography.titleLarge)
            Text("Son depremler, yakindaki hareketler ve detayli deprem bilgileri burada listelenecek.", color = KalkanTextMuted)
        }
    }
}
