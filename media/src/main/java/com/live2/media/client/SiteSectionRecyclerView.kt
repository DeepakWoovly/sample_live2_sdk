package com.live2.media.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.L1PlayerHelper
import com.live2.media.R
import com.live2.media.VideoSDK
import com.live2.media.client.model.PostModel
import com.live2.media.internal.network.ViewState
import com.live2.media.ui.carousel.CarouselViewHolder
import com.live2.media.ui.story.StoryViewHolder
import com.live2.media.ui.storywindow.StoryWindowViewHolder

class SiteSectionRecyclerView: RecyclerView, SiteSectionViewClickListener {
    private lateinit var playerHelper: L1PlayerHelper
    var lastVisibleItemPosition = 0
    private var videoPreviewHandler = Handler(Looper.getMainLooper())
    private var videosList: List<PostModel.Video>? = null
    private var videosForVideoScreen: List<PostModel.Video>? = null
    private lateinit var mContext: LifecycleOwner
    private lateinit var viewModel: Live2ViewModel
    private var viewTypeString: String? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SiteSectionRecyclerView)
        viewTypeString = typedArray.getString(R.styleable.SiteSectionRecyclerView_viewType)
        typedArray.recycle()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)



    private fun initializeComponentsForStory(list: List<PostModel.Video>) {
        if (::mContext.isInitialized) {
            playerHelper = L1PlayerHelper(mContext, context)
            val siteSectionViewAdapter = SiteSectionViewAdapter( playerHelper,this, list, SiteSectionType.StoryView)
            adapter = siteSectionViewAdapter

            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (firstVisibleItemPosition != lastVisibleItemPosition) {
                        videoPreviewHandler.removeCallbacksAndMessages(null)
                        val newVisibleItem = findViewHolderForAdapterPosition(firstVisibleItemPosition) as? StoryViewHolder
                        val previousVisibleItem = findViewHolderForAdapterPosition(lastVisibleItemPosition) as? StoryViewHolder
                        val newVideoItem = siteSectionViewAdapter.getItemAtPosition(firstVisibleItemPosition)
                        val oldVideoItem = siteSectionViewAdapter.getItemAtPosition(lastVisibleItemPosition)
                        lastVisibleItemPosition = firstVisibleItemPosition
                        previousVisibleItem?.loadImage(oldVideoItem)
                        videoPreviewHandler.postDelayed({
                            newVisibleItem?.loadMedia(newVideoItem)
                        },800)
                    }
                }
            })
        }
    }
    private fun initializeComponentsForStoryWindow(list: List<PostModel.Video>) {
        if (::mContext.isInitialized) {
            playerHelper = L1PlayerHelper(mContext, context)
            val siteSectionViewAdapter = SiteSectionViewAdapter( playerHelper,this, list, SiteSectionType.StoryWindowView)
            adapter = siteSectionViewAdapter

            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (firstVisibleItemPosition != lastVisibleItemPosition) {
                        videoPreviewHandler.removeCallbacksAndMessages(null)
                        val newVisibleItem = findViewHolderForAdapterPosition(firstVisibleItemPosition) as? StoryWindowViewHolder
                        val previousVisibleItem = findViewHolderForAdapterPosition(lastVisibleItemPosition) as? StoryWindowViewHolder
                        val newVideoItem = siteSectionViewAdapter.getItemAtPosition(firstVisibleItemPosition)
                        val oldVideoItem = siteSectionViewAdapter.getItemAtPosition(lastVisibleItemPosition)
                        lastVisibleItemPosition = firstVisibleItemPosition
                        previousVisibleItem?.loadImage(oldVideoItem)
                        videoPreviewHandler.postDelayed({
                            newVisibleItem?.loadMedia(newVideoItem)
                        },800)
                    }
                }
            })
        }
    }
    private fun initializeComponentsForCarousel(list: List<PostModel.Video>) {
        if (::mContext.isInitialized) {
            playerHelper = L1PlayerHelper(mContext, context)
            val siteSectionViewAdapter = SiteSectionViewAdapter( playerHelper,this, list, SiteSectionType.Carousel)
            adapter = siteSectionViewAdapter

            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (firstVisibleItemPosition != lastVisibleItemPosition) {
                        videoPreviewHandler.removeCallbacksAndMessages(null)
                        val newVisibleItem = findViewHolderForAdapterPosition(firstVisibleItemPosition) as? CarouselViewHolder
                        val previousVisibleItem = findViewHolderForAdapterPosition(lastVisibleItemPosition) as? CarouselViewHolder
                        val newVideoItem = siteSectionViewAdapter.getItemAtPosition(firstVisibleItemPosition)
                        val oldVideoItem = siteSectionViewAdapter.getItemAtPosition(lastVisibleItemPosition)
                        lastVisibleItemPosition = firstVisibleItemPosition
                        previousVisibleItem?.loadImage(oldVideoItem)
                        videoPreviewHandler.postDelayed({
                            newVisibleItem?.loadMedia(newVideoItem)
                        },800)
                    }
                }
            })
        }
    }



    fun init(context: LifecycleOwner, embedId: String){
        this.mContext = context
        viewModel = Live2ViewModel()
        if (viewModel.isInitialized()){
            viewModel.fetchFirstSetOfData(embedId)
        }
        observe()
    }

    private fun observe(){
        viewModel.siteSectionsLiveData.observe(mContext){state ->
            when(state){
                is ViewState.Error -> {
                }
                ViewState.Loading -> {}
                is ViewState.Success -> {
                    videosList = state.data.videos
                    initializeComponents(state.data.videos)
                }
            }
        }
    }

    private fun initializeComponents(list: List<PostModel.Video>){
        when (viewTypeString?.let { ViewType.fromValue(it) }) {
            ViewType.StoryView -> {
                initializeComponentsForStory(list)
            }
            ViewType.StoryWindowView -> {
                initializeComponentsForStoryWindow(list)
            }
            ViewType.Carousel -> {
                initializeComponentsForCarousel(list)
            }
            else -> {}
        }
    }

    override fun onItemClicked(position: Int) {
        val intent = Intent(context, VideoSDK::class.java)
        val bundle = Bundle()
        val arrayList = arrayListOf<PostModel.Video>()
        videosForVideoScreen = videosList?.subList(position, videosList?.size!!)
        videosForVideoScreen?.let { arrayList.addAll(it) }
        bundle.putBoolean("isStory", true)
        bundle.putParcelableArrayList("List", arrayList )
        intent.putExtras(bundle)
        context.startActivity(intent)
    }
}