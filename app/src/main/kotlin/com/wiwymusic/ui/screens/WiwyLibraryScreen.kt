/*
 * WiwyMusic — Biblioteca / Playlist (landing tipo mockup, datos reales)
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.wiwymusic.LocalDatabase
import com.wiwymusic.LocalDownloadUtil
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.R
import com.wiwymusic.constants.PlaylistSortType
import com.wiwymusic.db.entities.Playlist
import com.wiwymusic.ui.component.CreatePlaylistDialog
import com.wiwymusic.ui.component.PlaylistThumbnail
import com.wiwymusic.ui.component.PremiumFeatureDialog
import com.wiwymusic.ui.component.PremiumLockBadge
import com.wiwymusic.utils.UserPrefs

private val WiwyOrange = Color(0xFFF5791F)
private val WiwyBg = Color(0xFF0A0A0C)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

@Composable
fun WiwyLibraryScreen(navController: NavController) {
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current

    val likedCount by database.likedSongsCount().collectAsState(initial = 0)
    val playlists by remember { database.playlists(PlaylistSortType.CREATE_DATE, true) }.collectAsState(initial = emptyList())
    val recentEvents by remember { database.events() }.collectAsState(initial = emptyList())
    val downloads by downloadUtil.downloads.collectAsState()
    val isPremium by UserPrefs.isPremium.collectAsState()
    val downloadedCount = downloads.values.count { it.state == Download.STATE_COMPLETED }
    val recentCount = recentEvents.map { it.song.song.id }.distinct().size

    var showCreate by remember { mutableStateOf(false) }
    var showImportPremiumDialog by remember { mutableStateOf(false) }
    var showDownloadsPremiumDialog by remember { mutableStateOf(false) }
    if (showCreate) {
        CreatePlaylistDialog(onDismiss = { showCreate = false })
    }
    if (showImportPremiumDialog) {
        PremiumFeatureDialog(
            featureName = "Importar playlist",
            onDismiss = { showImportPremiumDialog = false },
        )
    }
    if (showDownloadsPremiumDialog) {
        PremiumFeatureDialog(
            featureName = "Descargas",
            onDismiss = { showDownloadsPremiumDialog = false },
        )
    }

    val insets = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(Modifier.fillMaxSize().background(WiwyBg)) {
        Column(Modifier.fillMaxSize()) {
            // Cabecera FIJA (se mantiene igual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, bottom = 12.dp)
                    .padding(top = statusTop + 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Playlist", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                com.wiwymusic.ui.component.WiwyProfileAvatar()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = insets.calculateBottomPadding() + 96.dp),
            ) {
                // Subtítulo
                item {
                    Text(
                        "Tus colecciones de música",
                        color = WiwyMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 14.dp),
                    )
                }

                // Nueva playlist / Importar playlist
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ActionCard(R.drawable.add, "Nueva playlist", Modifier.weight(1f)) { showCreate = true }
                        ActionCard(
                            icon = R.drawable.download,
                            label = "Importar playlist",
                            modifier = Modifier.weight(1f),
                            locked = isPremium != true,
                        ) {
                            if (isPremium == true) {
                                navController.navigate("settings/backup_restore")
                            } else {
                                showImportPremiumDialog = true
                            }
                        }
                    }
                }

                // Fijadas
                item { SectionTitle("Fijadas") }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PinnedCard(
                            icon = R.drawable.favorite,
                            iconTint = Color.White,
                            iconBg = Color(0x33FFFFFF),
                            title = "Favoritos",
                            subtitle = "$likedCount canciones",
                            background = Brush.horizontalGradient(listOf(Color(0xFFF5791F), Color(0xFFE0325A))),
                            modifier = Modifier.weight(1f),
                        ) { navController.navigate("auto_playlist/liked") }
                        PinnedCard(
                            icon = R.drawable.history,
                            iconTint = Color(0xFF8B8BF5),
                            iconBg = Color(0x338B8BF5),
                            title = "Escuchadas recientemente",
                            subtitle = "$recentCount canciones",
                            background = Brush.horizontalGradient(listOf(WiwyCard, WiwyCard)),
                            modifier = Modifier.weight(1f),
                        ) { navController.navigate("history") }
                    }
                }

                // Mis playlists
                item { SectionTitle("Mis playlists") }
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistRow(playlist, downloads) {
                        navController.navigate("local_playlist/${playlist.id}")
                    }
                }

                // Playlists inteligentes
                item { SectionTitle("Playlists inteligentes") }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SmartCard(
                            icon = R.drawable.download,
                            iconTint = Color(0xFF5C8DF2),
                            iconBg = Color(0x335C8DF2),
                            title = "Descargas",
                            subtitle = "$downloadedCount canciones",
                            modifier = Modifier.weight(1f),
                            locked = isPremium != true,
                        ) {
                            if (isPremium == true) {
                                navController.navigate("download_queue")
                            } else {
                                showDownloadsPremiumDialog = true
                            }
                        }
                        SmartCard(R.drawable.fire, WiwyOrange, Color(0x33F5791F), "Más escuchadas", "Top 50", Modifier.weight(1f)) {
                            navController.navigate("top_playlist/50")
                        }
                    }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = insets.calculateBottomPadding() + 20.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(WiwyOrange)
                .clickable { showCreate = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.add), null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
    )
}

@Composable
private fun ActionCard(
    icon: Int,
    label: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WiwyCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(painterResource(icon), null, tint = WiwyOrange, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (locked) {
            PremiumLockBadge(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun PinnedCard(
    icon: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    background: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, downloads: Map<String, Download>, onClick: () -> Unit) {
    val database = LocalDatabase.current
    val songs by remember(playlist.id) { database.playlistSongs(playlist.id) }.collectAsState(initial = emptyList())
    val totalSec = songs.sumOf { it.song.song.duration.coerceAtLeast(0) }
    val downloaded = songs.isNotEmpty() && songs.all { downloads[it.song.id]?.state == Download.STATE_COMPLETED }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistThumbnail(
            thumbnails = playlist.thumbnails,
            size = 64.dp,
            placeHolder = {
                Icon(painterResource(R.drawable.queue_music), null, tint = WiwyMuted, modifier = Modifier.size(28.dp))
            },
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(playlist.playlist.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                buildString {
                    append("${playlist.songCount} canciones")
                    if (totalSec > 0) append(" • ${durationLabel(totalSec)}")
                },
                color = WiwyMuted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (downloaded) {
                    Icon(painterResource(R.drawable.library_add_check), null, tint = WiwyOrange, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Descargada", color = WiwyOrange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(painterResource(R.drawable.cloud), null, tint = WiwyMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("En la nube", color = WiwyMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.more_vert), null, tint = WiwyMuted, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SmartCard(
    icon: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .background(WiwyCard)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = WiwyMuted, fontSize = 12.sp)
            }
        }
        if (locked) {
            PremiumLockBadge(Modifier.align(Alignment.TopEnd))
        }
    }
}

private fun durationLabel(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    return if (h > 0) "$h h $m min" else "$m min"
}
