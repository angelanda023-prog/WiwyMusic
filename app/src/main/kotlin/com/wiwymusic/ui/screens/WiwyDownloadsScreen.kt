/*
 * WiwyMusic — Descargas (landing tipo mockup, datos reales)
 * Basado en OpenTune (GPL-3.0).
 */

package com.wiwymusic.ui.screens

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.wiwymusic.LocalDownloadUtil
import com.wiwymusic.R
import com.wiwymusic.constants.AudioQuality
import com.wiwymusic.constants.AudioQualityKey
import com.wiwymusic.utils.rememberEnumPreference

private val WiwyOrange = Color(0xFFF5791F)
private val WiwyBg = Color(0xFF0A0A0C)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

@Composable
fun WiwyDownloadsScreen(navController: NavController) {
    val downloadUtil = LocalDownloadUtil.current
    val downloads by downloadUtil.downloads.collectAsState()
    val songCount = downloads.values.count { it.state == Download.STATE_COMPLETED }
    val albumsViewModel: com.wiwymusic.viewmodels.DownloadedAlbumsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val downloadedAlbums by albumsViewModel.downloadedAlbums.collectAsState()

    val (audioQuality) = rememberEnumPreference(AudioQualityKey, AudioQuality.AUTO)
    val qualityLabel = when (audioQuality) {
        AudioQuality.AUTO -> "Automática"
        AudioQuality.HIGH -> "Alta"
        AudioQuality.HIGHEST -> "Máxima"
        AudioQuality.LOW -> "Baja"
    }

    val (usedGb, totalGb, pct) = remember {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val avail = stat.availableBytes
        val used = (total - avail).coerceAtLeast(0)
        Triple(used / 1_000_000_000.0, total / 1_000_000_000.0, if (total > 0) (used.toFloat() / total) else 0f)
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(Modifier.fillMaxSize().background(WiwyBg)) {
        // Cabecera FIJA
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, bottom = 12.dp)
                .padding(top = statusTop + 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Descargas", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            com.wiwymusic.ui.component.WiwyProfileAvatar()
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
            // Espacio utilizado
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(WiwyCard).padding(18.dp),
                ) {
                    Text("Espacio utilizado", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFF333338))) {
                        Box(Modifier.fillMaxWidth(pct).fillMaxHeight().clip(CircleShape).background(WiwyOrange))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("%.0f GB de %.0f GB".format(usedGb, totalGb), color = WiwyMuted, fontSize = 13.sp)
                        Text("%.0f%% usado".format(pct * 100), color = WiwyMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Categorías
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(WiwyCard),
                ) {
                    DownloadCat(R.drawable.music_note, WiwyOrange, "Canciones", "$songCount canciones") {
                        navController.navigate("auto_playlist/downloaded")
                    }
                    Divider()
                    DownloadCat(R.drawable.album, WiwyOrange, "Álbumes", "${downloadedAlbums.size} álbumes") {
                        navController.navigate("downloaded_albums")
                    }
                    Divider()
                    DownloadCat(R.drawable.queue_music, Color(0xFF3FBF5F), "Playlists", "0 playlists") {
                        navController.navigate("library/playlists")
                    }
                    Divider()
                    DownloadCat(R.drawable.mic, WiwyOrange, "Podcasts", "0 podcasts") {}
                }
            }

            // Calidad de descarga
            item {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(WiwyCard)
                        .clickable { navController.navigate("settings/player") }
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.history), null, tint = WiwyMuted, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Calidad de descarga", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(qualityLabel, color = WiwyOrange, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.navigate_next), null, tint = WiwyOrange, modifier = Modifier.size(20.dp))
                }
            }

            // Limpiar descargas
            item {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(WiwyCard)
                        .clickable { navController.navigate("settings/storage") }
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.delete), null, tint = Color(0xFFE0483B), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Limpiar descargas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.navigate_next), null, tint = WiwyMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.padding(start = 60.dp, end = 16.dp).fillMaxWidth().height(1.dp).background(Color(0xFF262630)))
}

@Composable
private fun DownloadCat(icon: Int, tint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = WiwyMuted, fontSize = 13.sp)
        }
    }
}
