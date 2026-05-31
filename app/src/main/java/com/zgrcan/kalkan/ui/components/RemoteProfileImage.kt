package com.zgrcan.kalkan.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun RemoteProfileImage(
    photoUrl: String?,
    shape: Shape,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    var loadFailed by remember(photoUrl) { mutableStateOf(false) }
    val hasPhoto = !photoUrl.isNullOrBlank() && !loadFailed

    Box(modifier = modifier.clip(shape)) {
        fallback()
        if (hasPhoto) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profil foto\u011fraf\u0131",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
            )
        }
    }
}
