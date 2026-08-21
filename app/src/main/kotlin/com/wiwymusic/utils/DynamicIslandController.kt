package com.wiwymusic.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.wiwymusic.R
import com.wiwymusic.constants.DynamicIslandEnabledKey
import com.wiwymusic.playback.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.min
import kotlin.math.abs
import kotlin.math.roundToInt

/** Compact opt-in playback overlay shown only while WiwyMusic is in background. */
object DynamicIslandController : DefaultLifecycleObserver, Player.Listener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var context: Context? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var windowManager: WindowManager? = null
    private var islandView: View? = null
    private var titleView: TextView? = null
    private var artistView: TextView? = null
    private var artworkView: ImageView? = null
    private var artworkUri: String? = null
    private var playPauseButton: ImageButton? = null
    private var appInForeground = true

    fun initialize(appContext: Context) {
        if (context != null) return
        context = appContext.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        scope.launch {
            UserPrefs.isPremium.collectLatest { premium ->
                if (premium != true) {
                    hide()
                    releaseController()
                } else {
                    val enabled = withContext(Dispatchers.IO) {
                        appContext.dataStore.data.first()[DynamicIslandEnabledKey] ?: false
                    }
                    if (enabled && appInForeground) connectController()
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        appInForeground = true
        hide()
        val appContext = context ?: return
        scope.launch {
            val enabled = withContext(Dispatchers.IO) {
                appContext.dataStore.data.first()[DynamicIslandEnabledKey] ?: false
            }
            if (enabled && UserPrefs.isPremium.value == true) {
                connectController()
            } else {
                releaseController()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        appInForeground = false
        val appContext = context ?: return
        scope.launch {
            val enabled = withContext(Dispatchers.IO) {
                appContext.dataStore.data.first()[DynamicIslandEnabledKey] ?: false
            }
            if (
                enabled &&
                UserPrefs.isPremium.value == true &&
                Settings.canDrawOverlays(appContext) &&
                !appInForeground
            ) {
                connectController()
            }
        }
    }

    fun onPreferenceChanged(enabled: Boolean) {
        val appContext = context ?: return
        if (
            enabled &&
            UserPrefs.isPremium.value == true &&
            Settings.canDrawOverlays(appContext)
        ) {
            connectController()
        } else {
            hide()
            releaseController()
        }
    }

    private fun connectController() {
        if (controller != null || controllerFuture != null) {
            refresh()
            return
        }
        val appContext = context ?: return
        val token = SessionToken(appContext, ComponentName(appContext, MusicService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controllerFuture = null
                        controller = mediaController
                        mediaController.addListener(this)
                        refresh()
                    }
                    .onFailure { error ->
                        controllerFuture = null
                        Timber.w(error, "DynamicIsland: media controller unavailable")
                    }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        postRefresh()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        postRefresh()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        postRefresh()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        postRefresh()
    }

    private fun postRefresh() = mainHandler.post(::refresh)

    private fun refresh() {
        val mediaController = controller ?: return
        val hasPlayableItem =
            mediaController.currentMediaItem != null &&
                mediaController.playbackState != Player.STATE_IDLE &&
                mediaController.playbackState != Player.STATE_ENDED
        if (appInForeground || !hasPlayableItem) {
            hide()
            return
        }
        ensureView()
        val metadata = mediaController.currentMediaItem?.mediaMetadata
        titleView?.text = metadata?.title?.toString()?.takeIf { it.isNotBlank() }
            ?: context?.getString(R.string.app_name)
        artistView?.text = (metadata?.artist ?: metadata?.subtitle)?.toString().orEmpty()
        artistView?.visibility = if (artistView?.text.isNullOrBlank()) View.GONE else View.VISIBLE
        playPauseButton?.setImageResource(if (mediaController.isPlaying) R.drawable.pause else R.drawable.play)
        updateArtwork(metadata?.artworkUri?.toString())
    }

    private fun ensureView() {
        if (islandView != null) return
        val appContext = context ?: return
        val density = appContext.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).roundToInt()

        lateinit var leftSwipeHint: ImageView
        lateinit var rightSwipeHint: ImageView
        val root = LinearLayout(appContext).apply {
            var touchDownX = 0f
            var touchDownY = 0f
            val swipeThreshold = maxOf(dp(42), ViewConfiguration.get(appContext).scaledTouchSlop * 3)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(4), dp(5), dp(4))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(24).toFloat()
                setColor(Color.BLACK)
            }
            elevation = dp(14).toFloat()
            setOnClickListener {
                appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)?.let { intent ->
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    appContext.startActivity(intent)
                }
            }
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        animate().cancel()
                        touchDownX = event.rawX
                        touchDownY = event.rawY
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - touchDownX
                        val deltaY = event.rawY - touchDownY
                        if (abs(deltaX) > abs(deltaY)) {
                            val canMove = if (deltaX > 0f) {
                                controller?.nextMediaItemIndex != -1
                            } else {
                                controller?.previousMediaItemIndex != -1
                            }
                            if (canMove) {
                                translationX = deltaX.coerceIn(-dp(96).toFloat(), dp(96).toFloat())
                                val progress =
                                    ((abs(translationX) - dp(12)) / dp(48).toFloat()).coerceIn(0f, 1f)
                                leftSwipeHint.alpha = if (translationX > 0f) progress else 0f
                                rightSwipeHint.alpha = if (translationX < 0f) progress else 0f
                            }
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val deltaX = translationX
                        val deltaY = event.rawY - touchDownY
                        if (abs(deltaX) >= swipeThreshold && abs(deltaX) > abs(deltaY)) {
                            if (deltaX > 0f) {
                                controller?.seekToNextMediaItem()
                            } else {
                                controller?.seekToPreviousMediaItem()
                            }
                        } else {
                            view.performClick()
                        }
                        leftSwipeHint.animate().alpha(0f).setDuration(160L).start()
                        rightSwipeHint.animate().alpha(0f).setDuration(160L).start()
                        animate()
                            .translationX(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setDuration(260L)
                            .start()
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        leftSwipeHint.alpha = 0f
                        rightSwipeHint.alpha = 0f
                        animate()
                            .translationX(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setDuration(260L)
                            .start()
                        true
                    }
                    else -> true
                }
            }
        }

        artworkView = ImageView(appContext).apply {
            setImageResource(R.mipmap.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(Color.DKGRAY)
            }
            clipToOutline = true
        }
        root.addView(artworkView, LinearLayout.LayoutParams(dp(34), dp(34)))

        val textColumn = LinearLayout(appContext).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(7), 0, dp(3), 0)
        }
        titleView = TextView(appContext).apply {
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        artistView = TextView(appContext).apply {
            setTextColor(Color.rgb(190, 190, 190))
            textSize = 10f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        textColumn.addView(titleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textColumn.addView(artistView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(textColumn, LinearLayout.LayoutParams(0, dp(36), 1f))

        playPauseButton = playbackButton(appContext, R.drawable.play) {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
        }
        playPauseButton?.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(245, 121, 31))
        }
        playPauseButton?.setColorFilter(Color.BLACK)
        root.addView(playPauseButton, LinearLayout.LayoutParams(dp(34), dp(34)))

        val host = FrameLayout(appContext).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(24).toFloat()
                setColor(Color.BLACK)
            }
            clipToOutline = true
        }
        leftSwipeHint = ImageView(appContext).apply {
            setImageResource(R.drawable.skip_next)
            setColorFilter(Color.rgb(245, 121, 31))
            alpha = 0f
        }
        rightSwipeHint = ImageView(appContext).apply {
            setImageResource(R.drawable.skip_previous)
            setColorFilter(Color.rgb(245, 121, 31))
            alpha = 0f
        }
        host.addView(
            leftSwipeHint,
            FrameLayout.LayoutParams(dp(24), dp(24), Gravity.START or Gravity.CENTER_VERTICAL).apply {
                marginStart = dp(10)
            },
        )
        host.addView(
            rightSwipeHint,
            FrameLayout.LayoutParams(dp(24), dp(24), Gravity.END or Gravity.CENTER_VERTICAL).apply {
                marginEnd = dp(10)
            },
        )
        host.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        val metrics = appContext.resources.displayMetrics
        val params = WindowManager.LayoutParams(
            min(dp(320), metrics.widthPixels - dp(24)),
            dp(42),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            // Sin FLAG_LAYOUT_IN_SCREEN, Android respeta la barra de estado.
            // El margen deja la cápsula entre la barra y el primer widget.
            y = dp(4)
        }

        runCatching {
            val manager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            manager.addView(host, params)
            windowManager = manager
            islandView = host
            host.alpha = 0f
            host.translationY = -dp(52).toFloat()
            host.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(280L)
                .start()
        }.onFailure { Timber.w(it, "DynamicIsland: overlay could not be shown") }
    }

    private fun playbackButton(context: Context, icon: Int, action: () -> Unit) =
        ImageButton(context).apply {
            setImageResource(icon)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(10, 10, 10, 10)
            setOnClickListener { action() }
        }

    private fun updateArtwork(uri: String?) {
        if (uri == artworkUri) return
        artworkUri = uri
        val appContext = context ?: return
        if (uri.isNullOrBlank()) {
            artworkView?.setImageResource(R.mipmap.ic_launcher)
            return
        }
        scope.launch(Dispatchers.IO) {
            val bitmap = runCatching {
                val request = ImageRequest.Builder(appContext)
                    .data(uri)
                    .size(128, 128)
                    .allowHardware(false)
                    .build()
                (appContext.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (artworkUri == uri && bitmap != null) artworkView?.setImageBitmap(bitmap)
            }
        }
    }

    private fun hide() {
        val view = islandView ?: return
        runCatching { windowManager?.removeView(view) }
        islandView = null
        windowManager = null
        titleView = null
        artistView = null
        artworkView = null
        artworkUri = null
        playPauseButton = null
    }

    private fun releaseController() {
        controller?.removeListener(this)
        controller?.release()
        controller = null
        controllerFuture?.cancel(true)
        controllerFuture = null
    }
}
