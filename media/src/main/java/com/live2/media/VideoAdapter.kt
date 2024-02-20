package com.live2.media

// VideoAdapter.kt
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.core.exoplayer.PlayerHelper
import com.live2.media.core.exoplayer.VideoCacheHelper
import com.live2.media.ui.campaigns.CampaignsClickCallback
import com.live2.media.databinding.ItemVideoBinding
import com.live2.media.client.model.WatchableModel
import kotlinx.coroutines.Job


class VideoAdapter(
    val playerHelper: PlayerHelper,
    val cacheHelper: VideoCacheHelper,
    private var videoItemClickCallbacks: VideoItemClickCallbacks?,
    private val productCardListener: ProductCardListener,
    private val campaignsClickCallback: CampaignsClickCallback,
    private val isStory: Boolean
) :
    ListAdapter<WatchableModel, CommonViewHolder>(WatchableModelDiffCallback()) {
    var globalIsVideoFinished: Boolean = false
    var videoItemHashMap = HashMap<Int, ItemVideoBinding?>()
    val vms = mutableMapOf<Int, VideoItemVM>()
    private var videoPosition: Int = 0
    private var progressDisposable: Job? = null
    var pos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemView = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(itemView, this)
    }


    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        // holder.onBindVewHolder(position, getItem(position), null, null, null)
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.recycledViewPool.clear()
        for ((_, v) in videoItemHashMap) {
            v?.vm?.apply {
                v.clickCallbacks = null
                v.vm = null
                v.unbind()
            }
        }
    }



    override fun onViewAttachedToWindow(holder: CommonViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.d("VIDEOSDK", holder.bindingAdapterPosition.toString())
        pos = holder.bindingAdapterPosition
        holder.onBindVewHolder(
            pos,
            getItem(pos),
            payload = mutableListOf(),
            videoItemClickCallbacks!!,
            productCardListener,
            campaignsClickCallback,
            isStory
        )
    }

    private fun clearBindingMap() {
        for ((_, v) in videoItemHashMap) {
            v?.vm?.apply {
                v.clickCallbacks = null
                v.vm = null
                v.unbind()
            }
        }
    }

    fun getVms(position: Int): VideoItemVM? {
        return vms[position]
    }

    val videoFinishedListener = {
        if (!globalIsVideoFinished) {
            globalIsVideoFinished = true
            playerHelper.timer.startTimer()
        }
    }


    val loadingStateListener = {
        if (!globalIsVideoFinished) {
            val trailPosition = videoPosition
            if (itemCount > (trailPosition + 1)) {
                val cacheSize = 4
                //    val videosToCache = videoList.subList(trailPosition + 1, itemCount).take(cacheSize)
                //  cacheHelper.cacheVideos(videosToCache)
            }
        }
    }

    val listener = { position: Int ->
        clearBindingMap()
        //watchFragment.observePlayerProgressForRecommendation()
        vms.values.forEach { it.pauseSeekbar() }
        val currentVm = getVms(position)
        currentVm?.let {
            it.initSeekbar()
            clearProgressDisposable()
        }
    }

    private fun clearProgressDisposable() {
        if (progressDisposable != null) {
            progressDisposable?.cancel()
            progressDisposable = null
        }
    }

    fun clear() {
        videoItemClickCallbacks = null
        vms.values.forEach { it.pauseSeekbar() }
    }

    fun getCurrentItem(position: Int): WatchableModel {
        return getItem(position)
    }
}