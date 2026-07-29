/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */

package com.wiwymusic.ui.screens
import com.wiwymusic.ui.screens.DownloadQueueScreen

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wiwymusic.R
import com.wiwymusic.constants.DarkModeKey
import com.wiwymusic.constants.PureBlackKey
import com.wiwymusic.ui.component.BottomSheet
import com.wiwymusic.ui.component.BottomSheetMenu
import com.wiwymusic.ui.component.LocalMenuState
import com.wiwymusic.ui.component.WidgetSettings
import com.wiwymusic.ui.component.rememberBottomSheetState
import com.wiwymusic.ui.screens.BrowseScreen
import com.wiwymusic.ui.screens.artist.ArtistAlbumsScreen
import com.wiwymusic.ui.screens.artist.ArtistItemsScreen
import com.wiwymusic.ui.screens.artist.ArtistScreen
import com.wiwymusic.ui.screens.artist.ArtistSongsScreen
import com.wiwymusic.ui.screens.library.LibraryScreen
import com.wiwymusic.ui.screens.playlist.AutoPlaylistScreen
import com.wiwymusic.ui.screens.playlist.LocalPlaylistScreen
import com.wiwymusic.ui.screens.playlist.OnlinePlaylistScreen
import com.wiwymusic.ui.screens.playlist.TopPlaylistScreen
import com.wiwymusic.ui.screens.playlist.CachePlaylistScreen
import com.wiwymusic.ui.screens.search.OnlineSearchResult
import com.wiwymusic.ui.screens.settings.AboutScreen
import com.wiwymusic.ui.screens.settings.AccountSettings
import com.wiwymusic.ui.screens.settings.AppearanceSettings
import com.wiwymusic.ui.screens.settings.CustomizeBackground
import com.wiwymusic.ui.screens.settings.BackupAndRestore
import com.wiwymusic.ui.screens.settings.ChangelogScreen
import com.wiwymusic.ui.screens.settings.ContentSettings
import com.wiwymusic.ui.screens.settings.DarkMode
import com.wiwymusic.ui.screens.settings.DiscordLoginScreen
import com.wiwymusic.ui.screens.settings.DiscordSettings
import com.wiwymusic.ui.screens.settings.DebugSettings
import com.wiwymusic.ui.screens.settings.IntegrationScreen
import com.wiwymusic.ui.screens.settings.LastFMSettings
import com.wiwymusic.ui.screens.settings.MusicTogetherScreen
import com.wiwymusic.ui.screens.settings.PalettePickerScreen
import com.wiwymusic.ui.screens.settings.PlayerSettings
import com.wiwymusic.ui.screens.settings.PoTokenScreen
import com.wiwymusic.ui.screens.settings.PrivacySettings
import com.wiwymusic.ui.screens.settings.SettingsScreen
import com.wiwymusic.ui.screens.settings.StorageSettings
import com.wiwymusic.ui.screens.settings.ThemeCreatorScreen
import com.wiwymusic.ui.screens.settings.UpdateScreen
import com.wiwymusic.ui.screens.musicrecognition.MusicRecognitionRoute
import com.wiwymusic.ui.screens.musicrecognition.MusicRecognitionScreen
import com.wiwymusic.ui.screens.playlist.SpotifyPlaylistScreen
import com.wiwymusic.ui.screens.settings.AODSettings
import com.wiwymusic.ui.screens.settings.AndroidAutoSettings

