package com.live2.media

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live2.media.ui.campaigns.CampaignsClickCallback
import com.google.android.exoplayer2.ui.PlayerView
import com.live2.media.databinding.ItemVideoBinding
import com.live2.media.databinding.LayoutProductWidgetBinding
import com.live2.media.databinding.LayoutStickerViewBinding
import com.live2.media.internal.model.PostModel
import com.live2.media.internal.model.WatchableModel
import com.live2.media.utils.Utils
import com.live2.media.utils.Utils.Companion.invisible
import com.live2.media.utils.Utils.Companion.setHeightAndWidth
import com.live2.media.utils.Utils.Companion.show
import kotlin.properties.Delegates

class VideoViewHolder(
    private val view: ItemVideoBinding,
    private val adapter: VideoAdapter
) : CommonViewHolder(view.root) {
    private var playerView: PlayerView = view.playerLayout.playerView
    private val videoItemSeekbarVm = VideoItemSeekbarVm(adapter.playerHelper)
    private lateinit var productCardListener: ProductCardListener
    private lateinit var videoItemClickCallbacks: VideoItemClickCallbacks
    private lateinit var videoItemVM: VideoItemVM
    lateinit var playerWatchBodyView: PlayerWatchBodyView
    var isStory by Delegates.notNull<Boolean>()

    init {
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.setKeepContentOnPlayerReset(true)
        view.root.clipToOutline = true
        adapter.playerHelper.attachSeekbar(view.videoLayout.sbPlayer)
        addGestureDetector()
    }

    override fun onBindVewHolder(
        position: Int,
        watchableModel: WatchableModel,
        payload: MutableList<Any>?,
        videoItemClickCallbacks: VideoItemClickCallbacks,
        productCardListener: ProductCardListener,
        campaignsClickCallback: CampaignsClickCallback,
        isStory: Boolean
    ) {
        super.onBindVewHolder(
            position, watchableModel, payload, videoItemClickCallbacks,
            productCardListener, campaignsClickCallback, isStory
        )
        if (watchableModel is PostModel.Video) {
            view.clickCallbacks = videoItemClickCallbacks
            this.productCardListener = productCardListener
            this.videoItemClickCallbacks = videoItemClickCallbacks
            this.isStory = isStory
            bindTrailWatchableModel(
                position,
                watchableModel,
                payload,
                videoItemClickCallbacks,
                campaignsClickCallback
            )
        }
    }

    private fun bindTrailWatchableModel(
        position: Int,
        watchableModel: PostModel.Video,
        payload: MutableList<Any>?,
        videoItemClickCallbacks: VideoItemClickCallbacks,
        campaignsClickCallback: CampaignsClickCallback
    ) {
        val vm = VideoItemVM(
            watchableModel,
            position,
            adapter.playerHelper,
            campaignsClickCallback
        )
        videoItemVM = vm
        adapter.vms[position] = vm
        view.vm = vm
        view.clickCallbacks = videoItemClickCallbacks
        vm.initSeekbar()
        vm.binding = view
        adapter.videoItemHashMap[position] = view
        playerWatchBodyView = PlayerWatchBodyView(
            adapter.playerHelper,
            watchableModel,
            vm,
            adapter.cacheHelper,
            adapter.videoFinishedListener,
            adapter.loadingStateListener,
            view
        )
        updateProductWidget(watchableModel)
        showCampaignIfPresent(watchableModel)
        playerWatchBodyView.loadPlaceHolder(watchableModel)
        view.videoLayout.seekBarVm = videoItemSeekbarVm
        if (position == 0) {
            playerWatchBodyView.loadMedia(watchableModel)
        }
        if (!isStory){
            playerWatchBodyView.loadMedia(watchableModel)
        }
        showStickersIfPresent(watchableModel)
        updateSeekBarVisibility(watchableModel)
    }

    private fun updateProductWidget(products: PostModel.Video) {
        if (products.products.isNotEmpty()) {
            val url = products.products[0].product_image.substring(
                0,
                products.products[0].product_image.length - 7
            )
            val productLayout = view.videoLayout.productLayout
            productLayout.productLayout.visibility = View.VISIBLE
            Glide.with(view.root.context).load(url).into(view.videoLayout.productIcon1)
            view.videoLayout.tvProductCount.text =
                if (products.products.size == 1) {
                    "${products.products.size} Product"
                } else {
                    "${products.products.size} Products"
                }
            updateProductWidgetPager(
                productLayout, products
            )
        }
    }

    private fun updateProductWidgetPager(
        productBinding: LayoutProductWidgetBinding,
        products: PostModel.Video
    ) {
        ProductPlacementView(productBinding, products, productCardListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addGestureDetector() {
        val gd = GestureDetector(
            view.root.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (isStory) {
                        enableStoryModeTaps(e)
                    } else {
                        enablePlayPauseOnTap()
                    }
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    super.onDoubleTap(e)

                    val backwardVal =
                        (view.seekBackwardAnimation.left - view.seekBackwardText.right - 20f)
                    val forwardVal =
                        (view.seekForwardText.left - view.seekForwardAnimation.right - 20f)
                    val yVal = (view.guidelineVerticalMid.bottom / 2).toFloat()

                    view.vm?.let { vm ->
                        e.let { event ->
                            when {
                                event.x < view.guidelineVerticalMid.x -> {
                                    if ((adapter.playerHelper.player?.currentPosition
                                            ?: 0) <= 1000
                                    ) {
                                        return true
                                    }

                                    vm.leftClickCount -= 10
                                    vm.tenSecHandler.removeCallbacksAndMessages(null)

                                    view.apply {
                                        seekBackwardText.text = vm.leftClickCount.toString()
                                        seekBackwardOverlay.visibility = View.VISIBLE
                                        seekBackwardText.visibility = View.VISIBLE

                                        val anim =
                                            TranslateAnimation(0f, -backwardVal, yVal, yVal)
                                        anim.duration = 500
                                        seekBackwardAnimation.startAnimation(anim)
                                    }

                                    adapter.playerHelper.player?.let {
                                        it.seekTo(it.currentPosition - 10000)
                                    }
                                    tenSecondHandlerReset()
                                }

                                else -> {
                                    if (adapter.playerHelper.player?.let {
                                            (it.duration - it.currentPosition) <= 1000
                                        } == true) {
                                        return true
                                    }

                                    vm.rightClickCount += 10
                                    vm.tenSecHandler.removeCallbacksAndMessages(null)

                                    view.apply {
                                        seekForwardText.text = "+" + "${vm.rightClickCount}"
                                        seekForwardOverlay.visibility = View.VISIBLE
                                        seekForwardText.visibility = View.VISIBLE

                                        val anim =
                                            TranslateAnimation(0f, forwardVal, yVal, yVal)
                                        anim.duration = 500
                                        seekForwardAnimation.startAnimation(anim)
                                    }

                                    adapter.playerHelper.player?.let {
                                        it.seekTo(it.currentPosition + 10000)
                                    }
                                    tenSecondHandlerReset()
                                }
                            }
                        }
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    super.onLongPress(e)
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return
                    }
                }
            }
        )

        view.playerLayout.root.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
            true
        }
    }

    override fun onViewRecycled(position: Int, watchableModel: WatchableModel) {
        super.onViewRecycled(position, watchableModel)
        view.vm?.productPlacement?.unregisterPageChangeCallback()
    }

    private fun tenSecondHandlerReset() {
        view.vm?.tenSecHandler?.postDelayed({
            view.apply {
                seekBackwardOverlay.visibility = View.INVISIBLE
                seekBackwardText.visibility = View.INVISIBLE
                seekBackwardAnimation.visibility = View.INVISIBLE
                seekForwardOverlay.visibility = View.INVISIBLE
                seekForwardText.visibility = View.INVISIBLE
                seekForwardAnimation.visibility = View.INVISIBLE
                vm?.leftClickCount = 0
                vm?.rightClickCount = 0
            }
        }, 1000)
    }

    private fun showCampaignIfPresent(campaignModel: PostModel.Video) {
        if (campaignModel.overlay != null && !campaignModel.overlay.isUserResponded) {
            view.vm?.isTvCampaignsVisible?.set(true)
            when (campaignModel.getCampaignType()) {
                PostModel.Campaigns.POLL -> {
                    view.vm?.campaignType = PostModel.Campaigns.POLL
                    view.videoLayout.tvCampaigns.setImageResource(R.drawable.ic_poll_bg)
                }

                PostModel.Campaigns.MCQ -> {
                    view.vm?.campaignType = PostModel.Campaigns.MCQ
                    view.videoLayout.tvCampaigns.setImageResource(R.drawable.ic_mcq_bg)
                }

                PostModel.Campaigns.QnA -> {
                    view.vm?.campaignType = PostModel.Campaigns.QnA
                    view.videoLayout.tvCampaigns.setImageResource(R.drawable.ic_qna_bg)
                }

                PostModel.Campaigns.CTA -> {
                    view.vm?.campaignType = PostModel.Campaigns.CTA
                    view.vm?.isCampaignCTAEnabled?.set(true)
                }

                PostModel.Campaigns.DEFAULT -> {
                    view.vm?.campaignType = PostModel.Campaigns.NO_CAMPAIGN
                    view.vm?.isTvCampaignsVisible?.set(false)
                }

                PostModel.Campaigns.NO_CAMPAIGN -> {
                    view.vm?.isTvCampaignsVisible?.set(false)
                }
            }
        } else {
            view.vm?.isTvCampaignsVisible?.set(false)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showStickersIfPresent(stickerModel: PostModel.Video) {
        with(stickerModel) {
            if (videoStickers.isNotEmpty()) {
                val inflater = LayoutInflater.from(view.root.context)
                    for (sticker in videoStickers) {
                        val stickerView = LayoutStickerViewBinding.inflate(inflater, view.mainLayout, false)

                        Glide.with(view.root.context).load(sticker.sticker.url)
                            .into(stickerView.stickerImageView)
                        val screenWidth = Utils.getScreenWidth()
                        val screenHeight = Utils.getScreenHeight()

                        stickerView.stickerImageView.x = sticker.centerX.trimEnd('%').toFloat().times(screenWidth).div(100)- sticker.sticker.width.toFloat().div(2)
                        stickerView.stickerImageView.y = sticker.centerY.trimEnd('%').toFloat().times(screenHeight).div(100)- sticker.sticker.height.toFloat().div(2)
                        stickerView.stickerImageView.setHeightAndWidth(sticker.sticker.width.toDouble()+ 100,sticker.sticker.height.toDouble()+ 100)
                        stickerView.stickerImageView.rotation = sticker.rotationAngle.toFloat()
                        view.playerLayout.mainLayout.addView(stickerView.root)
                        stickerView.stickerImageView.setOnClickListener {
                            if (stickerModel.products.isNotEmpty()){
                                productCardListener.openProductOnStickerTap(stickerModel.products[0], sticker)
                            }
                        }
                    }
            }
        }
    }

    private fun enablePlayPauseOnTap() {
        if (playerView.player?.isPlaying == true) {
            playerView.player?.pause().also {
                view.ivPlayPause.setImageResource(R.drawable.ic_pause)
                view.videoLayout.ivPlayPause.setImageResource(R.drawable.ic_pause)
                view.ivPlayPause.show()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                view.ivPlayPause.invisible()
            }, 1000)

        } else {
            playerView.player?.play().also {
                view.ivPlayPause.setImageResource(R.drawable.live2_ic_play_video)
                view.videoLayout.ivPlayPause.setImageResource(R.drawable.live2_ic_play_video)
                view.ivPlayPause.show()
            }
            Handler().postDelayed({
                view.ivPlayPause.invisible()
            }, 1000)
        }
    }

    private fun enableStoryModeTaps(e: MotionEvent): Boolean {
        view.vm?.let {
            e.let { event ->
                when {
                    event.x < view.guidelineVerticalMid.x -> {
                       videoItemClickCallbacks.onVideoTapBackward(
                            bindingAdapterPosition,
                            videoItemVM
                        )
                    }
                    else -> {
                       videoItemClickCallbacks.onVideoTapForward(
                            bindingAdapterPosition,
                            videoItemVM
                        )
                    }
                }
            }
        }
        return true
    }


    private fun updateSeekBarVisibility(videoModel: PostModel.Video){
        if (videoModel.streamType == "LIVE"){
            videoItemVM.isSeekBarEnabled.set(false)
        }
    }
}