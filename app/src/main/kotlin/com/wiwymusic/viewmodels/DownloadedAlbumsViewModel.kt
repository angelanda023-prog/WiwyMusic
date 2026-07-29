/*
 * WiwyMusic — agrupa canciones descargadas por álbum para la pantalla de Descargas.
 */

package com.wiwymusic.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import com.wiwymusic.db.MusicDatabase
import com.wiwymusic.db.entities.AlbumEntity
import com.wiwymusic.playback.DownloadUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DownloadedAlbum(
    val album: AlbumEntity,
    val downloadedSongCount: Int,
)

@HiltViewModel
class DownloadedAlbumsViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadedAlbums =
        combine(database.allSongs(), downloadUtil.downloads) { songs, downloads ->
            songs
                .filter { downloads[it.id]?.state == Download.STATE_COMPLETED }
                .mapNotNull { it.album }
                .groupBy { it.id }
                .map { (_, albums) -> DownloadedAlbum(albums.first(), albums.size) }
                .sortedBy { it.album.title }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
