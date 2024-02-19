package com.live2.media.ui.campaigns

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import com.live2.media.R
import com.live2.media.databinding.LayoutPollDialogBinding
import com.live2.media.databinding.PollOptionViewBinding
import com.live2.media.databinding.ViewLineSeperatorBinding
import com.live2.media.internal.model.PostModel
import com.live2.media.utils.Utils.Companion.gone

class PollsDialog(
    private val context: Context,
    private val campaignModel: PostModel.Video,
    private val campaignsClickCallback: CampaignsClickCallback
) : Dialog(context), CampaignListener {

    private val inflater = LayoutInflater.from(context)
    private var binding: LayoutPollDialogBinding? = null
    private var selectedOptionText: String = ""
    private var selectedOptionId: String = ""
    private val optionsMap = mutableMapOf<String, String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutPollDialogBinding.inflate(LayoutInflater.from(context), null, false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding?.root!!)

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
        window.attributes.dimAmount = 0.0f

        binding?.ivClose?.setOnClickListener {
            dismiss()
        }
        setCanceledOnTouchOutside(true)
        setupData(campaignModel)
        binding?.submitPoll?.setOnClickListener {
            onSubmitClicked()
        }
    }

    private fun setupData(campaignModel: PostModel.Video) {
        if (campaignModel.overlay != null) {
            if (campaignModel.overlay.data.question != null && !campaignModel.overlay.data.options.isNullOrEmpty()) {
                binding?.tvQuestion?.text = campaignModel.overlay.data.question
                setupOptions(optionsList = campaignModel.overlay.data.options)
            }
        } else {
            binding?.pollsParent?.gone()
        }
    }

    override fun onSubmitClicked() {
        val optionsContainer = binding?.optionsLayout
        for (i in 0 until optionsContainer?.childCount!!){
            val child = optionsContainer.getChildAt(i)
            if (child is RadioButton){
                val option = optionsContainer.getChildAt(i) as RadioButton
                if (option.isChecked){
                    selectedOptionText = option.text.toString()
                    selectedOptionId = optionsMap[option.text.toString()]!!
                }
            }
        }
        campaignsClickCallback.onPollSubmitClicked(campaignModel.overlay?.campaignId!!, selectedOptionId, selectedOptionText)
        dismiss()
    }

    override fun setupOptions(optionsList: List<PostModel.Option>) {
        val optionsContainer = binding?.optionsLayout
        for (option in optionsList){
            val optionRadioButton = PollOptionViewBinding.inflate(inflater,optionsContainer, false)
            if (option.id != optionsList[0].id){
                val lineSeparator = ViewLineSeperatorBinding.inflate(inflater, optionsContainer, false)
                optionsContainer?.addView(lineSeparator.root)
            }
            optionRadioButton.root.text = option.value
            optionsContainer?.addView(optionRadioButton.root)
            optionsMap[option.value] = option.id
        }
    }
}