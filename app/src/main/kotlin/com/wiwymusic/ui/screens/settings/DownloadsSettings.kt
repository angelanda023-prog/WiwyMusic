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
import com.wiwymusic.constants.AudioQuality
import com.wiwymusic.constants.AudioQualityKey
import com.wiwymusic.constants.WifiOnlyDownloadKey
import com.wiwymusic.ui.component.EnumListPreference
import com.wiwymusic.ui.component.IconButton
import com.wiwymusic.ui.component.SwitchPreference
import com.wiwymusic.ui.utils.backToMain
import com.wiwymusic.utils.rememberEnumPreference
import com.wiwymusic.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(AudioQualityKey, AudioQuality.AUTO)
    val (wifiOnly, onWifiOnlyChange) = rememberPreference(WifiOnlyDownloadKey, defaultValue = false)

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState()),
    ) {
        WiwySettingsHeaderContentSpacer()

        EnumListPreference(
            title = { Text(stringResource(R.string.audio_quality)) },
            icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
            selectedValue = audioQuality,
            onValueSelected = onAudioQualityChange,
            valueText = {
                when (it) {
                    AudioQuality.HIGHEST -> stringResource(R.string.audio_quality_max)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            },
        )

        SwitchPreference(
            title = { Text("Descargar solo con Wi-Fi") },
            description = "Pausa las descargas cuando usas datos móviles",
            icon = { Icon(painterResource(R.drawable.wifi), null) },
            checked = wifiOnly,
            onCheckedChange = onWifiOnlyChange,
        )
    }

    WiwySettingsPageHeader(
        title = stringResource(R.string.wm_downloads),
        onBack = navController::navigateUp,
    )
}
