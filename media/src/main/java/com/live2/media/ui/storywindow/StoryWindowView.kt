package com.example.videosdk.feature.storywindow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videosdk.VideoSDK
import com.example.videosdk.feature.L1PlayerHelper
import com.example.videosdk.network.model.PostModel


class StoryWindowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), StoryWindowItemListener {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var playerHelper: L1PlayerHelper
    var lastVisibleItemPosition = 0
    private var videoPreviewHandler = Handler(Looper.getMainLooper())
    private var videoPreviewRunnable: Runnable? = null
    private var videosList: List<PostModel.Video>? = null
    private var videosForVideoScreen: List<PostModel.Video>? = null

    private fun observeLifecycle() {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                initializeComponents()
            }
        })
    }

    private fun initializeComponents() {
        if (::lifecycleOwner.isInitialized) {
            playerHelper = L1PlayerHelper(lifecycleOwner, context)
            val carouselAdapter = StoryWindowAdapter( playerHelper,this, videosList!!)
            adapter = carouselAdapter

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
                        val newVideoItem = carouselAdapter.getItemAtPosition(firstVisibleItemPosition)
                        val oldVideoItem = carouselAdapter.getItemAtPosition(lastVisibleItemPosition)
                        lastVisibleItemPosition = firstVisibleItemPosition
                        previousVisibleItem?.loadImage(oldVideoItem)
                        videoPreviewHandler.postDelayed({
                            newVisibleItem?.loadMedia(newVideoItem)
                        }, 800)
                    }
                }
            })
        }
    }

    fun setLifeCycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
        observeLifecycle()
    }

    fun setVideosList(list: List<PostModel.Video>){
        videosList = list
    }

    override fun onStoryClicked(position: Int) {
        val intent = Intent(context, VideoSDK::class.java)
        val bundle = Bundle()
        val arrayList = arrayListOf<PostModel.Video>()
        videosForVideoScreen = videosList?.subList(position, videosList?.size!!)
        videosForVideoScreen?.let { arrayList.addAll(it) }
        bundle.putParcelableArrayList("List", arrayList )
        intent.putExtras(bundle)
        context.startActivity(intent)
    }
}