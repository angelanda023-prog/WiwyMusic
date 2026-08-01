/*
 * WiwyMusic — Fase A: onboarding "¿Qué te gusta escuchar?".
 * Grid de artistas traídos dinámicamente de InnerTube (sin login de YouTube).
 * El usuario elige entre 1 y 10 y se guardan en su cuenta (Supabase).
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private val WiwyOrange = Color(0xFFF5791F)
private const val MIN_ARTISTS = 1
private const val MAX_ARTISTS = 10
private const val ARTIST_REQUEST_TIMEOUT_MS = 5_000L

private object OnboardingArtistCache {
    var artists: List<ArtistItem> = emptyList()
}

@Composable
fun WiwyOnboardingScreen(onDone: () -> Unit) {
    val scope = rememberCoroutineScope()

    var artists by remember { mutableStateOf<List<ArtistItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val selected = remember { mutableStateListOf<String>() }
    val byId = remember { mutableMapOf<String, ArtistItem>() }

    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ArtistItem>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    LaunchedEffectArtists(
        onProgress = { list ->
            artists = list
            list.forEach { byId[it.id] = it }
            if (list.isNotEmpty()) loading = false
        },
        onFinished = { loading = false },
    )

    // Búsqueda de artistas (con debounce)
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            searchResults = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(350)
        val results = withContext(Dispatchers.IO) {
            withTimeoutOrNull(ARTIST_REQUEST_TIMEOUT_MS) {
                YouTube.search(q, YouTube.SearchFilter.FILTER_ARTIST)
                    .getOrNull()?.items
                    ?.filterIsInstance<ArtistItem>()
                    ?.take(24)
                    .orEmpty()
            }.orEmpty()
        }
        results.forEach { byId[it.id] = it }
        searchResults = results
        searching = false
    }

    val displayed = if (query.trim().length >= 2) searchResults else artists

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
                "Elige de 1 a $MAX_ARTISTS artistas para personalizar tu experiencia.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Buscador de artistas
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar artista…") },
            leadingIcon = { Icon(painterResource(R.drawable.search), null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WiwyOrange,
                cursorColor = WiwyOrange,
                focusedLeadingIconColor = WiwyOrange,
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        )

        if (loading && query.isBlank()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WiwyOrange)
            }
        } else if (searching) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WiwyOrange)
            }
        } else if (displayed.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    if (query.trim().length >= 2) "Sin resultados" else "—",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(displayed, key = { it.id }) { artist ->
                    val isSel = selected.contains(artist.id)
                    ArtistPickCard(artist, isSel) {
                        if (isSel) selected.remove(artist.id)
                        else if (selected.size < MAX_ARTISTS) selected.add(artist.id)
                    }
                }
            }
        }

        // Botón inferior
        val enough = selected.size >= MIN_ARTISTS
        if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(4.dp))
        }
        Button(
            onClick = {
                if (!enough || saving) return@Button
                saving = true
                error = null
                scope.launch {
                    val picks = selected.mapNotNull { id ->
                        byId[id]?.let { UserPrefs.ArtistPick(it.id, it.title, it.thumbnail) }
                    }
                    val result = UserPrefs.completeOnboarding(picks)
                    saving = false
                    result.fold(
                        onSuccess = { onDone() },
                        onFailure = {
                            error = "No se pudo guardar. Revisa tu conexión e intenta de nuevo."
                        },
                    )
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
                    if (enough) "Continuar (${selected.size}/$MAX_ARTISTS)" else "Elige al menos 1 artista",
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

/**
 * Carga artistas desde InnerTube de forma progresiva. Solo mantiene tres peticiones activas,
 * publica cada lote apenas termina y reutiliza el resultado mientras viva el proceso.
 */
@Composable
private fun LaunchedEffectArtists(
    onProgress: (List<ArtistItem>) -> Unit,
    onFinished: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        OnboardingArtistCache.artists.takeIf { it.isNotEmpty() }?.let { cached ->
            onProgress(cached)
            onFinished()
            return@LaunchedEffect
        }

        val seeds = listOf(
            "pop", "rock", "reggaeton", "regional mexicano", "corridos",
            "trap latino", "electronica", "hip hop", "cumbia", "indie",
            "k-pop", "banda",
        ).shuffled()
        val accumulated = linkedMapOf<String, ArtistItem>()
        val semaphore = Semaphore(permits = 3)
        val results = Channel<List<ArtistItem>>(capacity = Channel.UNLIMITED)

        coroutineScope {
            val jobs = seeds.map { seed ->
                launch(Dispatchers.IO) {
                    val batch = semaphore.withPermit {
                        withTimeoutOrNull(ARTIST_REQUEST_TIMEOUT_MS) {
                            YouTube.search(seed, YouTube.SearchFilter.FILTER_ARTIST)
                                .getOrNull()?.items
                                ?.filterIsInstance<ArtistItem>()
                                ?.take(7)
                                .orEmpty()
                        }.orEmpty()
                    }
                    results.send(batch)
                }
            }
            launch {
                jobs.joinAll()
                results.close()
            }

            for (batch in results) {
                var changed = false
                batch.forEach { artist ->
                    if (accumulated.putIfAbsent(artist.id, artist) == null) changed = true
                }
                if (changed) onProgress(accumulated.values.toList())
            }
        }

        OnboardingArtistCache.artists = accumulated.values.toList()
        onFinished()
    }
}
