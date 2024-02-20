package com.live2.media.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.live2.media.L1PlayerHelper
import com.live2.media.databinding.LayoutCarouselItemBinding
import com.live2.media.databinding.LayoutStoryItemBinding
import com.live2.media.databinding.LayoutWindowStoryItemBinding
import com.live2.media.client.model.PostModel
import com.live2.media.ui.carousel.CarouselViewHolder
import com.live2.media.ui.story.StoryViewHolder
import com.live2.media.ui.storywindow.StoryWindowViewHolder

class SiteSectionViewAdapter(
    private val playerHelper: L1PlayerHelper,
    private val siteSectionViewClickListener: SiteSectionViewClickListener,
    private val videoList: List<PostModel.Video>,
    private val type: SiteSectionType
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (type) {
            SiteSectionType.StoryView -> {
                return StoryViewHolder(
                    LayoutStoryItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            SiteSectionType.StoryWindowView -> {
                return StoryWindowViewHolder(
                    LayoutWindowStoryItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            SiteSectionType.Carousel -> {
                return CarouselViewHolder(
                    LayoutCarouselItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (type) {
            SiteSectionType.StoryView -> {
                (holder as StoryViewHolder).bind(videoList[position], playerHelper, siteSectionViewClickListener , position)
            }
            SiteSectionType.StoryWindowView -> {
                (holder as StoryWindowViewHolder).bind(videoList[position], playerHelper, siteSectionViewClickListener , position)
            }
            SiteSectionType.Carousel -> {
                (holder as CarouselViewHolder).bind(videoList[position], playerHelper, siteSectionViewClickListener , position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return type.ordinal
    }


    override fun getItemCount(): Int = videoList.size

    fun getItemAtPosition(firstVisibleItemPosition: Int): PostModel.Video =
        videoList[firstVisibleItemPosition]

}