/*
 * WiwyMusic — Ajustes de Notificaciones
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.wiwymusic.LocalPlayerAwareWindowInsets
import com.wiwymusic.R
import com.wiwymusic.constants.EnableUpdateNotificationKey
import com.wiwymusic.ui.component.IconButton
import com.wiwymusic.ui.component.PreferenceEntry
import com.wiwymusic.ui.component.PreferenceGroupTitle
import com.wiwymusic.ui.component.SwitchPreference
import com.wiwymusic.ui.utils.backToMain
import com.wiwymusic.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current

    val (enableUpdateNotification, onEnableUpdateNotificationChange) = rememberPreference(
        EnableUpdateNotificationKey, defaultValue = false
    )
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            onEnableUpdateNotificationChange(true)
        }
    }

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

        PreferenceGroupTitle(title = stringResource(R.string.wm_notifications))

        PreferenceEntry(
            title = { Text(stringResource(R.string.wm_open_system_notifications)) },
            description = stringResource(R.string.wm_notifications_desc),
            icon = { Icon(painterResource(R.drawable.notifications), null) },
            onClick = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_update_notification)) },
            description = stringResource(R.string.enable_update_notification_desc),
            icon = { Icon(painterResource(R.drawable.new_release), null) },
            checked = enableUpdateNotification,
            onCheckedChange = { enabled ->
                if (enabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onEnableUpdateNotificationChange(true)
                    }
                } else {
                    onEnableUpdateNotificationChange(false)
                }
            }
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.wm_notifications)) },
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
