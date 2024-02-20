package com.live2.media

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live2.media.databinding.LayoutProductItemBinding
import com.live2.media.client.model.PostModel
import com.live2.media.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ProductPlacementViewPagerAdapter(val productsModel: List<PostModel.Product>, val productCardListener: ProductCardListener) : RecyclerView.Adapter<ProductPlacementViewPagerAdapter.ProductPlacementVH>() {

    private val _clickAction = PublishSubject.create<PostModel.Product>()
    private val _ctaClickAction = PublishSubject.create<PostModel.Product>()
    val scrollListener = PublishSubject.create<MotionEvent>()
    val clickAction: Observable<PostModel.Product> = _clickAction.hide()
    val ctaClickAction: Observable<PostModel.Product> = _ctaClickAction.hide()

    @SuppressLint("ClickableViewAccessibility")

    inner class ProductPlacementVH(binding: LayoutProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val productBinding = binding
        private val productIcon: ImageView = binding.productIcon
        private val productDesc: TextView = binding.productDesc
        private val amountTextView: TextView = binding.amountTxt
        private val mrpTextView: TextView = binding.mrpTxt
        private val percentOffValue: TextView = binding.tvPercentOff
        private val discountView: View = binding.percentOffBgInner
        private val discountBg: View = binding.percentOffBg

        init {
            itemView.setOnTouchListener { _, event ->
                scrollListener.onNext(event)
                false
            }

            binding.root.setOnClickListener {
                productCardListener.onProductCardClicked(productsModel[adapterPosition])
            }

            binding.productBtn.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                productCardListener.onOnCartClicked(productsModel[adapterPosition])
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(data: PostModel.Product, firstItem: Boolean, singleProduct: Boolean) {
            var url = ""
            if (data.product_image != null){
                 url = data.product_image.substring(0, data.product_image.length - 7)
            }
            Glide.with(itemView.context).load(url).into(productIcon)
            productIcon.clipToOutline = true
            productDesc.text = data.product_name

            amountTextView.text = "Rs. ${data.product_discounted_price/100}"
            mrpTextView.text = "Rs. ${data.product_price/100}"
            mrpTextView.paintFlags = mrpTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            productBinding.productBtn.text = "Add to cart"
            if (firstItem) (productBinding.productCardView.layoutParams as ViewGroup.MarginLayoutParams)
                .marginStart = Utils.dpToPx(28f)

            val hasDiscount = data.product_discount != ""

            if (hasDiscount) {
                productBinding.mrpTxt.visibility = View.VISIBLE
                percentOffValue.apply {
                    text = "${data.product_discount}%"
                }
            } else {
                discountView.visibility = View.GONE
                discountBg.visibility = View.GONE
            }
            productBinding.productCardView.post {
                if (singleProduct) productBinding.productCardView.layoutParams.width=
                    Resources.getSystem().displayMetrics.widthPixels - Utils.dpToPx(59f)
                 itemView.invalidate()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPlacementVH {
        return ProductPlacementVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_product_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productsModel.size
    }

    override fun onBindViewHolder(holder: ProductPlacementVH, position: Int) {
        holder.setData(productsModel[position], position == 0, productsModel.size == 1)
    }

}
