package com.live2.media.ui.carousel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.L1PlayerHelper
import com.live2.media.databinding.LayoutCarouselItemBinding
import com.live2.media.client.model.PostModel

class CarouselAdapter(
    private val playerHelper: L1PlayerHelper,
    private val carouselItemListener: CarouselItemListener,
    private val videoList: List<PostModel.Video>,
    private val isGrid: Boolean
) : RecyclerView.Adapter<CarouselViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        return CarouselViewHolder(
            LayoutCarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return videoList.size
    }


    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
      //  holder.bind(videoList[position], playerHelper, carouselItemListener, position, isGrid)
    }

    fun getItemAtPosition(firstVisibleItemPosition: Int): PostModel.Video =
        videoList[firstVisibleItemPosition]


    override fun onViewRecycled(holder: CarouselViewHolder) {
        super.onViewRecycled(holder)
    }

}