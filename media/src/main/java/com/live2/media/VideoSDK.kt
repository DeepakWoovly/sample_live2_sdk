package com.live2.media

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.core.exoplayer.OnSnapPositionChangeListener
import com.live2.media.core.exoplayer.PlayerHelper
import com.live2.media.core.exoplayer.SnapOnScrollListener
import com.live2.media.core.exoplayer.VideoCacheHelper
import com.live2.media.ui.campaigns.CampaignsClickCallback
import com.live2.media.ui.productdetails.ProductDetailsBottomSheet
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.live2.media.client.model.PostModel
import com.live2.media.internal.network.ViewState
import com.live2.media.client.Live2ViewModel
import com.live2.media.utils.Utils
import com.live2.media.utils.Utils.Companion.gone
import com.live2.media.utils.Utils.Companion.show
import kotlin.math.abs

class VideoSDK : AppCompatActivity(), OnSnapPositionChangeListener, ProductCardListener,
    CampaignsClickCallback, VideoItemClickCallbacks {
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var playerHelper: PlayerHelper
    private lateinit var pagerSnapHelper: PagerSnapHelper
    private var pos = 0
    private val exoPlayerCacheSize: Long = 90 * 1024 * 1024
    private lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
    private lateinit var exoDatabaseProvider: ExoDatabaseProvider
    private var videoItemVM: VideoItemVM? = null
    private lateinit var mainViewModel: Live2ViewModel
    private lateinit var videosList: List<PostModel.Video>
    private var isStory = false


    companion object {
        var videoPosition = ObservableInt(0)
        var countDownTimer = 0
        //lateinit var simpleCache: SimpleCache
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_sdk)
        mainViewModel = ViewModelProvider(this)[Live2ViewModel::class.java]
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = ExoDatabaseProvider(this)
        // simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

        val defaultDataSourceFactory = DefaultDataSource.Factory(this)
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
            this, defaultDataSourceFactory
        )

        pagerSnapHelper = PagerSnapHelper()
        recyclerView = findViewById(R.id.recyclerView)
        playerHelper = PlayerHelper(this, this) {
            val newPosition = pos + 1
            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }

            smoothScroller.targetPosition = newPosition
            recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            Handler(Looper.getMainLooper()).postDelayed({
                notifyRvPositionChanged(newPosition)
            }, 200)
        }

        val firstTrailModelToCache =
            getSampleVideoList().asSequence().filterIsInstance<PostModel.Video>().firstOrNull()
        observe()
        val bundle = intent.extras
        videosList = bundle?.getParcelableArrayList<PostModel.Video>("List")?.toList() ?: listOf()
        if (bundle?.getBoolean("isStory", false) != null) {
            isStory = bundle.getBoolean("isStory", false)
        }

        videoAdapter = VideoAdapter(
            playerHelper, VideoCacheHelper, this, this, this, isStory
        )

        videoAdapter.submitList(videosList)
        initTrailRv()
    }

    private fun observe() {
        mainViewModel.videoInteractionsLiveData.observe(this, Observer { state ->

            when (state) {
                is ViewState.Error -> {

                }

                ViewState.Loading -> {}
                is ViewState.Success -> {
                    videoAdapter.getVms(videoAdapter.pos).apply {
                        if (this != null) {
                            shouldShowFeedbackToast.set(true)
                            isTvCampaignsVisible.set(false)
                            item.overlay?.isUserResponded = true
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                shouldShowFeedbackToast.set(false)
                            }, 1500)
                        }
                    }
                }
            }
        })
    }

    private fun attachAdapterToRecyclerView() {
        recyclerView.adapter = videoAdapter
    }

    private fun initTrailRv() {
        attachAdapterToRecyclerView()
        if (isStory) {
            recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }
        pagerSnapHelper.attachToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(snapOnScrollListener)

    }

    private val flingThreshold by lazy {
        Resources.getSystem().displayMetrics.heightPixels * 0.95
    }

    private val helper by lazy {
        object : PagerSnapHelper() {
            override fun calculateDistanceToFinalSnap(
                layoutManager: RecyclerView.LayoutManager, targetView: View
            ): IntArray {
                val distanceToFinalSnap = super.calculateDistanceToFinalSnap(
                    layoutManager, targetView
                )!! //this is safe because base class is PageSnapHelper
                distanceToFinalSnap[1] = if (abs(distanceToFinalSnap[1]) > flingThreshold) {
                    0
                } else {
                    distanceToFinalSnap[1]
                }
                return distanceToFinalSnap
            }
        }
    }

    private val snapOnScrollListener: SnapOnScrollListener by lazy {
        SnapOnScrollListener(
            helper, SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE, this
        )
    }


    private fun getSampleVideoList(): List<PostModel> {
        // Replace this with your actual list of video URLs
        return listOf()
    }

    override fun onSnapPositionChange(position: Int) {
        if (isStory) {
            if (position == 0) {
                return
            }
            val currentHolder =
                recyclerView.findViewHolderForAdapterPosition(position) as VideoViewHolder
            val watchableModel = videoAdapter.getCurrentItem(position)
            if (watchableModel is PostModel.Video) {
                currentHolder.playerWatchBodyView.loadMedia(watchableModel)

            }
        }
    }

    private fun gotoNextItemInRecyclerView(position: Int) {
        val newPosition = position + 1
        notifyRvPositionChanged(newPosition)
    }

    private fun gotoPreviousItemInRecyclerView(position: Int) {
        val newPosition = position - 1
        if (newPosition > 0) {
            notifyRvPositionChanged(newPosition)
        }
    }

    private fun notifyRvPositionChanged(newPosition: Int) {
        recyclerView.smoothScrollToPosition(newPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::videoAdapter.isInitialized) videoAdapter.clear()
        // videoEndedDetector.stopListening()
        // Release the SimpleExoPlayer or perform other cleanup as needed
        playerHelper.player?.release()
        if (::videoAdapter.isInitialized) videoAdapter.clear()

    }

    private fun notifyTrailChanged() {
        videoAdapter.listener.invoke(videoAdapter.pos)
    }

    fun playerOnPause() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            // playerHelper.
        }
    }

    fun playerOnResume() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            playerHelper.playVideo()
        }
    }

    override fun onProductCardClicked(productModel: PostModel.Product) {
        val bottomSheet =
            productModel.product_details?.let { ProductDetailsBottomSheet.newInstance(productModel = it) }
        bottomSheet?.show(supportFragmentManager!!, ProductDetailsBottomSheet.TAG)

    }


    override fun onOnCartClicked(productModel: PostModel.Product) {

    }

    override fun openProductOnStickerTap(
        productModel: PostModel.Product, stickerModel: PostModel.VideoSticker
    ) {
        val bottomSheet =
            productModel.product_details?.let { ProductDetailsBottomSheet.newInstance(productModel = it) }
        bottomSheet?.show(supportFragmentManager!!, ProductDetailsBottomSheet.TAG)
    }

    override fun onMCQSubmitClicked(
        publicCampaignId: String, optionIds: List<String>, optionTextList: List<String>
    ) {
        mainViewModel.submitMcqResponse(
            publicCampaignId = publicCampaignId,
            optionIds = optionIds,
            optionTextList = optionTextList
        )
    }

    override fun onPollSubmitClicked(
        publicCampaignId: String, optionId: String, optionText: String
    ) {
        mainViewModel.submitPollResponse(
            publicCampaignId = publicCampaignId, optionId = optionId, optionText = optionText
        )
    }

    override fun onQuestionSubmitClicked(publicCampaignId: String, answerText: String) {
        mainViewModel.submitQuestionResponse(
            publicCampaignId = publicCampaignId, answerText = answerText
        )
    }

    override fun onPlayPauseClicked(videoItemVM: VideoItemVM) {
        if (playerHelper.player?.isPlaying == false) {
            videoItemVM.binding.videoLayout.ivPlayPause.setImageResource(R.drawable.live2_ic_play_video)
            playerHelper.player?.play()
        } else {
            videoItemVM.binding.videoLayout.ivPlayPause.setImageResource(R.drawable.ic_pause)
            playerHelper.player?.pause()
        }
    }

    override fun onShareClicked(videoItemVM: VideoItemVM) {
        val video = videoItemVM.video
        val videoDetails = videoItemVM.item

        val fileDownloadPath =
            applicationContext.getExternalFilesDir(null)?.path + "/trelldownloads/" + "${videoDetails.title}.mp4"

        Utils.shareVideo(
            context = applicationContext,
            videoTitle = videoDetails.title,
            appUrl = "https://play.google.com/store/apps/details?id=com.woovly.bucketlist&hl=en_IN&gl=US&pli=1",
            webUrl = "https://live2.ai/#shoppable_videos_live2ai",
            userId = "12345",
            isVideo = true,
            downloadedFilePath = fileDownloadPath
        )
    }

    override fun onMuteUnMuteClicked(videoItemVM: VideoItemVM) {
        if (playerHelper.player?.volume == 0f) {
            playerHelper.player?.volume = 1f
            videoItemVM.binding.videoLayout.ivMute.setBackgroundResource(R.drawable.btn_bg1)
            videoItemVM.binding.videoLayout.ivMute.setImageResource(R.drawable.ic_unmute_video)
        } else {
            playerHelper.player?.volume = 0f
            videoItemVM.binding.videoLayout.ivMute.setBackgroundResource(R.drawable.btn_bg)
            videoItemVM.binding.videoLayout.ivMute.setImageResource(R.drawable.ic_mute_video)
        }
    }

    override fun onCloseClicked(videoItemVM: VideoItemVM) {
        playerHelper.apply {
            player?.stop()
            player?.release()
            onDestroy()
            player = null
        }
        finish()
    }

    override fun onCampaignClicked(videoItemVM: VideoItemVM) {
        when (videoItemVM.campaignType) {
            PostModel.Campaigns.POLL -> {
                videoItemVM.openPollsDialog()
            }

            PostModel.Campaigns.MCQ -> {
                videoItemVM.openMCQDialog()
            }

            PostModel.Campaigns.QnA -> {
                videoItemVM.openQnaDialog()
            }

            PostModel.Campaigns.CTA -> {}

            PostModel.Campaigns.DEFAULT -> {}

            PostModel.Campaigns.NO_CAMPAIGN -> {}
        }
    }

    override fun onCampaignCTAClicked(videoItemVM: VideoItemVM) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPipClicked(videoItemVM: VideoItemVM) {
        this.videoItemVM = videoItemVM
        if (hasPipPermission(this)) {
            if (!isInPictureInPictureMode) {
                enterPiPMode()
                hideControllers(videoItemVM)
            }
        } else {
            startActivityForResult(
                Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                    Uri.parse("package:$packageName")
                ), 0
            )
        }
    }

    override fun onVideoTapForward(position: Int, videoItemVM: VideoItemVM) {
        gotoNextItemInRecyclerView(position)
    }

    override fun onVideoTapBackward(position: Int, videoItemVM: VideoItemVM) {
        gotoPreviousItemInRecyclerView(position)
    }

    private fun enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16))
                // Set the preferred aspect ratio for portrait shape
                .build()
            enterPictureInPictureMode(params)
        }
    }

    private fun showControls() {
        with(videoItemVM?.binding?.videoLayout!!) {
            ivMute.show()
            ivClose.show()
            ivPip.show()
            ivShare.show()
            ivPlayPause.show()
            tvDescription.show()
            tvDuration.show()
            if (videoItemVM?.item?.products?.isNotEmpty() == true) {
                productLayout.root.show()
            }
            if (videoItemVM?.campaignType == PostModel.Campaigns.CTA) {
                campaignCTA.show()
            }
            if (videoItemVM?.isTvCampaignsVisible?.get() == true) {
                tvCampaigns.show()
            }
        }
    }

    private fun hideControllers(videoItemVM: VideoItemVM) {
        with(videoItemVM.binding.videoLayout) {
            ivMute.gone()
            ivClose.gone()
            ivPip.gone()
            ivShare.gone()
            ivPlayPause.gone()
            productLayout.root.gone()
            campaignCTA.gone()
            tvCampaigns.gone()
            tvDescription.gone()
            tvDuration.gone()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            // Resume video playback or perform other actions
            playerHelper.playVideo()
        } else {
            showControls()
        }
    }

    private fun hasPipPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps?.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION") appOps?.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } else {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }
    }

}