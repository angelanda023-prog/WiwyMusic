/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.wiwymusic.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.wiwymusic.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconIdInactive: Int,
    @DrawableRes val iconIdActive: Int,
    val route: String,
) {
    object Home : Screens(
        titleId = R.string.home,
        iconIdInactive = R.drawable.home_outlined,
        iconIdActive = R.drawable.home_filled,
        route = "home"
    )

    object Search : Screens(
        titleId = R.string.search,
        iconIdInactive = R.drawable.search,
        iconIdActive = R.drawable.search,
        route = "search"
    )

    object Library : Screens(
        titleId = R.string.filter_library,
        iconIdInactive = R.drawable.library_outlined,
        iconIdActive = R.drawable.library_filled,
        route = "library"
    )

    object DownloadQueue : Screens(
        titleId = R.string.download_queue,
        iconIdInactive = R.drawable.downloading,
        iconIdActive = R.drawable.downloading,
        route = "download_queue"
    )

    object Favorites : Screens(
        titleId = R.string.wm_favorites,
        iconIdInactive = R.drawable.favorite_border,
        iconIdActive = R.drawable.favorite_filled,
        route = "favorites"
    )

    object SettingsTab : Screens(
        titleId = R.string.settings,
        iconIdInactive = R.drawable.settings,
        iconIdActive = R.drawable.settings,
        route = "settings"
    )

    object MoodAndGenres : Screens(
        titleId = R.string.mood_and_genres,
        iconIdInactive = R.drawable.style,
        iconIdActive = R.drawable.style,
        route = "mood_and_genres"
    )

    companion object {
        // WiwyMusic: nav inferior -> Inicio, Buscar, Biblioteca, Ajustes
        val MainScreens = listOf(Home, Search, Library, SettingsTab)
    }
}
