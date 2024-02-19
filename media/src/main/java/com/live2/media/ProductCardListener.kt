package com.live2.media

import com.live2.media.internal.model.PostModel


interface ProductCardListener {
    fun onProductCardClicked(productModel: PostModel.Product)
    fun onOnCartClicked(productModel: PostModel.Product)
    fun openProductOnStickerTap(productModel: PostModel.Product, stickerModel: PostModel.VideoSticker)
}