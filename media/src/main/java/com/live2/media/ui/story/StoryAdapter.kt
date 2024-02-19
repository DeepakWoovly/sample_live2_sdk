package com.example.videosdk.feature.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videosdk.databinding.LayoutStoryItemBinding
import com.example.videosdk.feature.L1PlayerHelper
import com.example.videosdk.network.model.PostModel

class StoryAdapter(
    private val playerHelper: L1PlayerHelper,
    private val storyItemListener: StoryItemListener,
    private val videoList: List<PostModel.Video>
): RecyclerView.Adapter<StoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        return StoryViewHolder(
            LayoutStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = videoList.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(videoList[position], playerHelper, storyItemListener, position)
    }

    fun getItemAtPosition(firstVisibleItemPosition: Int): PostModel.Video =
        videoList[firstVisibleItemPosition]
}