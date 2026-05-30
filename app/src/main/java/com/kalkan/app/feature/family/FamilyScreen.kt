package com.kalkan.app.feature.family

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
fun FamilyScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ailem", style = MaterialTheme.typography.headlineSmall)
        KalkanCard {
            Text("Aile durumu", style = MaterialTheme.typography.titleLarge)
            Text("Aile uyeleri, son bilinen konum ve guvenlik bildirimleri burada yer alacak.", color = KalkanTextMuted)
        }
    }
}
