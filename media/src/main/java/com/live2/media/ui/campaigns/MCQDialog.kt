package com.example.videosdk.feature.campaigns

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import com.example.videosdk.R
import com.example.videosdk.VideoItemVM
import com.example.videosdk.databinding.LayoutDialogBinding
import com.example.videosdk.databinding.McqOptionViewBinding
import com.example.videosdk.databinding.ViewLineSeperatorBinding
import com.example.videosdk.network.model.PostModel
import com.example.videosdk.util.Utils.Companion.gone

class MCQDialog(
    private val context: Context,
    private val campaignModel: PostModel.Video,
    private val campaignsClickCallback: CampaignsClickCallback
) : Dialog(context), CampaignListener {

    private val inflater = LayoutInflater.from(context)
    private lateinit var binding: LayoutDialogBinding
    private val selectedOptionsTextList = mutableListOf<String>()
    private val selectedOptionsIdsList = mutableListOf<String>()
    private val optionsMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDialogBinding.inflate(LayoutInflater.from(context), null, false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val window: Window = window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawableResource(R.color.transparent)
        window.attributes.apply {
            dimAmount = 0.0f
            windowAnimations = R.style.DialogAnimation
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }
        setCanceledOnTouchOutside(true)
        setupData(campaignModel)
    }

    @SuppressLint("InflateParams")
    private fun setupData(campaignModel: PostModel.Video) {
        if (campaignModel.overlay != null) {
            if (campaignModel.overlay.data.question != null && !campaignModel.overlay.data.options.isNullOrEmpty()) {
                binding.tvQuestion.text = campaignModel.overlay.data.question
                setupOptions(campaignModel.overlay.data.options)
                binding.submitMCQ.setOnClickListener {
                    onSubmitClicked()
                }
            }
        } else {
            binding.mcqParent.gone()
        }
    }

    override fun onSubmitClicked() {
        val optionsContainer = binding.optionsLayout
        for (i in 0 until optionsContainer.childCount){
            val child = optionsContainer.getChildAt(i)
            if (child  is CheckBox){
                val option = optionsContainer.getChildAt(i) as CheckBox
                if (option.isChecked){
                    selectedOptionsTextList.add(option.text.toString())
                    selectedOptionsIdsList.add(optionsMap[option.text.toString()]!!)
                }
            }
        }
        campaignsClickCallback.onMCQSubmitClicked(campaignModel.overlay?.campaignId!!, selectedOptionsIdsList, selectedOptionsTextList)
        dismiss()
    }

     override fun setupOptions(optionsList: List<PostModel.Option>){
        val optionsContainer = binding.optionsLayout
        for (option in optionsList){
            val optionCheckbox = McqOptionViewBinding.inflate(inflater,optionsContainer, false)
            if (option.id != optionsList[0].id){
                val lineSeparator = ViewLineSeperatorBinding.inflate(inflater, optionsContainer, false)
                optionsContainer.addView(lineSeparator.root)
            }
            optionCheckbox.root.text = option.value
            optionsContainer.addView(optionCheckbox.root)
            optionsMap[option.value] = option.id
        }
    }
}