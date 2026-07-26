/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */

package com.wiwymusic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.wiwymusic.BuildConfig
import com.wiwymusic.R

@Composable
fun buildQuickActions(
    navController: NavController,
    resetSearch: () -> Unit,
): List<SettingsQuickAction> =
    listOf(
        SettingsQuickAction(
            icon = painterResource(R.drawable.palette),
            label = stringResource(R.string.appearance),
            onClick = { resetSearch(); navController.navigate("settings/appearance") },
            accentColor = MaterialTheme.colorScheme.primary,
        ),
        SettingsQuickAction(
            icon = painterResource(R.drawable.play),
            label = stringResource(R.string.player_and_audio),
            onClick = { resetSearch(); navController.navigate("settings/player") },
            accentColor = MaterialTheme.colorScheme.tertiary,
        ),
        SettingsQuickAction(
            icon = painterResource(R.drawable.storage),
            label = stringResource(R.string.storage),
            onClick = { resetSearch(); navController.navigate("settings/storage") },
            accentColor = MaterialTheme.colorScheme.secondary,
        ),
        SettingsQuickAction(
            icon = painterResource(R.drawable.security),
            label = stringResource(R.string.privacy),
            onClick = { resetSearch(); navController.navigate("settings/privacy") },
            accentColor = MaterialTheme.colorScheme.error,
        ),
    )

@Composable
fun buildIntegrationActions(
    navController: NavController,
    resetSearch: () -> Unit,
): List<SettingsIntegrationAction> =
    listOf(
        SettingsIntegrationAction(
            icon = painterResource(R.drawable.bedtime),
            label = stringResource(R.string.AOD),
            onClick = { resetSearch(); navController.navigate("settings/appearance/always_on_display") },
            accentColor = Color(0xFFE3F2FD),
        ),
        SettingsIntegrationAction(
            icon = painterResource(R.drawable.discord),
            label = stringResource(R.string.discord),
            onClick = { resetSearch(); navController.navigate("settings/discord") },
            accentColor = Color(0xFF5865F2),
        ),
        SettingsIntegrationAction(
            icon = painterResource(R.drawable.integration),
            label = stringResource(R.string.integration),
            onClick = { resetSearch(); navController.navigate("settings/integration") },
            accentColor = MaterialTheme.colorScheme.secondary,
        ),
        SettingsIntegrationAction(
            icon = painterResource(R.drawable.fire),
            label = stringResource(R.string.music_together),
            onClick = { resetSearch(); navController.navigate("settings/music_together") },
            accentColor = MaterialTheme.colorScheme.tertiary,
        ),
    )

@Composable
fun buildSettingsGroups(
    navController: NavController,
    isAndroid12OrLater: Boolean,
    hasUpdate: Boolean,
    context: Context,
    resetSearch: () -> Unit,
): List<SettingsGroup> =
    buildList {
        val orange = androidx.compose.ui.graphics.Color(0xFFF5791F)
        add(
            SettingsGroup(
                title = "CUENTA",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.person),
                        title = "Cuenta",
                        subtitle = "Gestiona tu perfil",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/account") },
                    ),
                ),
            ),
        )
        add(
            SettingsGroup(
                title = "REPRODUCCIÓN",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.music_note),
                        title = "Audio",
                        subtitle = "Calidad, normalización y descargas",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/player") },
                    ),
                ),
            ),
        )
        add(
            SettingsGroup(
                title = "PERSONALIZACIÓN",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = "Apariencia",
                        subtitle = "Tema e idioma",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/appearance") },
                    ),
                ),
            ),
        )
        add(
            SettingsGroup(
                title = "NOTIFICACIONES Y PRIVACIDAD",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.notifications),
                        title = "Notificaciones",
                        subtitle = "Preferencias de notificaciones",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/notifications") },
                    ),
                    SettingsItem(
                        icon = painterResource(R.drawable.security),
                        title = "Privacidad",
                        subtitle = "Historial, datos y actividad",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/privacy") },
                    ),
                ),
            ),
        )
        add(
            SettingsGroup(
                title = "ALMACENAMIENTO",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.storage),
                        title = "Almacenamiento",
                        subtitle = "Liberar espacio",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/storage") },
                    ),
                ),
            ),
        )
        add(
            SettingsGroup(
                title = "INFORMACIÓN",
                items = listOf(
                    SettingsItem(
                        icon = painterResource(R.drawable.update),
                        title = "Actualizaciones",
                        subtitle = if (hasUpdate) "Nueva versión disponible" else "Buscar actualizaciones",
                        showUpdateIndicator = hasUpdate,
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/update") },
                    ),
                    SettingsItem(
                        icon = painterResource(R.drawable.info),
                        title = "Acerca de",
                        subtitle = "Versión y licencias",
                        accentColor = orange,
                        onClick = { resetSearch(); navController.navigate("settings/about") },
                    ),
                ),
            ),
        )
    }

