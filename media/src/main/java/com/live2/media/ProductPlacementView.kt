package com.live2.media

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.live2.media.databinding.LayoutProductWidgetBinding
import com.live2.media.client.model.PostModel
import com.live2.media.utils.Constants
import com.live2.media.utils.HorizontalMarginItemDecorations
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ProductPlacementView(
    productBinding: LayoutProductWidgetBinding,
    productsModel: PostModel.Video,
    private val productCardListener: ProductCardListener
) {
    var context: Context = productBinding.root.context
    private val disposables = CompositeDisposable()
    private val viewpager = productBinding.productPlacementViewPager
    private var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var swipeLastX = 0f
    private var swipeLastY = 0f

    init {
        setUpProducts(productsModel)
    }

    private fun cleanItemDecorator(viewpager: ViewPager2) {
        viewpager.run {
            while (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }
        }
    }

    fun unregisterPageChangeCallback() {
        onPageChangeCallback?.let {
            viewpager.unregisterOnPageChangeCallback(it)
        }
    }

    private fun setUpProducts(productsModel: PostModel.Video) {
        cleanItemDecorator(viewpager)

        viewpager.apply {
            adapter = null
            setPageTransformer(null)
            visibility = View.VISIBLE
        }
        viewpager.apply {
            val adapter = ProductPlacementViewPagerAdapter(productsModel.products, productCardListener)
            disposables.add(
                adapter.ctaClickAction
                    .throttleFirst(Constants.PRODUCT_PLACEMENT_THROTTLE, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { ProductModel ->
                        openProductButtonCta(ProductModel)
                    }
            )
            disposables.add(
                adapter.clickAction
                    .throttleFirst(Constants.PRODUCT_PLACEMENT_THROTTLE, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { ProductModel ->
                        openProduct(ProductModel)
                    }
            )
            disposables.add(
                adapter.scrollListener
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { event ->
                        swipeListener(event)
                    }
            )
            this.adapter = adapter
            with(this) {
                offscreenPageLimit = 2
                clipToPadding = false
                clipChildren = false
            }
        }
        cleanItemDecorator(viewpager)
        setUpItemDecorator()
        viewpager.visibility = View.VISIBLE
        onPageChangeCallback?.let {
            viewpager.unregisterOnPageChangeCallback(it)
        }
        this.onPageChangeCallback = onPageChangeCallback().also {
            viewpager.registerOnPageChangeCallback(it)
        }

    }

    private fun openProductButtonCta(productModel: PostModel.Product) {
        // product click
    }

    private fun onPageChangeCallback() = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //  setDiscountLabel(position)
        }
    }

    private fun getItemDecorator(): RecyclerView.ItemDecoration {
        return HorizontalMarginItemDecorations(
            context,
            R.dimen.pageMarginAndOffset
        )
    }

    private fun setUpItemDecorator() {
        viewpager.run {
            (getChildAt(0) as RecyclerView).clearOnChildAttachStateChangeListeners()
            val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
            val currentItemHorizontalMarginPx =
                resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                page.translationX = -pageTranslationX * position
            }
            viewpager.setPageTransformer(pageTransformer)
            this.addItemDecoration(getItemDecorator())
        }
    }

    private fun openProduct(productsModel: PostModel.Product) {
        //on product clicked
    }

    private fun swipeListener(event: MotionEvent) {
        viewpager.run {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val deltaX = event.x - swipeLastX
                    val deltaY = event.y - swipeLastY
                    if (abs(deltaX) > abs(deltaY)) {
                        this.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    swipeLastX = event.x
                    swipeLastY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - swipeLastX
                    val deltaY = event.y - swipeLastY
                    if (abs(deltaX) > 20 && abs(deltaX) > abs(deltaY)) {
                        this.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    swipeLastX = event.x
                    swipeLastY = event.y
                }

                MotionEvent.ACTION_UP -> {
                }
            }
            invalidate()
        }
    }
}