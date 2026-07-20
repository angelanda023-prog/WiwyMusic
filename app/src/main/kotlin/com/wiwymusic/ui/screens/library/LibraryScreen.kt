/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.wiwymusic.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.wiwymusic.LocalDatabase
import com.wiwymusic.R
import com.wiwymusic.constants.ChipSortTypeKey
import com.wiwymusic.constants.DisableBlurKey
import com.wiwymusic.constants.LibraryFilter
import com.wiwymusic.constants.PlaylistTagsFilterKey
import com.wiwymusic.constants.ShowTagsInLibraryKey
import com.wiwymusic.ui.component.ChipsRow
import com.wiwymusic.ui.component.TagsFilterChips
import com.wiwymusic.utils.rememberEnumPreference
import com.wiwymusic.utils.rememberPreference

@Composable
fun LibraryScreen(navController: NavController) {
    var filterType by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
    val (disableBlur) = rememberPreference(DisableBlurKey, true)

    val database = LocalDatabase.current
    val (showTagsInLibrary) = rememberPreference(ShowTagsInLibraryKey, true)
    val (selectedTagsFilter, onSelectedTagsFilterChange) = rememberPreference(PlaylistTagsFilterKey, "")
    val selectedTagIds = remember(selectedTagsFilter) {
        selectedTagsFilter.split(",").filter { it.isNotBlank() }.toSet()
    }

    val filterContent = @Composable {
        Column {
            Row {
                ChipsRow(
                    chips =
                    listOf(
                        // WiwyMusic: orden -> Favoritos, Playlists, Álbumes, Artistas, Historia, Canciones, Spotify
                        LibraryFilter.FAVORITES to stringResource(R.string.filter_favorites),
                        LibraryFilter.PLAYLISTS to stringResource(R.string.filter_playlists),
                        LibraryFilter.ALBUMS to stringResource(R.string.filter_albums),
                        LibraryFilter.ARTISTS to stringResource(R.string.filter_artists),
                        LibraryFilter.HISTORY to stringResource(R.string.history),
                        LibraryFilter.SONGS to stringResource(R.string.filter_songs),
                        LibraryFilter.SPOTIFY to stringResource(R.string.spotify)
                    ),
                    currentValue = filterType,
                    onValueUpdate = {
                        when (it) {
                            // Favoritos e Historia son accesos directos (navegan)
                            LibraryFilter.FAVORITES -> navController.navigate("auto_playlist/liked")
                            LibraryFilter.HISTORY -> navController.navigate("history")
                            else -> filterType =
                                if (filterType == it) {
                                    LibraryFilter.LIBRARY
                                } else {
                                    it
                                }
                        }
                    },
                    icons = mapOf(
                        LibraryFilter.FAVORITES to R.drawable.favorite,
                        LibraryFilter.PLAYLISTS to R.drawable.queue_music,
                        LibraryFilter.ALBUMS to R.drawable.album,
                        LibraryFilter.ARTISTS to R.drawable.person,
                        LibraryFilter.HISTORY to R.drawable.history,
                        LibraryFilter.SONGS to R.drawable.music_note,
                        LibraryFilter.SPOTIFY to R.drawable.spotify_icon
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            if (showTagsInLibrary) {
                TagsFilterChips(
                    database = database,
                    selectedTags = selectedTagIds,
                    onTagToggle = { tag ->
                        val newTags = if (tag.id in selectedTagIds) {
                            selectedTagIds - tag.id
                        } else {
                            selectedTagIds + tag.id
                        }
                        onSelectedTagsFilterChange(newTags.joinToString(","))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    // Capture M3 Expressive colors from theme outside drawBehind
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.secondary
    val color3 = MaterialTheme.colorScheme.tertiary
    val color4 = MaterialTheme.colorScheme.primaryContainer
    val color5 = MaterialTheme.colorScheme.secondaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // M3E Mesh gradient background layer at the top
        if (!disableBlur) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.7f) // Cover top 70% of screen
                    .align(Alignment.TopCenter)
                    .zIndex(-1f) // Place behind all content
                .drawBehind {
                    val width = size.width
                    val height = size.height

                    // Create mesh gradient with 5 color blobs for more variation
                    // First color blob - top left
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color1.copy(alpha = 0.38f),
                                color1.copy(alpha = 0.24f),
                                color1.copy(alpha = 0.14f),
                                color1.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.15f, height * 0.1f),
                            radius = width * 0.55f
                        )
                    )

                    // Second color blob - top right
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color2.copy(alpha = 0.34f),
                                color2.copy(alpha = 0.2f),
                                color2.copy(alpha = 0.11f),
                                color2.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.85f, height * 0.2f),
                            radius = width * 0.65f
                        )
                    )

                    // Third color blob - middle left
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color3.copy(alpha = 0.3f),
                                color3.copy(alpha = 0.17f),
                                color3.copy(alpha = 0.09f),
                                color3.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.3f, height * 0.45f),
                            radius = width * 0.6f
                        )
                    )

                    // Fourth color blob - middle right
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color4.copy(alpha = 0.26f),
                                color4.copy(alpha = 0.14f),
                                color4.copy(alpha = 0.08f),
                                color4.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.7f, height * 0.5f),
                            radius = width * 0.7f
                        )
                    )

                    // Fifth color blob - bottom center (helps with smooth fade)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color5.copy(alpha = 0.22f),
                                color5.copy(alpha = 0.12f),
                                color5.copy(alpha = 0.06f),
                                color5.copy(alpha = 0.02f),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.5f, height * 0.75f),
                            radius = width * 0.8f
                        )
                    )

                    // Add a final vertical gradient overlay to ensure smooth bottom fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                surfaceColor.copy(alpha = 0.22f),
                                surfaceColor.copy(alpha = 0.55f),
                                surfaceColor
                            ),
                            startY = height * 0.4f,
                            endY = height
                        )
                    )
                }
        ) {}
        }

        when (filterType) {
            LibraryFilter.LIBRARY -> LibraryMixScreen(
                navController,
                filterContent,
                onTabSelected = { filterType = it }
            )
            LibraryFilter.PLAYLISTS -> LibraryPlaylistsScreen(navController, filterContent)
            LibraryFilter.SONGS -> LibrarySongsScreen(
                navController,
                { filterType = LibraryFilter.LIBRARY }
            )
            LibraryFilter.ALBUMS -> LibraryAlbumsScreen(
                navController,
                { filterType = LibraryFilter.LIBRARY }
            )
            LibraryFilter.ARTISTS -> LibraryArtistsScreen(
                navController,
                { filterType = LibraryFilter.LIBRARY }
            )

            LibraryFilter.SPOTIFY -> {
                LibrarySpotifyPlaylistsScreen(
                    navController = navController,
                    filterContent = filterContent
                )
            }

            // Accesos directos: navegan desde onValueUpdate, no se renderizan aquí
            LibraryFilter.FAVORITES,
            LibraryFilter.HISTORY -> LibraryMixScreen(
                navController,
                filterContent,
                onTabSelected = { filterType = it }
            )
        }
    }
}