import com.wiwymusic.ui.utils.ShowMediaInfo
import com.wiwymusic.utils.rememberEnumPreference
import com.wiwymusic.utils.rememberPreference

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
    onActivateSearch: () -> Unit = {},
) {
    composable(Screens.Home.route) {
        // WiwyMusic: Inicio con estilo naranja y datos reales
        WiwyHomeScreen(navController)
    }
    composable(Screens.Search.route) {
        // WiwyMusic: página Buscar completa (cabecera + barra + secciones)
        WiwySearchLanding(
            navController = navController,
            onActivateSearch = onActivateSearch,
            onSearch = { q ->
                navController.navigate(
                    "search/${java.net.URLEncoder.encode(q, "UTF-8")}"
                )
            },
        )
    }
    composable(
        Screens.Library.route,
    ) {
        // WiwyMusic: Biblioteca tipo mockup (Favoritos, Playlists, Álbumes, Artistas, Historial)
        WiwyLibraryScreen(navController)
    }
    composable(Screens.Favorites.route) {
        // WiwyMusic: pestaña Favoritos -> canciones que te gustan (auto playlist "liked")
        AutoPlaylistScreen(navController, scrollBehavior)
    }
    composable("auth") {
        // WiwyMusic: acceso al backend propio (Supabase) por correo/contraseña
        com.wiwymusic.ui.screens.auth.WiwyAuthScreen(onAuthenticated = { navController.navigateUp() })
    }
    composable("library/playlists") {
        com.wiwymusic.ui.screens.library.LibraryPlaylistsScreen(navController, filterContent = {})
    }
    composable("library/albums") {
        com.wiwymusic.ui.screens.library.LibraryAlbumsScreen(navController, onDeselect = { navController.navigateUp() })
    }
    composable("library/artists") {
        com.wiwymusic.ui.screens.library.LibraryArtistsScreen(navController, onDeselect = { navController.navigateUp() })
    }
    composable("history") {
        HistoryScreen(navController)
    }
    composable("stats") {
        StatsScreen(navController)
    }
    composable("year_in_music") {
        YearInMusicScreen(navController)
    }
    composable(MusicRecognitionRoute) {
        MusicRecognitionScreen(navController)
    }
    composable(Screens.MoodAndGenres.route) {
        MoodAndGenresScreen(navController)
    }
    composable("account") {
        AccountScreen(navController, scrollBehavior)
    }
    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }
    composable("charts_screen") {
        ChartsScreen(navController)
    }
    composable(
        route = "browse/{browseId}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
            }
        )
    ) {
        BrowseScreen(
            navController,
            scrollBehavior,
            it.arguments?.getString("browseId")
        )
    }
    composable(
        route = "search/{query}",
        arguments =
            listOf(
                navArgument("query") {
                    type = NavType.StringType
                },
            ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }
    composable(
        route = "album/{albumId}",
        arguments =
            listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                },
            ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/songs",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/albums",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            }
        )
    ) {
        ArtistAlbumsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/items?browseId={browseId}&params={params}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }
    composable(
        route = "online_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "local_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "auto_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "cache_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "top_playlist/{top}",
        arguments =
            listOf(
                navArgument("top") {
                    type = NavType.StringType
                },
            ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments =
            listOf(
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        YouTubeBrowseScreen(navController)
    }
    composable("settings") {
        SettingsScreen(navController, scrollBehavior, latestVersionName)
    }
    composable("settings/appearance") {
        AppearanceSettings(navController, scrollBehavior)
    }
    composable("settings/appearance/palette_picker") {
        PalettePickerScreen(navController)
    }
    composable("settings/appearance/theme_creator") {
        ThemeCreatorScreen(navController)
    }
    composable(
        route = "settings/appearance/always_on_display",
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally { it / 3 }
        },
        exitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally { -it / 3 }
        },
        popEnterTransition = {
            fadeIn(tween(300)) + slideInHorizontally { -it / 3 }
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally { it / 3 }
        },
    ) {
        AODSettings(navController, scrollBehavior)
    }


    composable(
        route = "spotify_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        SpotifyPlaylistScreen(navController, scrollBehavior)
    }

    composable("settings/widget") {
        WidgetSettings(navController, scrollBehavior)
    }
    composable("settings/content") {
        ContentSettings(navController, scrollBehavior)
    }
    composable("settings/player") {
        PlayerSettings(navController, scrollBehavior)
    }
    composable("settings/storage") {
        StorageSettings(navController, scrollBehavior)
    }
    composable("settings/privacy") {
        PrivacySettings(navController, scrollBehavior)
    }
    composable("settings/backup_restore") {
        BackupAndRestore(navController, scrollBehavior)
    }
    composable("settings/discord") {
        DiscordSettings(navController, scrollBehavior)
    }
    composable("settings/integration") {
        IntegrationScreen(navController, scrollBehavior)
    }
    composable("settings/music_together") {
        MusicTogetherScreen(navController, scrollBehavior)
    }
    composable("settings/lastfm") {
        LastFMSettings(navController, scrollBehavior)
    }
    composable("settings/discord/experimental") {
        com.wiwymusic.ui.screens.settings.DiscordExperimental(navController)
    }
    composable("settings/misc") {
        DebugSettings(navController)
    }
    composable("settings/update") {
        UpdateScreen(navController, scrollBehavior)
    }
    composable("settings/changelog") {
        ChangelogScreen(navController, scrollBehavior)
    }
    composable("settings/discord/login") {
        DiscordLoginScreen(navController)
    }
    composable("settings/android_auto") {
        AndroidAutoSettings(
            navController,
            scrollBehavior,
            context = androidx.compose.ui.platform.LocalContext.current
        )
    }
    composable("settings/about") {
        AboutScreen(navController, scrollBehavior)
    }
    composable("settings/account") {
        com.wiwymusic.ui.screens.settings.AccountSettings(
            navController = navController,
            onClose = { navController.navigateUp() },
            latestVersionName = com.wiwymusic.BuildConfig.VERSION_NAME,
        )
    }
    composable("settings/downloads") {
        com.wiwymusic.ui.screens.settings.DownloadsSettings(navController, scrollBehavior)
    }
    composable("settings/notifications") {
        com.wiwymusic.ui.screens.settings.NotificationsSettings(navController, scrollBehavior)
    }
    composable("settings/recognize") {
        com.wiwymusic.ui.screens.settings.RecognizeSongScreen(navController, scrollBehavior)
    }
    composable(Screens.DownloadQueue.route) {
        // WiwyMusic: pestaña Descargas tipo mockup (espacio, categorías, calidad, limpiar)
        WiwyDownloadsScreen(navController)
    }
    composable("downloaded_albums") {
        DownloadedAlbumsScreen(navController)
    }
    composable("download_queue_list") {
        DownloadQueueScreen(navController)
    }
    composable("settings/po_token") {
        PoTokenScreen(navController, scrollBehavior)
    }
    composable("customize_background") {
        CustomizeBackground(navController)
    }
    composable(
        route = "$LOGIN_ROUTE?$LOGIN_URL_ARGUMENT={$LOGIN_URL_ARGUMENT}",
        arguments = listOf(
            navArgument(LOGIN_URL_ARGUMENT) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        LoginScreen(
            navController,
            startUrl = backStackEntry.arguments?.getString(LOGIN_URL_ARGUMENT)?.let(Uri::decode)
        )
    }


// ─────────────────────────────────────────────────────────────────────────
// Always On Display — como diálogo que cubre completamente
// ─────────────────────────────────────────────────────────────────────────
    composable(
        route = "always_on_display",
        // Esto la hace un diálogo que se superpone
    ) { backStackEntry ->
        Dialog(
            onDismissRequest = { navController.navigateUp() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AlwaysOnDisplayScreen(navController)
        }
    }

}