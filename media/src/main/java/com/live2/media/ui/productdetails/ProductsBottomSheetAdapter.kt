package com.live2.media.ui.productdetails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live2.media.databinding.BottomsheetProductItemBinding
import com.live2.media.client.model.PostModel
import com.live2.media.utils.Utils.Companion.gone

class ProductsBottomSheetAdapter(private val productsList: List<PostModel.Product>) :
    RecyclerView.Adapter<ProductsBottomSheetAdapter.ProductsViewHolder>() {

    private lateinit var context: Context
    inner class ProductsViewHolder(val view: BottomsheetProductItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: PostModel.Product) {
            with(product){
                view.apply {
                    val productImageUrl = product_image.substring(0, product_image.length - 7)
                    Glide.with(context).load(productImageUrl).into(ivProductImage)
                    tvProductTitle.text = product_name
                    tvProductPrice.text = "\u20B9" + product_discounted_price.toString()
                    if (product_discount.isEmpty() || product_discount == "0") {
                        verticalLineSeparator1.gone()
                        tvProductDiscount.gone()
                    } else {
                        tvProductDiscount.text = "$product_discount%"
                        tvProductPriceOriginal.text = product_price.toString()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        context = parent.context
        return ProductsViewHolder(
            BottomsheetProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        holder.bind(productsList[position])
    }
}