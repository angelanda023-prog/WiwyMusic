/*
 * WiwyMusic — Biblioteca (landing tipo mockup, datos reales)
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.wiwymusic.LocalDatabase
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.LocalPlayerConnection
import com.wiwymusic.R
import com.wiwymusic.constants.AlbumSortType
import com.wiwymusic.constants.ArtistSortType
import com.wiwymusic.constants.PlaylistSortType
import com.wiwymusic.db.entities.Song
import com.wiwymusic.models.toMediaMetadata
import com.wiwymusic.playback.queues.YouTubeQueue
import com.wiwymusic.ui.component.CreatePlaylistDialog

private val WiwyOrange = Color(0xFFF5791F)
private val WiwyBg = Color(0xFF0A0A0C)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

@Composable
fun WiwyLibraryScreen(navController: NavController) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current

    val likedCount by database.likedSongsCount().collectAsState(initial = 0)
    val playlists by remember { database.playlists(PlaylistSortType.CREATE_DATE, true) }.collectAsState(initial = emptyList())
    val albums by remember { database.albums(AlbumSortType.CREATE_DATE, true) }.collectAsState(initial = emptyList())
    val artists by remember { database.artists(ArtistSortType.CREATE_DATE, false) }.collectAsState(initial = emptyList())
    val recentEvents by remember { database.events() }.collectAsState(initial = emptyList())
    val recentSongs = recentEvents.map { it.song }.distinctBy { it.song.id }.take(8)

    var showCreate by remember { mutableStateOf(false) }
    if (showCreate) {
        CreatePlaylistDialog(onDismiss = { showCreate = false })
    }

    val insets = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    val statusTop = androidx.compose.foundation.layout.WindowInsets.statusBars
        .asPaddingValues().calculateTopPadding()

    Column(Modifier.fillMaxSize().background(WiwyBg)) {
    // Cabecera FIJA
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, bottom = 12.dp)
            .padding(top = statusTop + 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Biblioteca", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable { showCreate = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.add), null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(WiwyOrange).clickable { navController.navigate("settings") },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.person), null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = insets.calculateBottomPadding() + 24.dp),
    ) {
        // Categorías
        item {
            CategoryRow(R.drawable.favorite, Color(0xFFF25D7A), Color(0x33F25D7A), "Favoritos", "$likedCount canciones") {
                navController.navigate("auto_playlist/liked")
            }
        }
        item {
            CategoryRow(R.drawable.music_note, WiwyOrange, Color(0x33F5791F), "Mis Playlists", "${playlists.size} playlists") {
                navController.navigate("library/playlists")
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showCreate = true }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.add), null, tint = WiwyOrange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text("Nueva playlist", color = WiwyOrange, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            CategoryRow(R.drawable.album, Color(0xFF3FBF5F), Color(0x333FBF5F), "Álbumes", "${albums.size} álbumes") {
                navController.navigate("library/albums")
            }
        }
        item {
            CategoryRow(R.drawable.person, Color(0xFF5C8DF2), Color(0x335C8DF2), "Artistas", "${artists.size} artistas") {
                navController.navigate("library/artists")
            }
        }
        item {
            CategoryRow(R.drawable.history, Color(0xFF5C8DF2), Color(0x335C8DF2), "Historial", "Escuchado recientemente") {
                navController.navigate("history")
            }
        }

        // Escuchado recientemente
        if (recentSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Escuchado recientemente", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.navigate_next), null, tint = WiwyMuted, modifier = Modifier.size(22.dp).clickable { navController.navigate("history") })
                }
            }
            items(recentSongs) { song ->
                RecentSongRow(song) {
                    playerConnection?.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                }
            }
        }
    }
    }
}

@Composable
private fun CategoryRow(
    icon: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = WiwyMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RecentSongRow(song: Song, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(WiwyCard),
        ) {
            if (song.thumbnailUrl != null) {
                AsyncImage(model = song.thumbnailUrl, contentDescription = song.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString { it.name }, color = WiwyMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(WiwyCard),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.play), null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
