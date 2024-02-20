package com.live2.media

import androidx.recyclerview.widget.DiffUtil
import com.live2.media.client.model.PostModel
import com.live2.media.client.model.WatchableModel

class WatchableModelDiffCallback : DiffUtil.ItemCallback<WatchableModel>() {

    @Suppress("ReturnCount")
    override fun areItemsTheSame(oldItem: WatchableModel, newItem: WatchableModel): Boolean {
        if (oldItem is PostModel.Video && newItem is PostModel.Video) {
            return oldItem.id == newItem.id
        }
        return false
    }

    @Suppress("TooGenericExceptionCaught")
    override fun areContentsTheSame(oldItem: WatchableModel, newItem: WatchableModel): Boolean {
        return try {
            oldItem.hashCode() == newItem.hashCode()
        } catch (e: NullPointerException) {
            false
        }
    }
}