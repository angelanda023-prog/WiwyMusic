/*
 * WiwyMusic — Pantalla de Inicio (estilo naranja, datos reales)
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.wiwymusic.LocalDatabase
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.LocalPlayerConnection
import com.wiwymusic.R
import com.wiwymusic.constants.PremiumPromoLastShownAtKey
import com.wiwymusic.db.entities.Song
import com.wiwymusic.innertube.YouTube
import com.wiwymusic.innertube.models.AlbumItem
import com.wiwymusic.innertube.models.ArtistItem
import com.wiwymusic.innertube.models.PlaylistItem
import com.wiwymusic.innertube.models.SongItem
import com.wiwymusic.innertube.models.WatchEndpoint
import com.wiwymusic.innertube.models.YTItem
import com.wiwymusic.models.toMediaMetadata
import com.wiwymusic.models.MediaMetadata
import com.wiwymusic.playback.queues.YouTubeQueue
import com.wiwymusic.utils.SupabaseAuth
import com.wiwymusic.utils.UserPrefs
import com.wiwymusic.utils.PremiumContact
import com.wiwymusic.utils.PremiumLaunchPromo
import com.wiwymusic.utils.dataStore
import com.wiwymusic.utils.getAsync
import com.wiwymusic.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Calendar

private val WiwyOrange = Color(0xFFF5791F)
private val WiwyBg = Color(0xFF0A0A0C)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

private val tileGradients = listOf(
    listOf(Color(0xFFF5791F), Color(0xFFE0322E)),
    listOf(Color(0xFF5B33D6), Color(0xFF9B2FB8)),
    listOf(Color(0xFF109E7A), Color(0xFF0E6E8C)),
    listOf(Color(0xFFD6337A), Color(0xFF8C2FB8)),
)

private val genreIcons = listOf(
    R.drawable.fire, R.drawable.album, R.drawable.headphones, R.drawable.equalizer, R.drawable.music_note
)
private val genreTints = listOf(
    WiwyOrange, Color(0xFFB86BFF), Color(0xFF2FD0C0), Color(0xFF2FC08C), Color(0xFFF5B301)
)

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "¡Buenos días"
    in 12..19 -> "¡Buenas tardes"
    else -> "¡Buenas noches"
}

private data class FeaturedSong(
    val metadata: MediaMetadata,
)

@Composable
private fun PremiumPromoDialog(
    onDismiss: () -> Unit,
    onObtain: () -> Unit,
) {
    val imageDescription = stringResource(R.string.premium_promo_image_description)
    val obtainDescription = stringResource(R.string.premium_promo_obtain)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val adSize = minOf(maxWidth * 0.94f, maxHeight * 0.82f)
            Box(modifier = Modifier.size(adSize)) {
                Image(
                    painter = painterResource(R.drawable.wiwymusic_premium_launch),
                    contentDescription = imageDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = adSize * 0.055f)
                        .fillMaxWidth(0.66f)
                        .height(adSize * 0.115f)
                        .clip(RoundedCornerShape(adSize * 0.04f))
                        .clickable(onClick = onObtain)
                        .semantics {
                            contentDescription = obtainDescription
                            role = Role.Button
                        },
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(WiwyOrange),
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = stringResource(R.string.premium_promo_dismiss),
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
fun WiwyHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current
    val keepListening by viewModel.keepListening.collectAsState()
    val quickPicks by viewModel.quickPicks.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val accountName by viewModel.accountName.collectAsState()
    val supabaseSession by SupabaseAuth.session.collectAsState()
    val onboardingCompleted by UserPrefs.onboarded.collectAsState()
    val isPremium by UserPrefs.isPremium.collectAsState()
    val context = LocalContext.current

    // Continuar escuchando: historial real de reproducción
    val database = LocalDatabase.current
    val recentEventsFlow = remember { database.events() }
    val recentEvents by recentEventsFlow.collectAsState(initial = emptyList())
    val continueSongs = recentEvents.map { it.song }.distinctBy { it.song.id }.take(12)
    val featuredSongs = remember(quickPicks, continueSongs, homePage) {
        quickPicks.orEmpty()
            .take(5)
            .map { FeaturedSong(it.toMediaMetadata()) }
            .ifEmpty {
                continueSongs
                    .take(5)
                    .map { FeaturedSong(it.toMediaMetadata()) }
                    .ifEmpty {
                        homePage?.sections.orEmpty()
                            .flatMap { it.items }
                            .filterIsInstance<SongItem>()
                            .distinctBy { it.id }
                            .take(5)
                            .map { FeaturedSong(it.toMediaMetadata()) }
                    }
            }
    }

    val insets = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    val premiumPromoGeneration by PremiumLaunchPromo.foregroundGeneration.collectAsState()
    var showPremiumPromo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        PremiumLaunchPromo.initialize()
    }
    LaunchedEffect(premiumPromoGeneration, supabaseSession?.userId, isPremium) {
        val nowMillis = System.currentTimeMillis()
        val lastShownAtMillis = context.dataStore.getAsync(PremiumPromoLastShownAtKey, 0L)
        if (PremiumLaunchPromo.claimIfEligible(
                foregroundGeneration = premiumPromoGeneration,
                hasSession = supabaseSession != null,
                isPremium = isPremium,
                lastShownAtMillis = lastShownAtMillis,
                nowMillis = nowMillis,
            )
        ) {
            context.dataStore.edit { preferences ->
                preferences[PremiumPromoLastShownAtKey] = nowMillis
            }
            showPremiumPromo = true
        }
    }

    if (showPremiumPromo) {
        PremiumPromoDialog(
            onDismiss = { showPremiumPromo = false },
            onObtain = {
                PremiumContact.open(context)
                showPremiumPromo = false
            },
        )
    }

    // Fase B: inicio personalizado según artistas preferidos (Supabase)
    var personalized by remember { mutableStateOf<List<Pair<String, List<SongItem>>>>(emptyList()) }
    LaunchedEffect(supabaseSession?.userId, onboardingCompleted) {
        personalized = emptyList()
        if (supabaseSession == null || onboardingCompleted != true) return@LaunchedEffect

        val artists = UserPrefs.getPreferredArtists().shuffled().take(4)
        if (artists.isEmpty()) return@LaunchedEffect
        val sections = withContext(Dispatchers.IO) {
            coroutineScope {
                artists.map { a ->
                    async {
                        val artistSongs = YouTube.artist(a.id).getOrNull()
                            ?.sections?.firstNotNullOfOrNull { sec ->
                                sec.items.filterIsInstance<SongItem>().takeIf { it.isNotEmpty() }
                            }
                            ?.take(10).orEmpty()
                        val songs = artistSongs.ifEmpty {
                            YouTube.search(a.name, YouTube.SearchFilter.FILTER_SONG)
                                .getOrNull()
                                ?.items
                                ?.filterIsInstance<SongItem>()
                                ?.filter { song ->
                                    song.artists.any { artist ->
                                        artist.id == a.id || artist.name.equals(a.name, ignoreCase = true)
                                    }
                                }
                                ?.take(10)
                                .orEmpty()
                        }
                        a.name to songs
                    }
                }.awaitAll()
            }
        }
        personalized = sections.filter { it.second.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WiwyBg),
        contentPadding = PaddingValues(
            top = insets.calculateTopPadding() + 8.dp,
            bottom = insets.calculateBottomPadding() + 24.dp,
        ),
    ) {
        item {
            val firstName = accountName
                .takeIf { it.isNotBlank() && it != "Guest" }?.trim()?.split(" ")?.firstOrNull()
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)) {
                Text(
                    if (firstName != null) "${greeting()}, $firstName!" else "${greeting()}!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text("Disfruta tu música favorita", color = WiwyMuted, fontSize = 14.sp)
            }
        }

        // Carrusel destacado
        featuredSongs.takeIf { it.isNotEmpty() }?.let { songs ->
            item { FeaturedCarousel(songs, playerConnection) }
        }

        // Fase B: "Porque te gusta X" según artistas preferidos
        personalized.forEach { (artistName, songs) ->
            item(key = "pers_$artistName") { SectionHeader("Porque te gusta $artistName") }
            item(key = "pers_row_$artistName") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(songs, key = { it.id }) { song ->
                        ArtCard(song.title, song.artists.joinToString { it.name }, song.thumbnail) {
                            playerConnection?.playQueue(
                                YouTubeQueue(
                                    song.endpoint ?: WatchEndpoint(videoId = song.id),
                                    song.toMediaMetadata(),
                                )
                            )
                        }
                    }
                }
            }
        }

        // Continuar escuchando (historial real; respaldo: keepListening)
        if (continueSongs.isNotEmpty()) {
            item { SectionHeader("Continuar escuchando") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(continueSongs) { song ->
                        ArtCard(song.title, song.artists.joinToString { it.name }, song.thumbnailUrl) {
                            playerConnection?.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                        }
                    }
                }
            }
        } else keepListening?.takeIf { it.isNotEmpty() }?.let { items ->
            item { SectionHeader("Continuar escuchando") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(items) { item ->
                        ArtCard(item.title, null, item.thumbnailUrl) {
                            (item as? Song)?.let { s ->
                                playerConnection?.playQueue(YouTubeQueue.radio(s.toMediaMetadata()))
                            }
                        }
                    }
                }
            }
        }

        // Recomendado para ti (quickPicks reales)
        quickPicks?.takeIf { it.isNotEmpty() }?.let { songs ->
            item { SectionHeader("Recomendado para ti") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(songs) { song ->
                        ArtCard(song.title, song.artists.joinToString { it.name }, song.thumbnailUrl) {
                            playerConnection?.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                        }
                    }
                }
            }
        }

        // Nuevos lanzamientos (álbumes reales)
        explorePage?.newReleaseAlbums?.takeIf { it.isNotEmpty() }?.let { albums ->
            item { SectionHeader("Nuevos lanzamientos") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(albums) { album ->
                        YtItemCard(album, playerConnection, navController)
                    }
                }
            }
        }

        // Playlists populares (reales del home)
        val popularPlaylists = homePage?.sections?.flatMap { it.items }
            ?.filterIsInstance<com.wiwymusic.innertube.models.PlaylistItem>()
            ?.distinctBy { it.id }?.take(10).orEmpty()
        if (popularPlaylists.isNotEmpty()) {
            item { SectionHeader("Playlists populares") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(popularPlaylists) { pl ->
                        YtItemCard(pl, playerConnection, navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCarousel(
    songs: List<FeaturedSong>,
    playerConnection: com.wiwymusic.playback.PlayerConnection?,
) {
    val pagerState = rememberPagerState { songs.size }
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
        ) { page ->
            val song = songs[page].metadata
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(WiwyCard),
            ) {
                if (song.thumbnailUrl != null) {
                    AsyncImage(
                        model = song.thumbnailUrl,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        ),
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WiwyOrange)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("DESTACADA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(song.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artists.joinToString { it.name }, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .width(168.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { playerConnection?.playQueue(YouTubeQueue.radio(song)) }
                            .padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(R.drawable.play), null, tint = WiwyOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Reproducir",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(songs.size) { i ->
                val selected = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) WiwyOrange else WiwyMuted.copy(alpha = 0.4f)),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ArtCard(title: String, subtitle: String?, thumbnailUrl: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(WiwyCard),
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(model = thumbnailUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(painterResource(R.drawable.music_note), null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.Center).size(44.dp))
            }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(38.dp).clip(CircleShape).background(WiwyOrange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.play), null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) {
            Text(subtitle, color = WiwyMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun YtItemCard(
    item: YTItem,
    playerConnection: com.wiwymusic.playback.PlayerConnection?,
    navController: NavController,
) {
    val isArtist = item is ArtistItem
    val subtitle = when (item) {
        is SongItem -> item.artists.joinToString { it.name }
        is AlbumItem -> item.artists?.joinToString { it.name }
        is ArtistItem -> null
        is PlaylistItem -> item.author?.name
    }
    Column(
        modifier = Modifier.width(150.dp).clickable {
            when (item) {
                is SongItem -> playerConnection?.playQueue(
                    YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata())
                )
                is AlbumItem -> navController.navigate("album/${item.id}")
                is ArtistItem -> navController.navigate("artist/${item.id}")
                is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(if (isArtist) CircleShape else RoundedCornerShape(16.dp))
                .background(WiwyCard),
        ) {
            if (item.thumbnail != null) {
                AsyncImage(model = item.thumbnail, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(painterResource(R.drawable.music_note), null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.Center).size(44.dp))
            }
            if (item is SongItem) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(38.dp).clip(CircleShape).background(WiwyOrange),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.play), null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, color = WiwyMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GradientTile(title: String, icon: Int, colors: List<Color>, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlaylistTile(title: String, thumbnail: String?, colors: List<Color>, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(colors)),
        ) {
            if (thumbnail != null) {
                AsyncImage(model = thumbnail, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
