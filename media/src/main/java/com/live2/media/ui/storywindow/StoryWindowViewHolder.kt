package com.live2.media.ui.storywindow

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.live2.media.L1PlayerHelper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.live2.media.databinding.LayoutWindowStoryItemBinding
import com.live2.media.databinding.WatchBodyBinding
import com.live2.media.client.model.PostModel
import com.live2.media.client.SiteSectionViewClickListener
import com.live2.media.utils.Utils
import com.live2.media.utils.Utils.Companion.gone
import com.live2.media.utils.Utils.Companion.show
import kotlin.math.abs

class StoryWindowViewHolder(private val view: LayoutWindowStoryItemBinding) :
    RecyclerView.ViewHolder(view.root) {

    private lateinit var playerHelper: L1PlayerHelper
    fun bind(
        watchableModel: PostModel.Video,
        playerHelper: L1PlayerHelper,
        siteSectionViewClickListener: SiteSectionViewClickListener,
        position: Int
    ) {
        this.playerHelper = playerHelper
        with(watchableModel) {
            loadPlaceHolder(view.playerLayout, this)
            view.root.setOnClickListener { siteSectionViewClickListener.onItemClicked(position) }
        }
    }

    fun loadMedia(post: PostModel.Video) {
        playerHelper.currentlyPlayingVideo = true
        loadVideo(view.playerLayout, post)
    }

    private fun loadVideo(watchBody: WatchBodyBinding, videoModel: PostModel.Video) {
        playerHelper.listener?.let {
            playerHelper.player?.removeListener(it)
        }
        playerHelper.player?.release()
        playerHelper.player = playerHelper.buildPlayer()
        watchBody.playerView.player = playerHelper.player
        watchBody.playerView.player?.volume = 0f
        playerHelper.listener = object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                val originalWidthHeightRatio =
                    videoSize.width.toFloat() / videoSize.height.toFloat()
                val diff = abs(Utils.screenWidthHeightRatio - originalWidthHeightRatio)
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
                        watchBody.ivLoading.gone()
                        watchBody.placeholder.gone()
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
                    }

                    Player.STATE_IDLE -> {}

                    Player.STATE_ENDED -> {
                        Log.d("VIDEO_SDK_DEBUG", "STATE_ENDED")
                    }
                }
            }
        }



        playerHelper.player?.let {
            playerHelper.prepare(it, videoModel)
        }
        playerHelper.listener?.let {
            playerHelper.player?.addListener(it)
        }
        playerHelper.player?.playWhenReady = true
    }

    fun loadImage(post: PostModel.Video) {
        view.playerLayout.placeholder.show()
    }

    private fun loadPlaceHolder(watchBody: WatchBodyBinding, post: PostModel.Video) {
        with(watchBody.placeholder) {
            var thumbnailUrl = post.thumbnailUrl.substring(0)
            thumbnailUrl = thumbnailUrl.substring(0, thumbnailUrl.indexOf(".png") + 4)
            Glide.with(context)
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
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource.let {
                            layoutParams.width = watchBody.root.width
                            layoutParams.height = watchBody.root.height
                            val originalWidthHeightRatio =
                                it.intrinsicWidth.toFloat() / it.intrinsicHeight.toFloat()
                            val diff = abs(Utils.screenWidthHeightRatio - originalWidthHeightRatio)
                            scaleType = if (diff < 0.2) {
                                ImageView.ScaleType.CENTER_CROP
                            } else {
                                ImageView.ScaleType.FIT_CENTER
                            }
                        }
                        watchBody.ivLoading.visibility = View.GONE
                        return false
                    }
                })
                .into(this)
        }
    }

}