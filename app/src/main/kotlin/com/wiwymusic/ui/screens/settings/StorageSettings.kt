/*
 * WiwyMusic — Almacenamiento (simplificado tipo Spotify: solo "Liberar espacio")
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.LocalPlayerConnection
import com.wiwymusic.R
import com.wiwymusic.extensions.directorySizeBytes
import com.wiwymusic.extensions.tryOrNull
import com.wiwymusic.ui.component.ActionPromptDialog
import com.wiwymusic.ui.component.IconButton
import com.wiwymusic.ui.component.PreferenceEntry
import com.wiwymusic.ui.player.CanvasArtworkPlaybackCache
import com.wiwymusic.ui.utils.backToMain
import com.wiwymusic.ui.utils.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StorageSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val imageDiskCache = context.imageLoader.diskCache ?: return
    val playerCache = LocalPlayerConnection.current?.service?.playerCache ?: return

    val playerCacheDir = remember { context.filesDir.resolve("exoplayer") }
    val coroutineScope = rememberCoroutineScope()

    var freeSpaceDialog by remember { mutableStateOf(false) }
    var imageCacheSize by remember { mutableStateOf(imageDiskCache.size) }
    var playerCacheSize by remember { mutableStateOf(0L) }

    LaunchedEffect(imageDiskCache) {
        while (isActive) {
            delay(500)
            imageCacheSize = imageDiskCache.size
        }
    }
    LaunchedEffect(playerCache, playerCacheDir) {
        while (isActive) {
            delay(500)
            playerCacheSize = withContext(Dispatchers.IO) {
                val cacheSpace = tryOrNull { playerCache.cacheSpace } ?: 0L
                if (cacheSpace == 0L) playerCacheDir.directorySizeBytes() else cacheSpace
            }
        }
    }

    val totalCache = imageCacheSize + playerCacheSize

    androidx.compose.material3.Scaffold(
        topBar = {
            WiwySettingsPageHeader(
                title = stringResource(R.string.storage),
                onBack = navController::navigateUp,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .verticalScroll(rememberScrollState())
        ) {

        CacheCard(
            icon = R.drawable.storage,
            title = stringResource(R.string.storage),
            description = stringResource(R.string.size_used, formatFileSize(totalCache)),
            progress = null,
            actions = {
                PreferenceEntry(
                    title = { Text("Liberar espacio") },
                    icon = { Icon(painterResource(R.drawable.delete_history), null) },
                    onClick = { freeSpaceDialog = true },
                )
            }
        )

        if (freeSpaceDialog) {
            ActionPromptDialog(
                title = "Liberar espacio",
                onDismiss = { freeSpaceDialog = false },
                onConfirm = {
                    coroutineScope.launch(Dispatchers.IO) {
                        tryOrNull {
                            playerCache.keys.forEach { key -> playerCache.removeResource(key) }
                        }
                        tryOrNull {
                            imageDiskCache.clear()
                            com.wiwymusic.utils.ArtworkStorage.clear(context)
                        }
                        tryOrNull { CanvasArtworkPlaybackCache.clear() }
                    }
                    freeSpaceDialog = false
                },
                onCancel = { freeSpaceDialog = false },
                content = {
                    Text("Se borrará la caché temporal (canciones en caché, imágenes y portadas). Tus descargas guardadas no se eliminan.")
                }
            )
        }
    }

    }
}

@Composable
fun CacheCard(
    icon: Int,
    title: String,
    description: String,
    progress: Float?,
    actions: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Card(
                    modifier = Modifier.padding(end = 12.dp),
                    shape = androidx.compose.material3.MaterialTheme.shapes.small,
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column {
                    Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text(
                        description,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.padding(4.dp))
            actions()
        }
    }
}
