package com.live2.media

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.live2.media.core.exoplayer.PlayerModule.audioAttr
import com.live2.media.core.exoplayer.PlayerModule.defaultBandwidthMeter
import com.live2.media.core.exoplayer.PlayerModule.loadControl
import com.live2.media.core.exoplayer.PlayerModule.renderersFactory
import com.live2.media.internal.model.PostModel

class L1PlayerHelper(
    lifecycleOwner: LifecycleOwner,
    private val context: Context
) : LifecycleObserver {

    var listener: Player.Listener? = null
    var player: ExoPlayer? = null
    var handler: Handler? = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    var currentlyPlayingVideo = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun buildPlayer() = ExoPlayer.Builder(context, renderersFactory(context))
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

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)

    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setUpstreamDataSourceFactory(httpDataSourceFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    fun prepare(player: ExoPlayer, post: PostModel.Video) {
        val defaultDataSourceFactory = DefaultDataSource.Factory(context)
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
            context,
            defaultDataSourceFactory
        )
        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(post.videoUrl))
        player.setMediaSource(source)
        player.prepare()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        player?.playWhenReady = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        player?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        runnable?.let {
            handler?.postDelayed(it, 3000)
        }
    }

}