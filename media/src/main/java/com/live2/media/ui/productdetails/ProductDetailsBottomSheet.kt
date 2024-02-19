package com.live2.media.ui.productdetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.live2.media.utils.DotsIndicatorDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.live2.media.R
import com.live2.media.databinding.LayoutProductAttributeBinding
import com.live2.media.databinding.LayoutProductDetailsBottomsheetBinding
import com.live2.media.internal.model.PostModel
import com.live2.media.utils.Utils.Companion.getTextFromHTML
import com.live2.media.utils.Utils.Companion.gone


class ProductDetailsBottomSheet(private val productModel: PostModel.ProductDetails) :
    BottomSheetDialogFragment() {

    private lateinit var binding: LayoutProductDetailsBottomsheetBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog


    companion object {
        const val TAG = "ProductDetailsBottomSheet"
        fun newInstance(productModel: PostModel.ProductDetails) =
            ProductDetailsBottomSheet(productModel)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ProductDetailsBottomSheetStyle)
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                setPeekHeight(700, true)
            }
        }
        bottomSheetDialog.dismissWithAnimation = true
        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutProductDetailsBottomsheetBinding.inflate(inflater, container, false)
        initViews(binding, productModel)
        initOnClickListeners(binding)
        initObservers(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.show()
    }

    private fun initObservers(binding: LayoutProductDetailsBottomsheetBinding) {
    }

    private fun initOnClickListeners(binding: LayoutProductDetailsBottomsheetBinding) {
        binding.apply {
            ivClose.setOnClickListener {
                dismissBottomSheet()
            }

            tvBack.setOnClickListener {
                dismissBottomSheet()
            }
        }
    }

    private fun initViews(
        binding: LayoutProductDetailsBottomsheetBinding,
        productModel: PostModel.ProductDetails
    ) {
        initProductImagesRv(binding, productModel)
        initProductDetails(binding, productModel)
        initProductAttributes(binding, productModel)
    }

    @SuppressLint("SetTextI18n")
    private fun initProductDetails(
        binding: LayoutProductDetailsBottomsheetBinding,
        productModel: PostModel.ProductDetails
    ) {
        with(binding) {
            productModel.apply {
                tvProductTitle.text = product_name
                tvProductPrice.text = "\u20B9" + (product_discounted_price/100).toString()
                tvDescription.text = getTextFromHTML(product_description)
                if (product_discount.isEmpty() || product_discount == "0") {
                    verticalLineSeparator1.gone()
                    tvProductDiscount.gone()
                } else {
                    tvProductDiscount.text = product_discount
                    tvProductPriceOriginal.text = product_price.toString()
                }
            }
        }
    }

    private fun initProductAttributes(
        binding: LayoutProductDetailsBottomsheetBinding,
        productModel: PostModel.ProductDetails
    ){
        val inflater = LayoutInflater.from(requireContext())
        with(binding){
            productModel.apply {
                if (attributeSet.isNotEmpty()){
                    if (attributeSet.size == 1){
                        binding.lineSeparator1.gone()
                        return
                    }
                    for (attribute in attributeSet){
                        val attributeLayout = LayoutProductAttributeBinding.inflate(inflater, llProductAttributesParent, false)
                        attributeLayout.apply {
                            root.orientation = LinearLayout.HORIZONTAL
                            attributeNameTextView.text = attribute.displayName
                            val optionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, attribute.allowedValues)
                            optionsSpinner.adapter = optionAdapter
                            llProductAttributesParent.addView(attributeLayout.root)
                        }
                    }
                }
            }
        }
    }

    private fun initProductImagesRv(
        binding: LayoutProductDetailsBottomsheetBinding,
        productModel: PostModel.ProductDetails
    ) {
        with(binding.vpProductVariant) {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ProductVariantPagerAdapter(context, productModel.product_images)
            addItemDecoration(
                DotsIndicatorDecoration(
                    colorInactive = ContextCompat.getColor(context, R.color.charcoal_grey),
                    colorActive = ContextCompat.getColor(context, R.color.charcoal_grey)
                )
            )
        }
    }

    private fun dismissBottomSheet() {
        bottomSheetDialog.dismiss()
    }
}