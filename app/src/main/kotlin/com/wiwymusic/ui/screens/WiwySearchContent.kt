/*
 * WiwyMusic — Contenido de la pestaña Buscar (estilo mockup, datos reales)
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.wiwymusic.R
import com.wiwymusic.innertube.models.AlbumItem
import com.wiwymusic.innertube.models.PlaylistItem
import com.wiwymusic.viewmodels.HomeViewModel

private val WiwyOrange = Color(0xFFF5791F)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

private data class WiwyGenre(val name: String, val icon: Int, val tint: Color)

private val wiwyGenres = listOf(
    WiwyGenre("Rock", R.drawable.album, Color(0xFFE0322E)),
    WiwyGenre("Pop", R.drawable.mic, Color(0xFF7C5CFF)),
    WiwyGenre("Reggaetón", R.drawable.headphones, Color(0xFFE0322E)),
    WiwyGenre("Hip Hop", R.drawable.radio, Color(0xFF9B6BFF)),
    WiwyGenre("Electrónica", R.drawable.equalizer, Color(0xFF2F8CF5)),
    WiwyGenre("Regional Mexicano", R.drawable.radio, Color(0xFFF5A623)),
    WiwyGenre("Indie", R.drawable.music_note, Color(0xFFE0559B)),
    WiwyGenre("Jazz", R.drawable.graphic_eq, Color(0xFFF5791F)),
)

@Composable
fun WiwySearchLanding(
    navController: NavController,
    onActivateSearch: () -> Unit,
    onSearch: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val statusTop = androidx.compose.foundation.layout.WindowInsets.statusBars
        .asPaddingValues().calculateTopPadding()
    Column(Modifier.fillMaxSize().background(Color(0xFF0A0A0C))) {
        // Cabecera FIJA: "Buscar" + avatar
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, bottom = 8.dp)
                .padding(top = statusTop + 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Buscar", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(WiwyOrange)
                    .clickable { navController.navigate("settings") },
                contentAlignment = Alignment.Center,
            ) {
                if (accountImageUrl != null) {
                    AsyncImage(model = accountImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Icon(painterResource(R.drawable.person), null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
        // Barra de búsqueda FIJA
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WiwyCard)
                .clickable { onActivateSearch() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.search), null, tint = WiwyMuted, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text("Buscar canciones, artistas, álbumes…", color = WiwyMuted, fontSize = 15.sp)
        }
        // Contenido desplazable
        Box(Modifier.weight(1f)) {
            WiwySearchContent(navController = navController, onSearch = onSearch, viewModel = viewModel)
        }
    }
}

@Composable
fun WiwySearchContent(
    navController: NavController,
    onSearch: (String) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp = 8.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val allItems = homePage?.sections?.flatMap { it.items } ?: emptyList()
    // Álbumes destacados: nuevos lanzamientos (fuente dedicada) + los del home como respaldo
    val albums = (explorePage?.newReleaseAlbums.orEmpty() + allItems.filterIsInstance<AlbumItem>())
        .distinctBy { it.id }.take(10)
    val playlists = allItems.filterIsInstance<PlaylistItem>().distinctBy { it.id }.take(10)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
    ) {
        // Identificar canción
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFF5791F), Color(0xFFE0322E))))
                    .clickable { navController.navigate("music_recognition") }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.graphic_eq), null, tint = Color.White, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Identificar canción", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Toca para reconocer música", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }
        }

        // Explorar géneros (grid 2 columnas)
        item { SectionHeader("Explorar géneros") }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                wiwyGenres.chunked(2).forEach { rowGenres ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGenres.forEach { g ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(WiwyCard)
                                    .clickable { onSearch(g.name) }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(painterResource(g.icon), null, tint = g.tint, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    g.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    lineHeight = 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (rowGenres.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // Álbumes destacados (reales)
        if (albums.isNotEmpty()) {
            item { SectionHeader("Álbumes destacados") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(albums) { album ->
                        MediaCard(album.title, album.artists?.joinToString { it.name }, album.thumbnail, false) {
                            navController.navigate("album/${album.id}")
                        }
                    }
                }
            }
        }

        // Playlists destacadas (reales)
        if (playlists.isNotEmpty()) {
            item { SectionHeader("Playlists destacadas") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(playlists) { pl ->
                        MediaCard(pl.title, pl.author?.name, pl.thumbnail, false) {
                            navController.navigate("online_playlist/${pl.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
    )
}

@Composable
private fun MediaCard(title: String, subtitle: String?, thumbnailUrl: String?, circle: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(if (circle) CircleShape else RoundedCornerShape(14.dp))
                .background(WiwyCard),
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(model = thumbnailUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(painterResource(R.drawable.music_note), null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.Center).size(40.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, color = WiwyMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
