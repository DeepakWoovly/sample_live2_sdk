package com.example.videosdk.feature.productdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.videosdk.databinding.LayoutAttributeItemBinding

class SpinnerAdapter(
    private val context: Context,
    resource: Int,
    attributes: List<String>
): ArrayAdapter<String>(context,resource, attributes) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    private fun getCustomView(position: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val customView = LayoutAttributeItemBinding.inflate(inflater, parent, false)
        customView.tvSpinnerItem.text = getItem(position)
        return customView.root
    }
}