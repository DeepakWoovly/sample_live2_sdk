package com.live2.media.ui.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.VideoSDK
import com.live2.media.L1PlayerHelper
import com.live2.media.client.model.PostModel
import com.live2.media.internal.network.ViewState
import com.live2.media.internal.Live2ViewModel


class StoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), StoryItemListener {

    private lateinit var playerHelper: L1PlayerHelper
    var lastVisibleItemPosition = 0
    private var videoPreviewHandler = Handler(Looper.getMainLooper())
    private var videosList: List<PostModel.Video>? = null
    private var videosForVideoScreen: List<PostModel.Video>? = null
    private lateinit var mContext: LifecycleOwner
    private lateinit var viewModel: Live2ViewModel

    private fun initializeComponents(list: List<PostModel.Video>) {
        if (::mContext.isInitialized) {
            playerHelper = L1PlayerHelper(mContext, context)
            val carouselAdapter = StoryAdapter( playerHelper,this, list)
            adapter = carouselAdapter

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
                        val newVideoItem = carouselAdapter.getItemAtPosition(firstVisibleItemPosition)
                        val oldVideoItem = carouselAdapter.getItemAtPosition(lastVisibleItemPosition)
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

    override fun onStoryClicked(position: Int) {
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