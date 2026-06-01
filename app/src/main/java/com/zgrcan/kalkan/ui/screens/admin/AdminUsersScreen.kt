package com.zgrcan.kalkan.ui.screens.admin

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zgrcan.kalkan.core.design.components.KalkanCard
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanRed
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.viewmodel.AdminUsersState
import com.zgrcan.kalkan.viewmodel.AdminUsersViewModel

@Composable
fun AdminUsersScreen(
    onBackClick: () -> Unit,
    viewModel: AdminUsersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = remember(uiState) { uiState.toUserStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AdminUsersTopBar(
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = KalkanBlue)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(stats.chunked(2), key = { row -> row.first().title }) { rowStats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowStats.forEach { stat ->
                                CompactUserStatCard(
                                    stat = stat,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowStats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Text(
                text = "Kullanıcılar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Kullanıcı istatistikleri ve özet veriler",
                style = MaterialTheme.typography.bodySmall,
                color = KalkanTextMuted,
            )
        }
    }
}

@Composable
private fun CompactUserStatCard(
    stat: UserStat,
    modifier: Modifier = Modifier,
) {
    KalkanCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp, max = 88.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(stat.tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.tint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = KalkanTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class UserStat(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color,
)

private fun AdminUsersState.toUserStats(): List<UserStat> {
    val items = listOf(
        UserStat("Toplam Kullanıcı", totalUsers, Icons.Rounded.Person, KalkanBlue),
        UserStat("Misafir Kullanıcılar", guestUsers, Icons.Rounded.Person, Color(0xFF7C3AED)),
        UserStat("Aktif Cihazlar", activeDevices, Icons.Rounded.PhoneAndroid, Color(0xFF0EA5E9)),
        UserStat("Deprem Bildirimi Açık", earthquakeEnabledUsers, Icons.Rounded.NotificationsActive, Color(0xFF10B981)),
        UserStat("Toplam Aile Grubu", totalFamilies, Icons.Rounded.Groups, Color(0xFFF59E0B)),
        UserStat("Aktif SOS Durumu", activeSosCount, Icons.Rounded.Warning, KalkanRed),
    )
    return items
}
