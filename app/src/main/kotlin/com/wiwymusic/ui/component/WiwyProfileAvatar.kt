package com.wiwymusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wiwymusic.R
import com.wiwymusic.utils.UserPrefs

/**
 * Avatar del usuario (foto o color preseleccionado), en su color de marca
 * naranja por defecto. Puramente decorativo: no es un botón ni navega a
 * ningún lado (Ajustes ya está disponible en la barra inferior).
 */
@Composable
fun WiwyProfileAvatar(modifier: Modifier = Modifier) {
    val wiwyAvatar by UserPrefs.avatarUrl.collectAsState()
    val presetColor = wiwyAvatar?.takeIf { it.startsWith("preset:") }
        ?.removePrefix("preset:")?.toIntOrNull()
        ?.let { WiwyAvatarPresets.getOrNull(it) }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(presetColor ?: Color(0xFFF5791F)),
        contentAlignment = Alignment.Center,
    ) {
        if (presetColor == null && wiwyAvatar != null) {
            AsyncImage(
                model = wiwyAvatar,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
