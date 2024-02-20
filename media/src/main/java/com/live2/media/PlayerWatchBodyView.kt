package com.live2.media

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.live2.media.core.exoplayer.PlayerHelper
import com.live2.media.core.exoplayer.VideoCacheHelper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.live2.media.databinding.ItemVideoBinding
import com.live2.media.databinding.WatchBodyBinding
import com.live2.media.client.model.PostModel
import com.live2.media.utils.Utils.Companion.gone
import kotlin.math.abs

class PlayerWatchBodyView(
    val playerHelper: PlayerHelper,
    videoItem: PostModel.Video,
    private val videoVM: VideoCommonItemVm,
    private val cacheHelper: VideoCacheHelper,
    private val videoFinishedListener: () -> Unit,
    private val loadingStateListener: () -> Unit,
    watchBodyBinding: ItemVideoBinding
) {

    private val watchBody = watchBodyBinding.playerLayout

    init {
        playerHelper.player?.volume = 1f
        watchBodyBinding.videoLayout.ivPlayPause.setImageResource(R.drawable.live2_ic_play_video)
        watchBodyBinding.videoLayout.ivMute.setBackgroundResource(R.drawable.btn_bg1)
        watchBodyBinding.videoLayout.ivMute.setImageResource(R.drawable.ic_unmute_video)
        watchBody.mainLayout.removeAllViews()
        if (videoItem.products.isEmpty()){
            watchBodyBinding.videoLayout.productLayout.productLayout.gone()
        }
    }

    fun loadPlaceHolder(post: PostModel.Video) {
        val iv = watchBody.placeholder
        var thumbnailUrl = post.thumbnailUrl.substring(0)
        thumbnailUrl = thumbnailUrl.substring(0, thumbnailUrl.indexOf(".png") + 4)
        Glide.with(iv.context)
            .load(thumbnailUrl)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    watchBody.ivLoading.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    resource.let {
                        val originalWidthHeightRatio =
                            it.intrinsicWidth.toFloat() / it.intrinsicHeight.toFloat()
                        val diff = abs(screenWidthHeightRatio - originalWidthHeightRatio)
                        iv.layoutParams.width = watchBody.root.width
                        iv.layoutParams.height = watchBody.root.height
                        if (diff < 0.2) {
                            iv.scaleType = ImageView.ScaleType.CENTER_CROP
                        } else {
                            iv.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                    }
                    watchBody.ivLoading.visibility = View.GONE
                    return false
                }
            })
            .into(iv)
    }

    private fun toggleLayoutVisibility(watchBody: WatchBodyBinding) {
        watchBody.playerView.alpha = 0f
        watchBody.placeholder.visibility = View.VISIBLE
    }

    fun loadMedia(post: PostModel.Video) {
        playerHelper.currentlyPlayingVideo = true
        toggleLayoutVisibility(watchBody)
        loadVideo(post)
    }

    private fun loadVideo(post: PostModel.Video) {
        playerHelper.listener?.let {
            playerHelper.player?.removeListener(it)
        }
        playerHelper.destroyPrevious()
        playerHelper.prevVideoView = watchBody.playerView
        playerHelper.prevImageView = watchBody.placeholder
        //cacheHelper.cancelOngoingDownloadsAsync()
        val videoUrl = post.videoUrl
        playerHelper.player = playerHelper.findPlayerOrCreateNew(videoUrl)
        watchBody.playerView.player = playerHelper.player

        playerHelper.listener = object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                val originalWidthHeightRatio =
                    videoSize.width.toFloat() / videoSize.height.toFloat()
                val diff = abs(screenWidthHeightRatio - originalWidthHeightRatio)
                if (diff < (videoSize.pixelWidthHeightRatio * 0.2)) {
                    watchBody.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    watchBody.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {

                    Player.STATE_BUFFERING -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (playerHelper.player?.playbackState == Player.STATE_BUFFERING) {
                                watchBody.ivLoading.visibility = View.VISIBLE
                            }
                        }, 2000)
                    }

                    Player.STATE_READY -> {
                        watchBody.ivLoading.visibility = View.GONE
//                        watchBody.placeholder.visibility = View.GONE
                        watchBody.playerView.animate().alpha(1f)
                        watchBody.playerView
                            .animate()
                            .alpha(1f)
                            .withEndAction {
                                if (watchBody.placeholder.isVisible) {
                                    watchBody.placeholder.visibility = View.INVISIBLE
                                }
                            }
                            .duration = 50
                        cacheNextItem()
                    }

                    Player.STATE_IDLE -> {
                    }

                    Player.STATE_ENDED -> {
                        videoFinishedListener.invoke()
                    }
                }
            }
        }

        playerHelper.player?.let {
            playerHelper.prepare(it, post)
        }
        playerHelper.listener?.let {
            playerHelper.player?.addListener(it)
        }
        playerHelper.player?.playWhenReady = playerHelper.shouldStartPlaying
        // as the playerHelper player is already instantiated, set pausePlayerIfPlayerPreparing to false
        playerHelper.pausePlayerIfPlayerPreparing = false


    }

    private val screenWidthHeightRatio: Float by lazy {
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        screenWidth.toFloat() / screenHeight.toFloat()
    }

    private fun cacheNextItem() {
        loadingStateListener.invoke()
    }

}