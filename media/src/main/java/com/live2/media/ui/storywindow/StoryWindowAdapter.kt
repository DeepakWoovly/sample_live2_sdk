package com.example.videosdk.feature.storywindow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videosdk.databinding.LayoutWindowStoryItemBinding
import com.example.videosdk.feature.L1PlayerHelper
import com.example.videosdk.network.model.PostModel

class StoryWindowAdapter(
    private val playerHelper: L1PlayerHelper,
    private val storyItemListener: StoryWindowItemListener,
    private val videoList: List<PostModel.Video>
): RecyclerView.Adapter<StoryWindowViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryWindowViewHolder {
        return StoryWindowViewHolder(
            LayoutWindowStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = videoList.size

    override fun onBindViewHolder(holder: StoryWindowViewHolder, position: Int) {
        holder.bind(videoList[position], playerHelper, storyItemListener, position)
    }

    fun getItemAtPosition(firstVisibleItemPosition: Int): PostModel.Video =
        videoList[firstVisibleItemPosition]
}