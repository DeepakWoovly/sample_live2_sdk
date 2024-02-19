package com.live2.media

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.live2.media.ui.campaigns.CampaignsClickCallback
import com.live2.media.internal.model.WatchableModel

abstract class CommonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    open fun onBindVewHolder(
        position: Int,
        watchableModel: WatchableModel,
        payload: MutableList<Any>?,
        videoItemClickCallbacks: VideoItemClickCallbacks,
        productCardListener: ProductCardListener,
        campaignsClickCallback: CampaignsClickCallback,
        isStory: Boolean
    ) {
    }

    open fun onViewDetached(position: Int, watchableModel: WatchableModel) {
    }

    open fun onViewRecycled(position: Int, watchableModel: WatchableModel) {
    }
}