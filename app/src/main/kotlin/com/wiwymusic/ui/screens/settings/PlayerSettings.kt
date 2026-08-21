/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.wiwymusic.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.R
import com.wiwymusic.constants.ArtistSeparatorsKey
import com.wiwymusic.constants.ExternalDownloaderEnabledKey
import com.wiwymusic.constants.ExternalDownloaderPackageKey
import com.wiwymusic.constants.DynamicIslandEnabledKey
import com.wiwymusic.constants.AudioNormalizationKey
import com.wiwymusic.constants.AudioOffload
import com.wiwymusic.constants.AudioQuality
import com.wiwymusic.constants.AudioQualityKey
import com.wiwymusic.constants.MaximumQualityWifiOnlyKey
import com.wiwymusic.constants.AutoDownloadOnLikeKey
import com.wiwymusic.constants.AutoStartOnBluetoothKey
import com.wiwymusic.constants.AutoSkipNextOnErrorKey
import com.wiwymusic.constants.PauseOnDeviceMuteKey
import com.wiwymusic.constants.PermanentShuffleKey
import com.wiwymusic.constants.PersistentQueueKey

import com.wiwymusic.constants.SkipSilenceKey
import com.wiwymusic.constants.StopMusicOnTaskClearKey
import com.wiwymusic.constants.WakelockKey
import com.wiwymusic.constants.HistoryDuration
import com.wiwymusic.constants.AudioCrossfadeDurationKey
import com.wiwymusic.constants.PlayerStreamClient
import com.wiwymusic.constants.PlayerStreamClientKey
import com.wiwymusic.constants.SeekExtraSeconds
import com.wiwymusic.ui.component.ArtistSeparatorsDialog
import com.wiwymusic.ui.component.TagsManagementDialog
import com.wiwymusic.ui.component.TextFieldDialog
import com.wiwymusic.ui.component.EnumListPreference
import com.wiwymusic.ui.component.IconButton
import com.wiwymusic.ui.component.ListDialog
import com.wiwymusic.ui.component.PreferenceEntry
import com.wiwymusic.ui.component.PreferenceGroupTitle
import com.wiwymusic.ui.component.SliderPreference
import com.wiwymusic.ui.component.CrossfadeSliderPreference
import com.wiwymusic.ui.component.SwitchPreference
import com.wiwymusic.ui.component.PremiumFeatureDialog
import com.wiwymusic.ui.component.PremiumLockBadge
import com.wiwymusic.ui.utils.backToMain
import com.wiwymusic.utils.rememberEnumPreference
import com.wiwymusic.utils.rememberPreference
import com.wiwymusic.utils.DynamicIslandController
import com.wiwymusic.utils.UserPrefs
import com.wiwymusic.LocalDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val isPremium by UserPrefs.isPremium.collectAsState()
    var showDynamicIslandPremiumDialog by remember { mutableStateOf(false) }
    val (dynamicIslandEnabled, onDynamicIslandEnabledChange) = rememberPreference(
        DynamicIslandEnabledKey,
        defaultValue = false,
    )
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val granted = Settings.canDrawOverlays(context)
        onDynamicIslandEnabledChange(granted)
        DynamicIslandController.onPreferenceChanged(granted)
    }
    if (showDynamicIslandPremiumDialog) {
        PremiumFeatureDialog(
            featureName = stringResource(R.string.dynamic_island_title),
            onDismiss = { showDynamicIslandPremiumDialog = false },
        )
    }
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val (playerStreamClient, onPlayerStreamClientChange) = rememberEnumPreference(
        PlayerStreamClientKey,
        defaultValue = PlayerStreamClient.ANDROID_VR
    )
    val (maximumQualityWifiOnly, onMaximumQualityWifiOnlyChange) = rememberPreference(
        MaximumQualityWifiOnlyKey,
        defaultValue = true
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(
        PersistentQueueKey,
        defaultValue = true
    )
    val (permanentShuffle, onPermanentShuffleChange) = rememberPreference(
        PermanentShuffleKey,
        defaultValue = false
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        AudioNormalizationKey,
        defaultValue = true
    )
    val (wifiOnlyDownload, onWifiOnlyDownloadChange) = rememberPreference(
        com.wiwymusic.constants.WifiOnlyDownloadKey,
        defaultValue = false
    )
    val (audioOffload, onAudioOffloadChange) = rememberPreference(
        AudioOffload,
        defaultValue = false
    )

    val (seekExtraSeconds, onSeekExtraSeconds) = rememberPreference(
        SeekExtraSeconds,
        defaultValue = false
    )

    val (autoDownloadOnLike, onAutoDownloadOnLikeChange) = rememberPreference(
        AutoDownloadOnLikeKey,
        defaultValue = false
    )
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(
        AutoSkipNextOnErrorKey,
        defaultValue = false
    )
    val (pauseOnDeviceMute, onPauseOnDeviceMuteChange) = rememberPreference(
        PauseOnDeviceMuteKey,
        defaultValue = false
    )
    val (autoStartOnBluetooth, onAutoStartOnBluetoothChange) = rememberPreference(
        AutoStartOnBluetoothKey,
        defaultValue = false
    )
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(
        StopMusicOnTaskClearKey,
        defaultValue = false
    )
    val (historyDuration, onHistoryDurationChange) = rememberPreference(
        HistoryDuration,
        defaultValue = 30f
    )

    val (audioCrossfadeSeconds, onAudioCrossfadeSecondsChange) = rememberPreference(
        AudioCrossfadeDurationKey,
        defaultValue = 0
    )

    val (artistSeparators, onArtistSeparatorsChange) = rememberPreference(
        ArtistSeparatorsKey,
        defaultValue = ",;/&"
    )
    val (externalDownloaderEnabled, onExternalDownloaderEnabledChange) = rememberPreference(
        ExternalDownloaderEnabledKey,
        defaultValue = false
    )
    val (externalDownloaderPackage, onExternalDownloaderPackageChange) = rememberPreference(
        ExternalDownloaderPackageKey,
        defaultValue = ""
    )

    val (wakelockEnabled, onWakelockChange) = rememberPreference(
        WakelockKey,
        defaultValue = false
    )

    var showArtistSeparatorsDialog by remember { mutableStateOf(false) }
    var showTagsManagementDialog by remember { mutableStateOf(false) }
    var showPlayerStreamClientDialog by remember { mutableStateOf(false) }
    var showExternalDownloaderPackageDialog by remember { mutableStateOf(false) }
    val database = LocalDatabase.current

    if (showArtistSeparatorsDialog) {
        ArtistSeparatorsDialog(
            currentSeparators = artistSeparators,
            onDismiss = { showArtistSeparatorsDialog = false },
            onSave = { newSeparators ->
                onArtistSeparatorsChange(newSeparators)
                showArtistSeparatorsDialog = false
            }
        )
    }

    if (showTagsManagementDialog) {
        TagsManagementDialog(
            database = database,
            onDismiss = { showTagsManagementDialog = false }
        )
    }

    if (showExternalDownloaderPackageDialog) {
        TextFieldDialog(
            initialTextFieldValue = androidx.compose.ui.text.input.TextFieldValue(externalDownloaderPackage),
            onDone = { pkg ->
                onExternalDownloaderPackageChange(pkg)
                showExternalDownloaderPackageDialog = false
            },
            onDismiss = { showExternalDownloaderPackageDialog = false },
            singleLine = true,
            maxLines = 1,
        )
    }

    if (showPlayerStreamClientDialog) {
        ListDialog(
            onDismiss = { showPlayerStreamClientDialog = false },
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            items(listOf(PlayerStreamClient.ANDROID_VR, PlayerStreamClient.WEB_REMIX)) { value ->
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPlayerStreamClientChange(value)
                            showPlayerStreamClientDialog = false
                        }.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    RadioButton(
                        selected = value == playerStreamClient,
                        onClick = null,
                    )

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text =
                            when (value) {
                                PlayerStreamClient.ANDROID_VR -> stringResource(R.string.player_stream_client_android_vr)
                                else -> stringResource(R.string.player_stream_client_web_remix)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text =
                            when (value) {
                                PlayerStreamClient.ANDROID_VR -> stringResource(R.string.player_stream_client_android_vr_desc)
                                else -> stringResource(R.string.player_stream_client_web_remix_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            WiwySettingsPageHeader(
                title = stringResource(R.string.player_and_audio),
                onBack = navController::navigateUp,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
        ) {

        PreferenceGroupTitle(
            title = stringResource(R.string.player)
        )

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
            }
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.maximum_quality_wifi_only)) },
            description = stringResource(R.string.maximum_quality_wifi_only_desc),
            icon = { Icon(painterResource(R.drawable.wifi), null) },
            checked = maximumQualityWifiOnly,
            onCheckedChange = onMaximumQualityWifiOnlyChange,
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.audio_normalization)) },
            icon = { Icon(painterResource(R.drawable.volume_up), null) },
            checked = audioNormalization,
            onCheckedChange = onAudioNormalizationChange
        )

        SwitchPreference(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.dynamic_island_title))
                    if (isPremium != true) {
                        Spacer(Modifier.width(7.dp))
                        PremiumLockBadge()
                    }
                }
            },
            description = stringResource(R.string.dynamic_island_description),
            icon = { Icon(painterResource(R.drawable.music_note), null) },
            checked =
                isPremium == true &&
                    dynamicIslandEnabled &&
                    Settings.canDrawOverlays(context),
            onCheckedChange = { enabled ->
                if (isPremium != true) {
                    showDynamicIslandPremiumDialog = true
                } else if (!enabled) {
                    onDynamicIslandEnabledChange(false)
                    DynamicIslandController.onPreferenceChanged(false)
                } else if (Settings.canDrawOverlays(context)) {
                    onDynamicIslandEnabledChange(true)
                    DynamicIslandController.onPreferenceChanged(true)
                } else {
                    overlayPermissionLauncher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                }
            },
        )

        CrossfadeSliderPreference(
            value = audioCrossfadeSeconds,
            onValueChange = onAudioCrossfadeSecondsChange,
            isEnabled = !audioOffload,
        )

        SwitchPreference(
            title = { Text("Descargar solo con Wi-Fi") },
            description = "Pausa las descargas cuando usas datos móviles",
            icon = { Icon(painterResource(R.drawable.wifi), null) },
            checked = wifiOnlyDownload,
            onCheckedChange = onWifiOnlyDownloadChange
        )
    }

    }
}
