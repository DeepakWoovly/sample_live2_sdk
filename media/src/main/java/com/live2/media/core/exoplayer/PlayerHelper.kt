package com.live2.media.core.exoplayer

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.live2.media.utils.PauseableCountDownTimer
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.live2.media.internal.model.PostModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


const val PLAYER_POOL_SIZE = 5

class PlayerHelper(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val finishCallback: () -> Unit
) : LifecycleObserver {

    var bufferCount: Int = 0
    var prevImageView: ImageView? = null
    private val loadControlBufferDuration = 12000 // 12 seconds
    private val bufferPlaybackDuration = 1500 // 1.5 seconds (as currently ts is 2 seconds)
    private val initialBitrate = 400000L // 40 kbps
    private val lifecycle = lifecycleOwner.lifecycle
    var prevVideoView: PlayerView? = null
    var listener: Player.Listener? = null
    var pausePlayerIfPlayerPreparing = false

    private var currentPosition = 0
    private val playersPool = mutableMapOf<Int, ExoPlayer>()
    private val playersPostMap = mutableMapOf<String, ExoPlayer>()
    var currentlyPlayingVideo = false
    var player: ExoPlayer? = null
    var handler: Handler? = Handler()
    var runnable: Runnable? = null

    private var progressDisposable: Disposable? = null
    private var watchTime: Long = 0
    var trailTimeTimeStamp = System.currentTimeMillis()
    var playerProgress: (Long) -> Unit = { }

    //private val simpleCache: SimpleCache = VideoSDK.simpleCache

    val timer: PauseableCountDownTimer by lazy {
        PauseableCountDownTimer(300.toLong(), 10, finishCallback)
    }

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)


    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setUpstreamDataSourceFactory(httpDataSourceFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    val shouldStartPlaying: Boolean
        get() = lifecycleOwner.lifecycle.currentState
            .isAtLeast(Lifecycle.State.RESUMED) && !pausePlayerIfPlayerPreparing

    private var pauseTime = 0L
    private var pauseStartTime = System.currentTimeMillis()
    var isPaused = false

    init {
        lifecycle.addObserver(this)
    }

    fun findPlayerOrCreateNew(videoSourceUrl: String?): ExoPlayer {
        if (videoSourceUrl != null) {
            return playersPostMap.getOrPut(videoSourceUrl) {
                getNextPlayer()
            }
        }
        return getNextPlayer()
    }

    private fun getNextPlayer(): ExoPlayer {
        currentPosition++
        if (currentPosition == PLAYER_POOL_SIZE) {
            currentPosition = 0
        }
        return playersPool.getOrPut(currentPosition) { buildPlayer() }
    }

    private fun buildPlayer() = ExoPlayer.Builder(context, renderersFactory(context))
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .setLoadControl(loadControl())
        .setAudioAttributes(audioAttr(), false)
        .setBandwidthMeter(defaultBandwidthMeter(context))
        .build()
        .apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_ONE
            setHandleAudioBecomingNoisy(true)
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        // Start the player when the app is in the foreground
        player?.playWhenReady = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        // Pause the player when the app is in the background
        player?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        progressDisposable?.dispose()
        // Release the player when the app is destroyed
        releaseExoplayer()
        lifecycle.removeObserver(this)
        handler = null
        runnable = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (!currentlyPlayingVideo) {
            runnable?.let {
                handler?.postDelayed(it, 3000)
            }
        }
    }

    private fun releaseExoplayer() {
        destroyPrevious()
        listener?.let {
            player?.removeListener(it)
        }
        playersPool.values.forEach {
            it.release()
        }
    }

    fun destroyPrevious() {
        prevVideoView?.alpha = .5f
        prevImageView?.visibility = View.VISIBLE
        player?.stop()
        prevVideoView?.player = null
        handler?.removeCallbacksAndMessages(null)
    }

    fun playVideo() {
        pausePlayerIfPlayerPreparing = false
        if (timer.isTimerRunning) {
            timer.resumeTimer()
        }
        player?.playWhenReady = true
    }

    private fun renderersFactory(context: Context): RenderersFactory {
        return DefaultRenderersFactory(context)
    }

    private fun loadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(
                loadControlBufferDuration,
                loadControlBufferDuration,
                bufferPlaybackDuration,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .build()
    }

    private fun audioAttr(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    private fun defaultBandwidthMeter(context: Context): ResettableBandwidthMeter {
        return ResettableBandwidthMeter.Builder(context)
            .setInitialBitrateEstimate(initialBitrate)
            .build()
    }

    fun prepare(player: ExoPlayer, post: PostModel.Video) {
        val defaultDataSourceFactory = DefaultDataSource.Factory(context)
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
            context,
            defaultDataSourceFactory
        )

        if (post.streamType == "VOD") {
            player.setMediaSource(progressiveMediaFactory(post, dataSourceFactory))
        } else if (post.streamType == "LIVE") {
            player.setMediaSource(hlsSourceFactory(post, dataSourceFactory))
        }

        player.prepare()

        progressDisposable?.dispose()
        progressDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map { player.currentPosition / 1000 }
            .distinct()
            .subscribe {
                playerProgress(it)
            }

    }

    fun resetPauseTime() {
        pauseTime = 0
        pauseStartTime = System.currentTimeMillis()
        isPaused = false
    }


    private fun progressiveMediaFactory(
        videoModel: PostModel.Video,
        dataSourceFactory: DataSource.Factory
    ): ProgressiveMediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoModel.videoUrl))
    }

    private fun hlsSourceFactory(
        videoModel: PostModel.Video,
        dataSourceFactory: DataSource.Factory
    ): HlsMediaSource {
        return HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoModel.videoUrl))
    }


}