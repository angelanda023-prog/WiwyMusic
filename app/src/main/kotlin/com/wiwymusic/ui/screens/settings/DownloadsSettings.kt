/*
 * WiwyMusic — Ajustes de Descargas
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.R
import com.wiwymusic.ui.component.IconButton
import com.wiwymusic.ui.component.PreferenceEntry
import com.wiwymusic.ui.component.PreferenceGroupTitle
import com.wiwymusic.ui.screens.Screens
import com.wiwymusic.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceGroupTitle(title = stringResource(R.string.wm_downloads))

        PreferenceEntry(
            title = { Text(stringResource(R.string.download_queue)) },
            icon = { Icon(painterResource(R.drawable.downloading), null) },
            onClick = { navController.navigate(Screens.DownloadQueue.route) },
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.storage)) },
            description = stringResource(R.string.wm_downloads_storage_desc),
            icon = { Icon(painterResource(R.drawable.storage), null) },
            onClick = { navController.navigate("settings/storage") },
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.wm_downloads)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
