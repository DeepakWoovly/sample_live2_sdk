package com.live2.media.ui.floatingwindow

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.live2.media.VideoSDK
import com.live2.media.L1PlayerHelper
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.live2.media.R
import com.live2.media.databinding.LayoutPipItemBinding
import com.live2.media.databinding.WatchBodyBinding
import com.live2.media.internal.model.PostModel
import com.live2.media.utils.Utils
import kotlin.math.abs

class PiPView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var playerHelper: L1PlayerHelper
    private var videoPreviewHandler = Handler(Looper.getMainLooper())
    private var videosList: List<PostModel.Video>? = null
    private var binding: LayoutPipItemBinding

    init {
         inflate(context, R.layout.layout_pip_item, this)
         binding = LayoutPipItemBinding.inflate(LayoutInflater.from(context), this, false)
    }

    private fun observeLifecycle() {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                initializeComponents()
            }
        })
    }

    fun setLifeCycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
        observeLifecycle()
    }

    private fun initializeComponents() {
        if (::lifecycleOwner.isInitialized) {
            playerHelper = L1PlayerHelper(lifecycleOwner, context)
            playerHelper.currentlyPlayingVideo = true
        }
    }

    fun onCarouselItemClicked(position: Int) {
        val intent = Intent(context, VideoSDK::class.java)
        val bundle = Bundle()
        val arrayList = arrayListOf<PostModel.Video>()
        videosList?.let { arrayList.addAll(it) }
        bundle.putParcelableArrayList("List", arrayList )
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun setVideosList(list: List<PostModel.Video>){
        videosList = list
    }

    fun loadPlaceHolder(watchBody: WatchBodyBinding, post: PostModel.Video) {
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

    fun loadVideo(watchBody: WatchBodyBinding, videoModel: PostModel.Video) {
        playerHelper.listener?.let {
            playerHelper.player?.removeListener(it)
        }
        playerHelper.player?.release()
        playerHelper.player = playerHelper.buildPlayer()
        watchBody.playerView.player = playerHelper.player
        watchBody.playerView.player?.volume = 0f
        playerHelper.listener = object : Player.Listener{

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
                    }

                    Player.STATE_IDLE -> {}

                    Player.STATE_ENDED -> {}
                }
            }
        }



        playerHelper.player?.let {
            playerHelper.prepare(it,videoModel)
        }
        playerHelper.listener?.let {
            playerHelper.player?.addListener(it)
        }
        playerHelper.player?.playWhenReady = true
    }

}