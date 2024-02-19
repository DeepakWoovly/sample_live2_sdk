package com.live2.media.internal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize

class PostModel(override val itemType: Int) : WatchableModel, Parcelable{
    data class Model(
        val data: Data,
        val success: Boolean
    )

    @Parcelize
    data class Data(
        val layoutType: Int,
        val layoutTypeName: String,
        val sectionSubTitle: String,
        val sectionTitle: String,
        val showSectionSubTitle: Boolean,
        val showSectionTitle: Boolean,
        val videos: List<Video>
    ): Parcelable

    enum class Campaigns{
        POLL, MCQ, QnA, CTA, DEFAULT, NO_CAMPAIGN
    }

    @Parcelize
    data class Video(
        val createdAt: String? = null,
        val createdBy: String? = null,
        val deletedAt: String? = null,
        val id: String,
        val labels: List<String>? = null,
        val overlay: Overlay? = null,
        val products: List<Product>,
        val sampleProduct: List<SampleProduct>? = null,
        val publicVideoId: String? = null,
        val status: String? = null,
        val streamType: String? = null,
        val teamId: String? = null,
        val thumbnailAssetId: String? = null,
        val thumbnailUrl: String,
        val title: String,
        val updatedAt: String? = null,
        val videoAssetId: String? = null,
        val videoUrl: String,
        val visibility: String? = null,
        val videoStickers: List<VideoSticker>,
        var isPlaying:Boolean = false,
        var isLive: Boolean = false
    ) : WatchableModel, Parcelable {
        override val itemType: Int
            get() = TODO("Not yet implemented")

        fun getCampaignType():Campaigns{
            return when(overlay?.layoutId){
                1 -> Campaigns.DEFAULT
                2 -> Campaigns.POLL
                3 -> Campaigns.MCQ
                4 -> Campaigns.QnA
                5 -> Campaigns.CTA
                else -> Campaigns.NO_CAMPAIGN
            }
        }
    }

    @Parcelize
    data class VariantOption(
        val key: String,
        val value: String
    ):Parcelable

    @Parcelize
    data class VariantImage(
        val publicImageId: String
    ): Parcelable

    @Parcelize
    data class ProductVariant(
        val compareAtPriceX100: Int? = null,
        val createdAt: String? = null,
        val description: String? = null,
        val descriptionHtml: String? = null,
        val discountedPrice: String? = null ,
        val externalProductId: String? = null,
        val externalVariantId: String? = null,
        val id: String? = null,
        val isDefaultVariant: Boolean? = null,
        val isInventoryAvailable: Boolean? = null,
        val productId: String? = null,
        val sellingPriceX100: Int? = null,
        val storeId: String? = null,
        val teamId: String? = null,
        val title: String? = null,
        val updatedAt: String? = null,
        val url: String? = null,
        val variantImages: List<VariantImage>? = null,
        val variant_name: String? = null,
        val variant_options: List<VariantOption>? = null
    ): Parcelable

    @Parcelize
    data class ProductDetails(
        val attributeSet: List<AttributeSet>,
        val product_description: String,
        val product_discount: String,
        val product_discounted_price: Int,
        val product_id: String,
        val product_images: List<String>,
        val product_name: String,
        val product_price: Double,
        val product_variants: List<ProductVariant>? = null
    ): Parcelable

    @Parcelize
    data class Product(
        val is_product_discount_visible: Boolean? = null,
        val product_details: ProductDetails,
        val product_discount: String,
        val product_discounted_price: Int,
        val product_id: String,
        val product_image: String,
        val product_name: String,
        val product_price: Double
    ): Parcelable

    @Parcelize
    data class SampleProduct(
        val product_image: String
    ): Parcelable

    @Parcelize
    data class Overlay(
        val data: OverlayData,
        val layoutId: Int,
        val campaignId: String,
        var isUserResponded: Boolean = false
    ): Parcelable

    @Parcelize
    data class OverlayData(
        val options: List<Option>? = null,
        val question: String? = null
    ): Parcelable

    @Parcelize
    data class Option(
        val id: String,
        val value: String
    ): Parcelable

    @Parcelize
    data class AttributeSet(
        val allowedValues: List<String>,
        val displayName: String,
        val externalAttributeId: String,
        val isRequired: Boolean,
        val isVisible: Boolean,
        val key: String,
        val position: Int,
        val type: String
    ): Parcelable

    @Parcelize
    data class VideoSticker(
        val centerX: String,
        val centerY: String,
        val createdAt: String,
        val createdBy: String,
        val endTime: String,
        val id: String,
        val rotationAngle: String,
        val scale: String,
        val startTime: String,
        val stickerId: String,
        val updatedAt: String,
        val videoId: String,
        val sticker: Stickers,
        val payload: PayLoad? = null
    ): Parcelable

    @Parcelize
    data class Stickers(
        val url: String,
        val height: String,
        val width: String
    ): Parcelable

    @Parcelize
    data class PayLoad(
        val url: String? = null,
        val productId: String? = null,
        val uiType: String? = null
    ) : Parcelable
}