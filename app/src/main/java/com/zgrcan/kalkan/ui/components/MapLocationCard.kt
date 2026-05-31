package com.zgrcan.kalkan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zgrcan.kalkan.core.design.theme.KalkanBlue
import com.zgrcan.kalkan.core.design.theme.KalkanTextMuted
import com.zgrcan.kalkan.model.FamilyMember
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.util.TimeAgoUtils

@Composable
fun MapLocationCard(
    member: FamilyMember,
    onOpenLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    openMapButtonLabel: String = "Konumu Aç",
) {
    val statusType = SafetyStatusType.from(member.lastStatusType)
    val style = member.statusStyle(statusType)
    val hasLocation = member.lastStatusLatitude != null && member.lastStatusLongitude != null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = style.containerColor),
        border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(style.accentColor),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = member.displayName.ifBlank { "Üye" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier = Modifier
                            .background(style.badgeBackground, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = style.accentColor,
                        )
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = style.accentColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    text = if (hasLocation) {
                        "Son güncelleme: ${TimeAgoUtils.format(member.lastStatusAt)}"
                    } else {
                        "Konum paylaşılmadı"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasLocation) KalkanTextMuted else style.accentColor.copy(alpha = 0.85f),
                )
                if (hasLocation) {
                    OutlinedButton(
                        onClick = onOpenLocationClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, KalkanBlue.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KalkanBlue),
                    ) {
                        Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(openMapButtonLabel, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

private data class MemberStatusStyle(
    val label: String,
    val accentColor: Color,
    val badgeBackground: Color,
    val containerColor: Color,
    val icon: ImageVector,
)

private fun FamilyMember.statusStyle(statusType: SafetyStatusType?): MemberStatusStyle =
    when (statusType) {
        SafetyStatusType.SOS -> MemberStatusStyle(
            label = "ACİL SOS",
            accentColor = Color(0xFFC5221F),
            badgeBackground = Color(0xFFFCE8E6),
            containerColor = Color(0xFFFFF1F1),
            icon = Icons.Rounded.HealthAndSafety,
        )
        SafetyStatusType.NEED_HELP -> MemberStatusStyle(
            label = "Yardım İstiyorum",
            accentColor = Color(0xFFB45309),
            badgeBackground = Color(0xFFFFEDD5),
            containerColor = Color(0xFFFFF7ED),
            icon = Icons.AutoMirrored.Rounded.Help,
        )
        SafetyStatusType.SHARE_LOCATION -> MemberStatusStyle(
            label = "Konum Paylaştı",
            accentColor = Color(0xFF1A73E8),
            badgeBackground = Color(0xFFE8F0FE),
            containerColor = Color(0xFFF5F9FF),
            icon = Icons.Rounded.MyLocation,
        )
        SafetyStatusType.SAFE -> MemberStatusStyle(
            label = "İyiyim",
            accentColor = Color(0xFF137333),
            badgeBackground = Color(0xFFE6F4EA),
            containerColor = Color(0xFFF3FAF5),
            icon = Icons.Rounded.CheckCircle,
        )
        null -> MemberStatusStyle(
            label = "Belirsiz",
            accentColor = Color(0xFF5F6368),
            badgeBackground = Color(0xFFF1F3F4),
            containerColor = Color(0xFFF8FAFC),
            icon = Icons.Rounded.LocationOff,
        )
    }
