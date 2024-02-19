package com.example.videosdk.feature

import com.example.videosdk.network.model.PostModel

interface ProductCardListener {
    fun onProductCardClicked(productModel: PostModel.Product)
    fun onOnCartClicked(productModel: PostModel.Product)

    fun openProductOnStickerTap(productModel: PostModel.Product, stickerModel: PostModel.VideoSticker)
}