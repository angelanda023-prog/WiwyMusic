/*
 * WiwyMusic — álbumes con canciones descargadas (agrupado, no lista plana de canciones).
 */

package com.wiwymusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.wiwymusic.viewmodels.DownloadedAlbumsViewModel

private val WiwyBg = Color(0xFF0A0A0C)
private val WiwyCard = Color(0xFF17171C)
private val WiwyMuted = Color(0xFF9A9AA2)

@Composable
fun DownloadedAlbumsScreen(
    navController: NavController,
    viewModel: DownloadedAlbumsViewModel = hiltViewModel(),
) {
    val albums by viewModel.downloadedAlbums.collectAsState()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(Modifier.fillMaxSize().background(WiwyBg)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, bottom = 12.dp)
                .padding(top = statusTop + 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Álbumes descargados", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        }

        if (albums.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay álbumes descargados", color = WiwyMuted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                items(albums, key = { it.album.id }) { downloadedAlbum ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(WiwyCard)
                            .clickable { navController.navigate("album/${downloadedAlbum.album.id}") }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = downloadedAlbum.album.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                downloadedAlbum.album.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${downloadedAlbum.downloadedSongCount} canciones descargadas",
                                color = WiwyMuted,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
