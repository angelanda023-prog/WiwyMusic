/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.wiwymusic.viewmodels
import kotlinx.coroutines.flow.combine

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import com.wiwymusic.constants.AutoPlaylistSongSortDescendingKey
import com.wiwymusic.constants.AutoPlaylistSongSortType
import com.wiwymusic.constants.AutoPlaylistSongSortTypeKey
import com.wiwymusic.constants.HideExplicitKey
import com.wiwymusic.constants.HideVideoKey
import com.wiwymusic.constants.SongSortType
import com.wiwymusic.db.MusicDatabase
import com.wiwymusic.extensions.filterExplicit
import com.wiwymusic.extensions.reversed
import com.wiwymusic.extensions.toEnum
import com.wiwymusic.playback.DownloadUtil
import com.wiwymusic.utils.SyncUtils
import com.wiwymusic.utils.dataStore
import com.wiwymusic.utils.get
import com.wiwymusic.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AutoPlaylistViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
    savedStateHandle: SavedStateHandle,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    // WiwyMusic: la pestaña Favoritos usa la ruta "favorites" sin argumento -> por defecto "liked".
    val playlist = savedStateHandle.get<String>("playlist") ?: "liked"

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private fun AutoPlaylistSongSortType.toSongSortType(): SongSortType =
        when (this) {
            AutoPlaylistSongSortType.CREATE_DATE -> SongSortType.CREATE_DATE
            AutoPlaylistSongSortType.NAME -> SongSortType.NAME
            AutoPlaylistSongSortType.ARTIST -> SongSortType.ARTIST
            AutoPlaylistSongSortType.PLAY_TIME -> SongSortType.PLAY_TIME
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val likedSongs =
        context.dataStore.data
            .map {
                Triple(
                    it[AutoPlaylistSongSortTypeKey].toEnum(AutoPlaylistSongSortType.CREATE_DATE) to (it[AutoPlaylistSongSortDescendingKey]
                        ?: true),
                    it[HideExplicitKey] ?: false,
                    it[HideVideoKey] ?: false,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit, hideVideo) ->
                val (sortType, descending) = sortDesc
                val songSortType = sortType.toSongSortType()
                when (playlist) {
                    "liked" -> database.likedSongs(songSortType, descending, hideVideo).map { it.filterExplicit(hideExplicit) }
                    "downloaded" -> combine(
                        database.allSongs().flowOn(Dispatchers.IO),
                        downloadUtil.downloads
                    ) { songs, downloads ->
                        songs.filter {
                            downloads[it.id]?.state == Download.STATE_COMPLETED
                        }.let { filteredSongs ->
                            when (songSortType) {
                                SongSortType.CREATE_DATE -> filteredSongs.sortedBy {
                                    downloads[it.id]?.updateTimeMs ?: 0L
                                }
                                SongSortType.NAME -> filteredSongs.sortedBy { it.song.title }
                                SongSortType.ARTIST -> filteredSongs.sortedBy { song ->
                                    song.artists.joinToString(separator = "") { it.name }
                                }
                                SongSortType.PLAY_TIME -> filteredSongs.sortedBy { it.song.totalPlayTime }
                            }.reversed(descending).filterExplicit(hideExplicit)
                        }
                    }
                    else -> MutableStateFlow(emptyList())
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                when (playlist) {
                    "liked" -> syncUtils.syncLikedSongs()
                    else -> Unit
                }
            } catch (e: Exception) {
                reportException(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun syncLikedSongs() {
        refresh()
    }
}
