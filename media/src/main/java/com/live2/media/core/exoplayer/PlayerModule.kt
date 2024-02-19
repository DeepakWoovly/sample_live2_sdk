package com.live2.media.core.exoplayer

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.mkv.MatroskaExtractor
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object PlayerModule {

    private const val loadControlBufferDuration = 12000 // 12 seconds
    private const val bufferPlaybackDuration = 1500 // 1.5 seconds (as currently ts is 2 seconds)
    private const val initialBitrate = 400000L // 40 kbps
    private const val maxVideoCacheSize = 100 * 1024 * 1024L // 100 MB
    private const val minVideoCacheSize = 10 * 1024 * 1024L // 10 MB


    fun defaultBandwidthMeter(context: Context): ResettableBandwidthMeter {
        return ResettableBandwidthMeter.Builder(context)
            .setInitialBitrateEstimate(initialBitrate)
            .build()
    }

    fun bandwidthMeter(defaultBandwidthMeter: ResettableBandwidthMeter): BandwidthMeter {
        return defaultBandwidthMeter
    }

    fun cacheDataSourceFactory(cache: Cache, factory: DataSource.Factory): CacheDataSource.Factory {
        val cacheDataSourceFactory = CacheDataSource.Factory()
        cacheDataSourceFactory.setCache(cache)
        cacheDataSourceFactory.setUpstreamDataSourceFactory(factory)
        return cacheDataSourceFactory
    }

    //need application context
    fun cache(context: Context): Cache {
        val downloadFolder = File(context.cacheDir, "videocache")
        val dbProvider = ExoDatabaseProvider(context)
        val maxCacheSize = StorageUtils.calculateDiskCacheSize(
            downloadFolder,
            minVideoCacheSize,
            maxVideoCacheSize
        )
        return SimpleCache(downloadFolder, LeastRecentlyUsedCacheEvictor(maxCacheSize), dbProvider)
    }

    fun hlsMediaSourceFactory(
        cacheDataSourceFactory: CacheDataSource.Factory
    ): HlsMediaSource.Factory {
        val mediaDataSourceFactory = DefaultHlsDataSourceFactory(cacheDataSourceFactory)
        return HlsMediaSource.Factory(mediaDataSourceFactory)
            .setAllowChunklessPreparation(true)
    }

    fun dashMediaSourceFactory(cacheDataSourceFactory: CacheDataSource.Factory): DashMediaSource.Factory {
        return DashMediaSource.Factory(cacheDataSourceFactory)
    }

    fun loadControl(): LoadControl {
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

    fun audioAttr(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    fun progressiveMediaSourceFactory(
        factory: DataSource.Factory,
        extractorsFactory: ExtractorsFactory
    ): ProgressiveMediaSource.Factory {
        return ProgressiveMediaSource.Factory(factory, extractorsFactory)
    }

    fun renderersFactory(context: Context): RenderersFactory {
        return DefaultRenderersFactory(context)
    }

    fun extractorsFactory(): ExtractorsFactory {
        return ExtractorsFactory {
            return@ExtractorsFactory arrayOf(Mp3Extractor(), Mp4Extractor(), MatroskaExtractor())
        }
    }

}
