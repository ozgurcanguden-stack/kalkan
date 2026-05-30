package com.kalkan.app.feature.earthquakes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.South
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kalkan.app.core.design.theme.KalkanBlue
import com.kalkan.app.core.design.theme.KalkanBorder
import com.kalkan.app.core.design.theme.KalkanTextMuted
import com.kalkan.app.domain.model.Earthquake
import java.util.Locale

private val SurfaceVariant = Color(0xFFE0E3E5)
private val TertiaryFixed = Color(0xFFFCDEB5)
private val OnTertiaryFixed = Color(0xFF271901)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EarthquakesScreen(
    viewModel: EarthquakeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isRefreshing = when (val state = uiState) {
        is EarthquakeUiState.Success -> state.isRefreshing
        is EarthquakeUiState.Error -> state.isRefreshing
        is EarthquakeUiState.Empty,
        EarthquakeUiState.Loading,
        -> false
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            EarthquakesTopBar(onRefreshClick = viewModel::refresh)
            EarthquakeLastUpdatedLabel(lastUpdatedAt = uiState.lastUpdatedAt)
            SearchAndFilter()
            QuickFilters(
                selectedFilter = selectedFilter,
                onFilterClick = viewModel::selectFilter,
            )
            EarthquakeContent(uiState = uiState, onRetry = viewModel::refresh)
            Spacer(modifier = Modifier.height(12.dp))
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = KalkanBlue,
        )
    }
}

@Composable
private fun EarthquakesTopBar(onRefreshClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Son Depremler",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Row {
            IconButton(onClick = onRefreshClick) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Yenile", tint = MaterialTheme.colorScheme.primary)
            }
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
            Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Filtrele", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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
        Icon(Icons.Rounded.Search, contentDescription = null, tint = KalkanTextMuted)
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            singleLine = true,
            cursorBrush = SolidColor(KalkanBlue),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Bolge veya sehir ara...",
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
private fun QuickFilters(
    selectedFilter: EarthquakeFilter,
    onFilterClick: (EarthquakeFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EarthquakeFilter.entries.forEach { filter ->
            FilterChip(
                text = filter.label,
                selected = filter == selectedFilter,
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun FilterChip(
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
    )
}

@Composable
private fun EarthquakeContent(
    uiState: EarthquakeUiState,
    onRetry: () -> Unit,
) {
    when (uiState) {
        EarthquakeUiState.Loading -> LoadingState()
        is EarthquakeUiState.Empty -> EmptyState(onRetry = onRetry)
        is EarthquakeUiState.Success -> EarthquakeList(earthquakes = uiState.earthquakes)
        is EarthquakeUiState.Error -> ErrorState(uiState = uiState, onRetry = onRetry)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = KalkanBlue)
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    StatusCard(
        title = "Deprem verisi bulunamadi",
        message = "Secili filtre icin gosterilecek AFAD kaydi yok.",
        onRetry = onRetry,
    )
}

@Composable
private fun ErrorState(
    uiState: EarthquakeUiState.Error,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StatusCard(
            title = "Veriler alinamadi",
            message = uiState.message,
            onRetry = onRetry,
            isError = true,
        )
        if (uiState.cachedEarthquakes.isNotEmpty()) {
            Text(
                text = "Son yuklenen veriler",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            EarthquakeList(earthquakes = uiState.cachedEarthquakes)
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    message: String,
    onRetry: () -> Unit,
    isError: Boolean = false,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isError) Icons.Rounded.Warning else Icons.Rounded.Search,
                contentDescription = null,
                tint = if (isError) OnErrorContainer else KalkanBlue,
            )
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted)
            TextButton(onClick = onRetry) {
                Text("Tekrar dene", color = KalkanBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EarthquakeList(earthquakes: List<Earthquake>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        earthquakes.forEach { earthquake ->
            EarthquakeCard(earthquake = earthquake)
        }
    }
}

@Composable
private fun EarthquakeCard(earthquake: Earthquake) {
    val badgeColors = earthquake.badgeColors()
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
                    .background(badgeColors.first, RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = String.format(Locale("tr", "TR"), "%.1f", earthquake.magnitude),
                    style = MaterialTheme.typography.headlineSmall,
                    color = badgeColors.second,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = earthquake.title.ifBlank { "ML" },
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeColors.second.copy(alpha = 0.78f),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = earthquake.location.ifBlank { "Konum bilgisi yok" },
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
                    DetailText(icon = Icons.Rounded.Schedule, text = earthquake.dateTime.formatEarthquakeDate())
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(KalkanBorder, CircleShape),
                    )
                    DetailText(icon = Icons.Rounded.South, text = "${earthquake.depth} km")
                }
            }

            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = KalkanBorder)
        }
    }
}

@Composable
private fun DetailText(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = KalkanTextMuted)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = KalkanTextMuted, maxLines = 1)
    }
}

private fun Earthquake.badgeColors(): Pair<Color, Color> =
    when {
        magnitude >= 5.0 -> ErrorContainer to OnErrorContainer
        magnitude >= 4.0 -> TertiaryFixed to OnTertiaryFixed
        else -> SurfaceVariant to Color(0xFF0F172A)
    }
