package com.zgrcan.kalkan.feature.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeFilterChips
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeUiState
import com.zgrcan.kalkan.feature.earthquakes.EarthquakeViewModel
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.util.MapIntentHelper
import com.zgrcan.kalkan.util.MapsConfig
import com.zgrcan.kalkan.viewmodel.FamilyGroupViewModel

private val StitchSecondary = Color(0xFF0051D5)
private val StitchSurface = Color(0xFFF7F9FB)
private val StitchOutline = Color(0xFF76777D)

@Composable
fun MapScreen(
    user: AppUser?,
    focusFamilyMembers: Boolean = false,
    earthquakeViewModel: EarthquakeViewModel = hiltViewModel(),
    familyGroupViewModel: FamilyGroupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val earthquakeState by earthquakeViewModel.uiState.collectAsState()
    val selectedEarthquakeFilter by earthquakeViewModel.selectedFilter.collectAsState()
    val familyState by familyGroupViewModel.uiState.collectAsState()

    var showEarthquakeLayer by remember { mutableStateOf(true) }
    var showFamilyLayer by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<MapSelectedItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pendingFamilyFocus by remember { mutableStateOf(false) }

    LaunchedEffect(focusFamilyMembers) {
        if (focusFamilyMembers) {
            pendingFamilyFocus = true
            showEarthquakeLayer = false
            showFamilyLayer = true
            if (selectedItem is MapSelectedItem.EarthquakeItem) {
                selectedItem = null
            }
        } else {
            pendingFamilyFocus = false
            showEarthquakeLayer = true
            showFamilyLayer = true
        }
    }
    var familyFocusRequestKey by remember { mutableStateOf(0) }

    LaunchedEffect(user?.uid, user?.familyGroupId) {
        familyGroupViewModel.loadFamilyGroup(user)
    }

    LaunchedEffect(isRefreshing, earthquakeState, familyState.isLoading) {
        if (!isRefreshing) return@LaunchedEffect
        val state = earthquakeState
        val earthquakeDone = state !is EarthquakeUiState.Loading &&
            !(state is EarthquakeUiState.Success && state.isRefreshing) &&
            !(state is EarthquakeUiState.Error && state.isRefreshing)
        val familyDone = !familyState.isLoading
        if (earthquakeDone && familyDone) {
            isRefreshing = false
        }
    }

    val earthquakes = remember(earthquakeState) { extractEarthquakes(earthquakeState) }
    val familyMembers = familyState.members
    val locatableFamilyMembers = remember(familyMembers) { familyMembers.withSharedMapLocations() }

    LaunchedEffect(pendingFamilyFocus, focusFamilyMembers, familyState.isLoading, locatableFamilyMembers) {
        if (!focusFamilyMembers || !pendingFamilyFocus || familyState.isLoading) return@LaunchedEffect
        showEarthquakeLayer = false
        showFamilyLayer = true
        familyFocusRequestKey++
        pendingFamilyFocus = false
    }

    val showFamilyEmptyMessage = focusFamilyMembers &&
        !familyState.isLoading &&
        familyState.hasGroup &&
        locatableFamilyMembers.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        if (MapsConfig.isConfigured) {
            KalkanGoogleMapContent(
                earthquakes = earthquakes,
                familyMembers = familyMembers,
                showEarthquakeMarkers = showEarthquakeLayer,
                showFamilyMarkers = showFamilyLayer && familyState.hasGroup,
                selectedItem = selectedItem,
                onMarkerClick = { selectedItem = it },
                familyFocusRequestKey = familyFocusRequestKey,
                locatableFamilyMembers = locatableFamilyMembers,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            MapApiKeyPlaceholder(modifier = Modifier.fillMaxSize())
        }

        if (showFamilyEmptyMessage) {
            FamilyMapEmptyStateBanner(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
            )
        }

        MapFloatingOverlay(
            selectedFilter = selectedEarthquakeFilter,
            onFilterClick = earthquakeViewModel::selectFilter,
            showEarthquakeLayer = showEarthquakeLayer,
            onEarthquakeLayerToggle = {
                showEarthquakeLayer = !showEarthquakeLayer
                if (!showEarthquakeLayer && selectedItem is MapSelectedItem.EarthquakeItem) {
                    selectedItem = null
                }
            },
            showFamilyLayer = showFamilyLayer,
            onFamilyLayerToggle = {
                showFamilyLayer = !showFamilyLayer
                if (!showFamilyLayer && selectedItem is MapSelectedItem.FamilyItem) {
                    selectedItem = null
                }
            },
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                earthquakeViewModel.refresh()
                familyGroupViewModel.refresh()
            },
        )

        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StitchSurface.copy(alpha = 0.92f))
                    .padding(16.dp),
            ) {
                CircularProgressIndicator(color = StitchSecondary, modifier = Modifier.size(28.dp))
            }
        }

        selectedItem?.let { item ->
            MapMarkerBottomSheet(
                selectedItem = item,
                onDismiss = { selectedItem = null },
                onOpenExternalMap = { latitude, longitude ->
                    if (!MapIntentHelper.openLocation(context, latitude, longitude)) {
                        Toast.makeText(context, "Google Maps açılamadı.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun MapFloatingOverlay(
    selectedFilter: com.zgrcan.kalkan.feature.earthquakes.EarthquakeFilter,
    onFilterClick: (com.zgrcan.kalkan.feature.earthquakes.EarthquakeFilter) -> Unit,
    showEarthquakeLayer: Boolean,
    onEarthquakeLayerToggle: () -> Unit,
    showFamilyLayer: Boolean,
    onFamilyLayerToggle: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Harita",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StitchSurface.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StitchSurface.copy(alpha = 0.92f))
                    .border(1.dp, StitchOutline.copy(alpha = 0.2f), CircleShape),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Yenile", tint = StitchSecondary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapLayerChip(
                label = "Aile",
                icon = Icons.Rounded.Groups,
                selected = showFamilyLayer,
                onClick = onFamilyLayerToggle,
            )
            MapLayerChip(
                label = "Depremler",
                icon = Icons.Rounded.Public,
                selected = showEarthquakeLayer,
                onClick = onEarthquakeLayerToggle,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StitchSurface.copy(alpha = 0.92f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            EarthquakeFilterChips(
                selectedFilter = selectedFilter,
                onFilterClick = onFilterClick,
            )
        }
    }
}

@Composable
private fun MapLayerChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else StitchSurface.copy(alpha = 0.92f)
    val content = if (selected) Color.White else MaterialTheme.colorScheme.primary
    val borderColor = if (selected) Color.Transparent else StitchOutline.copy(alpha = 0.25f)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Color.White else StitchSecondary, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FamilyMapEmptyStateBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StitchSurface.copy(alpha = 0.96f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Aile üyelerinizin paylaşılan konumu bulunmuyor.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun extractEarthquakes(uiState: EarthquakeUiState): List<Earthquake> =
    when (uiState) {
        is EarthquakeUiState.Success -> uiState.earthquakes
        is EarthquakeUiState.Error -> uiState.cachedEarthquakes
        EarthquakeUiState.Loading,
        is EarthquakeUiState.Empty,
        -> emptyList()
    }
