package com.zgrcan.kalkan.feature.earthquakes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanBorder
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.util.formattedMagnitude

private val OnErrorContainer = Color(0xFF93000A)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EarthquakesScreen(
    onEarthquakeClick: (String) -> Unit = {},
    viewModel: EarthquakeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val visibleCount by viewModel.visibleCount.collectAsState()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            item {
                EarthquakesTopBar()
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                EarthquakeLastUpdatedLabel(lastUpdatedAt = uiState.lastUpdatedAt)
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                SearchAndFilter()
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                EarthquakeFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterClick = viewModel::selectFilter,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            earthquakeListItems(
                uiState = uiState,
                visibleCount = visibleCount,
                onRetry = viewModel::refresh,
                onEarthquakeClick = onEarthquakeClick,
                onLoadMore = viewModel::loadMore,
            )
            item {
                AfadDataSourceFooter()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
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
private fun EarthquakesTopBar() {
    Text(
        text = "Son Depremler",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
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

private fun LazyListScope.earthquakeListItems(
    uiState: EarthquakeUiState,
    visibleCount: Int,
    onRetry: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    when (uiState) {
        EarthquakeUiState.Loading -> {
            item(key = "loading") {
                LoadingState()
            }
        }
        is EarthquakeUiState.Empty -> {
            item(key = "empty") {
                EmptyState(onRetry = onRetry)
            }
        }
        is EarthquakeUiState.Success -> {
            paginatedEarthquakeItems(
                earthquakes = uiState.earthquakes,
                visibleCount = visibleCount,
                onEarthquakeClick = onEarthquakeClick,
                onLoadMore = onLoadMore,
                listKeyPrefix = "success",
            )
        }
        is EarthquakeUiState.Error -> {
            item(key = "error") {
                StatusCard(
                    title = "Veriler alinamadi",
                    message = uiState.message,
                    onRetry = onRetry,
                    isError = true,
                )
            }
            if (uiState.cachedEarthquakes.isNotEmpty()) {
                item(key = "cached_header") {
                    Text(
                        text = "Son yuklenen veriler",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                paginatedEarthquakeItems(
                    earthquakes = uiState.cachedEarthquakes,
                    visibleCount = visibleCount,
                    onEarthquakeClick = onEarthquakeClick,
                    onLoadMore = onLoadMore,
                    listKeyPrefix = "cached",
                )
            }
        }
    }
}

private fun LazyListScope.paginatedEarthquakeItems(
    earthquakes: List<Earthquake>,
    visibleCount: Int,
    onEarthquakeClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    listKeyPrefix: String,
) {
    val visibleEarthquakes = earthquakes.take(visibleCount)
    val hasMore = earthquakes.size > visibleCount

    items(
        items = visibleEarthquakes,
        key = { earthquake -> "$listKeyPrefix-${earthquake.id}" },
    ) { earthquake ->
        EarthquakeCard(
            earthquake = earthquake,
            onClick = {
                if (earthquake.id.isNotBlank()) {
                    onEarthquakeClick(earthquake.id)
                }
            },
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }

    if (hasMore) {
        item(key = "$listKeyPrefix-load_more") {
            LoadMoreButton(
                onClick = onLoadMore,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
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
private fun LoadMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, KalkanBorder.copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = KalkanBlue,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = "Daha Fazla Göster",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AfadDataSourceFooter() {
    val context = LocalContext.current
    val mutedStyle = MaterialTheme.typography.bodySmall.copy(
        color = KalkanTextMuted,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Veri Kaynağı: AFAD",
            style = mutedStyle,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "https://deprem.afad.gov.tr",
            style = mutedStyle.copy(textDecoration = TextDecoration.Underline),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://deprem.afad.gov.tr"),
                )
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Tarayıcı açılamadı.", Toast.LENGTH_SHORT).show()
                }
            },
        )
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
private fun EarthquakeCard(
    earthquake: Earthquake,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeStyle = earthquake.badgeStyle()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .background(badgeStyle.background, RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = earthquake.formattedMagnitude(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = badgeStyle.content,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = earthquake.title.ifBlank { "ML" },
                    style = MaterialTheme.typography.labelLarge,
                    color = badgeStyle.content.copy(alpha = 0.78f),
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
                EarthquakeWarningLabel(earthquake = earthquake)
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
