/*
 * WiwyMusic — Fase A: onboarding "¿Qué te gusta escuchar?".
 * Grid de artistas traídos dinámicamente de InnerTube (sin login de YouTube).
 * El usuario elige ≥10 y se guardan en su cuenta (Supabase).
 */

package com.wiwymusic.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wiwymusic.R
import com.wiwymusic.innertube.YouTube
import com.wiwymusic.innertube.models.ArtistItem
import com.wiwymusic.utils.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val WiwyOrange = Color(0xFFF5791F)
private const val MIN_ARTISTS = 10

@Composable
fun WiwyOnboardingScreen(onDone: () -> Unit) {
    val scope = rememberCoroutineScope()

    var artists by remember { mutableStateOf<List<ArtistItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<String>() }
    val byId = remember { mutableMapOf<String, ArtistItem>() }

    LaunchedEffectArtists { list ->
        artists = list
        list.forEach { byId[it.id] = it }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars),
    ) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)) {
            Text(
                "¿Qué te gusta escuchar?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Selecciona al menos $MIN_ARTISTS artistas para personalizar tu experiencia.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WiwyOrange)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(artists, key = { it.id }) { artist ->
                    val isSel = selected.contains(artist.id)
                    ArtistPickCard(artist, isSel) {
                        if (isSel) selected.remove(artist.id) else selected.add(artist.id)
                    }
                }
            }
        }

        // Botón inferior
        val enough = selected.size >= MIN_ARTISTS
        Button(
            onClick = {
                if (!enough || saving) return@Button
                saving = true
                scope.launch {
                    val picks = selected.mapNotNull { id ->
                        byId[id]?.let { UserPrefs.ArtistPick(it.id, it.title, it.thumbnail) }
                    }
                    UserPrefs.completeOnboarding(picks)
                    saving = false
                    onDone()
                }
            },
            enabled = enough && !saving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WiwyOrange,
                contentColor = Color.White,
                disabledContainerColor = WiwyOrange.copy(alpha = 0.35f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) {
            if (saving) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    if (enough) "Continuar" else "Elige ${MIN_ARTISTS - selected.size} más",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ArtistPickCard(artist: ArtistItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .then(
                        if (selected) Modifier.border(3.dp, WiwyOrange, CircleShape) else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (artist.thumbnail != null) {
                    AsyncImage(
                        model = artist.thumbnail,
                        contentDescription = artist.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.artist),
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(WiwyOrange),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.check), null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            artist.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** Carga los artistas del onboarding desde InnerTube (varios géneros semilla). */
@Composable
private fun LaunchedEffectArtists(onLoaded: (List<ArtistItem>) -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val seeds = listOf(
            "pop", "rock", "reggaeton", "regional mexicano", "corridos",
            "trap latino", "electronica", "hip hop", "cumbia", "indie",
            "k-pop", "banda",
        )
        val result = withContext(Dispatchers.IO) {
            coroutineScope {
                seeds.map { seed ->
                    async {
                        YouTube.search(seed, YouTube.SearchFilter.FILTER_ARTIST)
                            .getOrNull()?.items
                            ?.filterIsInstance<ArtistItem>()
                            ?.take(7)
                            .orEmpty()
                    }
                }.awaitAll().flatten().distinctBy { it.id }
            }
        }
        onLoaded(result.shuffled())
    }
}
