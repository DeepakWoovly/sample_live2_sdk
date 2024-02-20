package com.live2.media.ui.productdetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.live2.media.R
import com.live2.media.databinding.LayoutProductsBottomsheetBinding
import com.live2.media.client.model.PostModel

class ProductsBottomSheet(
    private val productList: List<PostModel.Product>
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutProductsBottomsheetBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog

    companion object {
        const val TAG = "ProductsBottomSheet"
        fun newInstance(productList: List<PostModel.Product>) =
            ProductsBottomSheet(productList)
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
        binding = LayoutProductsBottomsheetBinding.inflate(inflater, container, false)
        initViews(binding)
//        initOnClickListeners(binding)
//        initObservers(binding)
        return binding.root
    }

    private fun initViews(binding: LayoutProductsBottomsheetBinding) {
        initProductsRv(binding)
    }

    @SuppressLint("SetTextI18n")
    private fun initProductsRv(binding: LayoutProductsBottomsheetBinding){
        with(binding){
            rvProducts.apply {
                adapter = ProductsBottomSheetAdapter(productList)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            if (productList.size == 1){
                tvTotalProducts.text = "1 Product"
            }else{
                tvTotalProducts.text = productList.size.toString() + "Products"
            }
        }
    }


}