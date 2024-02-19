package com.example.videosdk.feature.carousel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videosdk.VideoSDK
import com.example.videosdk.feature.L1PlayerHelper
import com.example.videosdk.network.model.PostModel
import java.util.ArrayList

class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), CarouselItemListener {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var playerHelper: L1PlayerHelper
    var lastVisibleItemPosition = 0
    private var videoPreviewHandler = Handler(Looper.getMainLooper())
    private var videosList: List<PostModel.Video>? = null
    private var videosForVideoScreen: List<PostModel.Video>? = null
    private var isGridLayout = false
    private lateinit var carouselAdapter: CarouselAdapter

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

    fun isGridLayoutManager(isGrid: Boolean) {
        isGridLayout = isGrid
    }

    private fun initializeComponents() {
        if (::lifecycleOwner.isInitialized) {
            layoutManager = if (isGridLayout) {
                playerHelper = L1PlayerHelper(lifecycleOwner, context)
                carouselAdapter = CarouselAdapter(playerHelper, this, videosList!!, true)
                adapter = carouselAdapter
                GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            } else {
                playerHelper = L1PlayerHelper(lifecycleOwner, context)
                carouselAdapter = CarouselAdapter(playerHelper, this, videosList!!, false)
                adapter = carouselAdapter
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (isGridLayout){
                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
                        val firstVisibleItemPosition =
                            layoutManager.findFirstCompletelyVisibleItemPosition()
                        if (firstVisibleItemPosition != lastVisibleItemPosition) {
                            val spanSizeLookup = layoutManager.spanSizeLookup
                            val spanIndex = spanSizeLookup.getSpanIndex(
                                firstVisibleItemPosition,
                                layoutManager.spanCount
                            )
                            if (spanIndex == 0) {
                                videoPreviewHandler.removeCallbacksAndMessages(null)
                                val newVisibleItem =
                                    findViewHolderForAdapterPosition(firstVisibleItemPosition) as? CarouselViewHolder
                                val previousVisibleItem =
                                    findViewHolderForAdapterPosition(lastVisibleItemPosition) as? CarouselViewHolder
                                val newVideoItem =
                                    carouselAdapter.getItemAtPosition(firstVisibleItemPosition)
                                val oldVideoItem =
                                    carouselAdapter.getItemAtPosition(lastVisibleItemPosition)
                                lastVisibleItemPosition = firstVisibleItemPosition
                                previousVisibleItem?.loadImage(oldVideoItem)
                                videoPreviewHandler.postDelayed({
                                    newVisibleItem?.loadMedia(newVideoItem)
                                }, 800)
                            }
                        }
                    }else{
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition =
                            layoutManager.findFirstCompletelyVisibleItemPosition()
                        if (firstVisibleItemPosition != lastVisibleItemPosition) {
                            videoPreviewHandler.removeCallbacksAndMessages(null)
                            val newVisibleItem =
                                findViewHolderForAdapterPosition(firstVisibleItemPosition) as? CarouselViewHolder
                            val previousVisibleItem =
                                findViewHolderForAdapterPosition(lastVisibleItemPosition) as? CarouselViewHolder
                            val newVideoItem =
                                carouselAdapter.getItemAtPosition(firstVisibleItemPosition)
                            val oldVideoItem =
                                carouselAdapter.getItemAtPosition(lastVisibleItemPosition)
                            lastVisibleItemPosition = firstVisibleItemPosition
                            previousVisibleItem?.loadImage(oldVideoItem)
                            videoPreviewHandler.postDelayed({
                                newVisibleItem?.loadMedia(newVideoItem)
                            }, 800)
                        }
                    }
                }
            })
        }
    }

    override fun onCarouselItemClicked(position: Int) {
        val intent = Intent(context, VideoSDK::class.java)
        val bundle = Bundle()
        val arrayList = arrayListOf<PostModel.Video>()
        videosForVideoScreen = videosList?.subList(position, videosList?.size!!)
        videosForVideoScreen?.let { arrayList.addAll(it) }
        bundle.putParcelableArrayList("List", arrayList)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun setVideosList(list: List<PostModel.Video>) {
        videosList = list
    }

}