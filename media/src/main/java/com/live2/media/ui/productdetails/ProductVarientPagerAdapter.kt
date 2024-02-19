package com.example.videosdk.feature.productdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.videosdk.R
import com.example.videosdk.databinding.ProductPagerItemBinding
import com.example.videosdk.network.model.PostModel

class ProductVariantPagerAdapter(private val context: Context, private val productImageList: List<String>):
    RecyclerView.Adapter<ProductVariantPagerAdapter.ProductVariantHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVariantHolder {
        val layout = ProductPagerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductVariantHolder(layout)
    }

    override fun getItemCount(): Int {
        return productImageList.size
    }

    override fun onBindViewHolder(holder: ProductVariantHolder, position: Int) {
        holder.bind(productImageList[position])
    }

    inner class ProductVariantHolder(val view: ProductPagerItemBinding): RecyclerView.ViewHolder(view.root){
        fun bind(productImage: String){
            val url = productImage.substring(0, productImage.length - 7)
            Glide.with(context).load(url).into(view.ivProductImage)
        }
    }
}