@Composable
fun buildInternalItems(
    navController: NavController,
    resetSearch: () -> Unit,
): List<SettingsItem> =
    listOf(
        SettingsItem(
            icon = painterResource(R.drawable.palette),
            title = stringResource(R.string.theme_creator_title),
            subtitle = stringResource(R.string.theme_creator_subtitle),
            accentColor = MaterialTheme.colorScheme.primary,
            keywords = listOf("theme", "creator", "seed", "material", "palette", "import", "export"),
            onClick = { resetSearch(); navController.navigate("settings/appearance/theme_creator") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.palette),
            title = stringResource(R.string.customize_colors),
            subtitle = stringResource(R.string.appearance),
            accentColor = MaterialTheme.colorScheme.primary,
            keywords = listOf("palette", "color", "accent", "tone", "dynamic color"),
            onClick = { resetSearch(); navController.navigate("settings/appearance/palette_picker") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.image),
            title = stringResource(R.string.customize_background_title),
            subtitle = stringResource(R.string.appearance),
            accentColor = MaterialTheme.colorScheme.secondary,
            keywords = listOf("background", "wallpaper", "image", "blur", "gradient"),
            onClick = { resetSearch(); navController.navigate("customize_background") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.discord),
            title = stringResource(R.string.discord_integration),
            subtitle = stringResource(R.string.integration),
            accentColor = Color(0xFF5865F2),
            keywords = listOf("discord", "rpc", "rich presence", "status", "activity"),
            onClick = { resetSearch(); navController.navigate("settings/discord") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.security),
            title = stringResource(R.string.advanced_login),
            subtitle = stringResource(R.string.discord),
            accentColor = Color(0xFF5865F2),
            keywords = listOf("token", "login", "authentication", "discord login"),
            onClick = { resetSearch(); navController.navigate("settings/discord/login") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.experiment),
            title = stringResource(R.string.experimental_features),
            subtitle = stringResource(R.string.experimental_features_description),
            accentColor = MaterialTheme.colorScheme.tertiary,
            keywords = listOf("experimental", "labs", "advanced", "discord experimental", "internal"),
            onClick = { resetSearch(); navController.navigate("settings/discord/experimental") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.integration),
            title = stringResource(R.string.lastfm_integration),
            subtitle = stringResource(R.string.integration),
            accentColor = MaterialTheme.colorScheme.secondary,
            keywords = listOf("lastfm", "last.fm", "scrobble", "listening history"),
            onClick = { resetSearch(); navController.navigate("settings/lastfm") },
        ),
        SettingsItem(
            icon = painterResource(R.drawable.fire),
            title = stringResource(R.string.music_together),
            subtitle = stringResource(R.string.integration),
            accentColor = MaterialTheme.colorScheme.tertiary,
            keywords = listOf("together", "session", "sync", "party", "join", "host"),
            onClick = { resetSearch(); navController.navigate("settings/music_together") },
        ),
    )
