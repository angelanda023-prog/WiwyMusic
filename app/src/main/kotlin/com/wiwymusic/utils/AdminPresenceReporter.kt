package com.wiwymusic.utils

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.wiwymusic.playback.MusicService
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import timber.log.Timber

/**
 * Reporta actividad administrativa sin intervenir en la lógica del reproductor.
 * Lee la MediaSession pública de la propia app como cualquier controlador Android.
 */
object AdminPresenceReporter : DefaultLifecycleObserver, Player.Listener {
    private const val HEARTBEAT_MS = 15_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sendMutex = Mutex()
    private var appContext: Context? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var heartbeatJob: Job? = null

    @Volatile private var appActive = false
    @Volatile private var isPlaying = false
    @Volatile private var songId: String? = null
    @Volatile private var songTitle: String? = null
    @Volatile private var songArtist: String? = null
    @Volatile private var songThumbnailUrl: String? = null

    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        scope.launch {
            SupabaseAuth.session.collectLatest { session ->
                if (session != null) sendLatest()
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        appActive = true
        ensureController()
        updateHeartbeat()
        sendLatest()
    }

    override fun onStop(owner: LifecycleOwner) {
        appActive = false
        sendLatest()
        updateHeartbeat()
    }

    private fun ensureController() {
        if (controller != null || controllerFuture != null) return
        val context = appContext ?: return
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        controllerFuture = null
                        mediaController.addListener(this)
                        syncFromController(mediaController)
                    }
                    .onFailure { error ->
                        controllerFuture = null
                        Timber.w(error, "AdminPresenceReporter: MediaController no disponible")
                    }
            },
            MoreExecutors.directExecutor(),
        )
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        controller?.let(::syncFromController)
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        controller?.let(::syncFromController)
    }

    override fun onIsPlayingChanged(playing: Boolean) {
        controller?.let(::syncFromController)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        controller?.let(::syncFromController)
    }

    private fun syncFromController(mediaController: MediaController) {
        val item = mediaController.currentMediaItem
        val metadata = item?.mediaMetadata
        isPlaying = mediaController.isPlaying
        songId = item?.mediaId?.takeIf { it.isNotBlank() }
        songTitle = metadata?.title?.toString()?.takeIf { it.isNotBlank() }
        songArtist = (metadata?.artist ?: metadata?.subtitle)?.toString()?.takeIf { it.isNotBlank() }
        songThumbnailUrl = metadata?.artworkUri?.toString()?.takeIf { it.isNotBlank() }
        updateHeartbeat()
        sendLatest()
    }

    private fun updateHeartbeat() {
        if (appActive || isPlaying) {
            if (heartbeatJob?.isActive == true) return
            heartbeatJob = scope.launch {
                while (isActive && (appActive || isPlaying)) {
                    sendLatestNow()
                    delay(HEARTBEAT_MS)
                }
            }
        } else {
            heartbeatJob?.cancel()
            heartbeatJob = null
        }
    }

    private fun sendLatest() {
        scope.launch { sendLatestNow() }
    }

    private suspend fun sendLatestNow() {
        if (SupabaseAuth.session.value == null) return
        sendMutex.withLock {
            val body = JSONObject()
                .put("user_id", SupabaseAuth.session.value?.userId ?: return@withLock)
                .put("app_active", appActive)
                .put("is_playing", isPlaying)
                .put("song_id", songId ?: JSONObject.NULL)
                .put("song_title", songTitle ?: JSONObject.NULL)
                .put("song_artist", songArtist ?: JSONObject.NULL)
                .put("song_thumbnail_url", songThumbnailUrl ?: JSONObject.NULL)
                .put("last_seen_at", Instant.now().toString())
                .put("updated_at", Instant.now().toString())

            SupabaseAuth.upsertAppPresence(body).onFailure { error ->
                Timber.d(error, "AdminPresenceReporter: no se pudo reportar presencia")
            }
        }
    }
}
