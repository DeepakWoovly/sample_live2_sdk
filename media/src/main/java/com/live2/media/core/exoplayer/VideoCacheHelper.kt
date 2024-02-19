package com.example.videosdk.core

import android.net.Uri
import com.example.videosdk.WatchableModel
import com.example.videosdk.network.model.PostModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.Downloader
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.ConcurrentModificationException

object VideoCacheHelper{

    private val downloaders = mutableListOf<Downloader>()
    private val disposables = CompositeDisposable()

    private lateinit var cacheDataSourceFactoryFactory: CacheDataSource.Factory

    fun initialize(cacheDataSourceFactoryFactory: CacheDataSource.Factory) {
        VideoCacheHelper.cacheDataSourceFactoryFactory = cacheDataSourceFactoryFactory
    }

    fun cacheVideo(trailId: String) {
//        val apiService =
//            ApiClient.getClient(UrlConstants.APP_SERVER_URL).create(ApiInterface::class.java)
//        val params = mapOf<String, Any>(
//            "trailId" to trailId,
//            "userId" to SPUtils.getMyUserId(),
//            "pageNo" to 1,
//            "clickSrc" to Constants.NOTIFICATION_PRE_CACHE
//        )
//        val call = apiService.fetchTrailDetails(params)
//        call.enqueue(object : Callback<WatchResponse> {
//            override fun onResponse(call: Call<WatchResponse>, response: Response<WatchResponse>) {
//                if (response.code() == HttpURLConnection.HTTP_OK &&
//                    response.body()?.status.equals(KeyConstants.KEY_SUCCESS, ignoreCase = true)
//                ) {
//                    cacheHlsAsync(response.body()?.trails?.getOrNull(0))
//                }
//            }
//
//            override fun onFailure(call: Call<WatchResponse>, t: Throwable) {
//                // Error fetching trails for caching. Do not retry.
//                // Do nothing. Do need to retry.
//            }
//        })
    }

    fun cacheHlsAsync(trailModel: com.example.videosdk.network.model.PostModel.Video?) {
        disposables += cacheHLS(trailModel)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = {
                it.printStackTraceIfDebug()
            })
    }

    private fun cacheHLS(trailModel: com.example.videosdk.network.model.PostModel.Video?): Completable {
        val videoUrl = trailModel?.videoUrl
        if (videoUrl.isNullOrBlank()) {
            return Completable.complete()
        }
        return downloadHls(createMediaItemFromUrl(videoUrl))
    }

    private fun cancelOngoingDownloadsCompletable(): Completable {
        return Flowable.fromIterable(downloaders)
            .map { it.cancel() }
            .toList()
            .map { downloaders.clear() }
            .ignoreElement()
            .subscribeOn(Schedulers.io())
    }

    fun cancelOngoingDownloadsAsync() {
        disposables += cancelOngoingDownloadsCompletable()
            .subscribeBy(onError = {
                it.printStackTraceIfDebug()
            })
    }

    private fun downloadHls(mediaItem: MediaItem): Completable {
        return Completable.fromObservable<Unit> {
            val hlsDownloader = getHlsDownloader(mediaItem)
            downloaders.add(hlsDownloader)
            try {
                hlsDownloader.download { _, _, percentDownloaded ->
                    if (percentDownloaded == 100f) {
                        downloaders.remove(hlsDownloader)
                        it.onComplete()
                    }
                }
            } catch (e: Exception) {
                downloaders.remove(hlsDownloader)
                it.onError(e)
            }
        }
    }

    private fun getHlsDownloader(mediaItem: MediaItem): Downloader {
        return HlsDownloader(mediaItem, cacheDataSourceFactoryFactory)
    }

    fun stopCaching() {
        try {
            downloaders.forEach {
                it?.cancel()
            }
            downloaders.clear()
        }
        catch (e: ConcurrentModificationException){

        }
        disposables.clear()
    }

    private fun Throwable.printStackTraceIfDebug() {

    }

    fun cacheVideos(videosToCache: List<WatchableModel>) {
        val downloadCompletable = Flowable.fromIterable(videosToCache)
            .flatMapCompletable { model ->
                when (model) {
                    is PostModel.Video -> {
                        cacheHLS(model)
                    }
                    else -> {
                        Completable.complete()
                    }
                }
            }

        disposables += cancelOngoingDownloadsCompletable()
            .andThen(downloadCompletable)
            .subscribeBy(onError = {
                it.printStackTraceIfDebug()
            })
    }

    private fun createMediaItemFromUrl(videoUrl: String): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .build()
    }
}

