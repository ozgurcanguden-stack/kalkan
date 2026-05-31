package com.zgrcan.kalkan.feature.earthquakes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted

@Composable
fun EarthquakeFilterChips(
    selectedFilter: EarthquakeFilter,
    onFilterClick: (EarthquakeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EarthquakeFilter.entries.forEach { filter ->
            EarthquakeFilterChip(
                text = filter.label,
                selected = filter == selectedFilter,
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun EarthquakeFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) KalkanBlue else MaterialTheme.colorScheme.surface
    val content = if (selected) Color.White else KalkanTextMuted

    Text(
        text = text,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        style = MaterialTheme.typography.labelLarge,
        color = content,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
    )
}
