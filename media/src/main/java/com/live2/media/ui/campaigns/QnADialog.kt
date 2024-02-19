package com.example.videosdk.feature.campaigns

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.example.videosdk.R
import com.example.videosdk.VideoItemVM
import com.example.videosdk.databinding.LayoutDialogBinding
import com.example.videosdk.databinding.LayoutQnaDialogBinding
import com.example.videosdk.databinding.VideoLayoutBinding
import com.example.videosdk.network.model.PostModel
import com.example.videosdk.util.Utils.Companion.keyboardIsVisible

class QnADialog(
    context: Context,
    private val campaignModel: PostModel.Video,
    private val campaignsClickCallback: CampaignsClickCallback
) : Dialog(context) {


    private var binding: LayoutQnaDialogBinding? = null
    private var textCount = 0
    private val textLimit = 160

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutQnaDialogBinding.inflate(LayoutInflater.from(context), null, false)
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

        binding?.ivClose?.setOnClickListener {
            dismiss()
        }
        setCanceledOnTouchOutside(true)
        setupData(campaignModel)
        initOnClickListeners(binding!!)
    }

    private fun initOnClickListeners(binding: LayoutQnaDialogBinding) {
        with(binding) {
            submitQna.setOnClickListener {
                campaignModel.overlay?.campaignId?.let { campaignId ->
                    campaignsClickCallback.onQuestionSubmitClicked(
                        campaignId,
                        etInputField.text.toString()
                    )
                }
                dismiss()
            }
        }
    }

    private fun setupData(campaignModel: PostModel.Video) {
        if (campaignModel.overlay != null) {
            if (campaignModel.overlay.data.question != null && campaignModel.overlay.data.options.isNullOrEmpty()) {
                val questionData = campaignModel.overlay.data.question
                val questionView = binding?.tvQuestion
                val etInputField = binding?.etInputField
                questionView?.text = questionData

                etInputField?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    @SuppressLint("SetTextI18n")
                    override fun afterTextChanged(s: Editable?) {
                        textCount = s?.length!!
                        binding?.tvTextLimit?.text = "$textCount/$textLimit"
                        if (textCount == 0) {
                            binding?.submitQna?.setBackgroundResource(R.drawable.btn_bg_white1)
                            binding?.submitQna?.isEnabled = false
                        } else {
                            binding?.submitQna?.setBackgroundResource(R.drawable.btn_bg_white)
                            binding?.submitQna?.isEnabled = true
                        }
                    }
                })

            }
        } else {
            binding?.qnaParent?.visibility = View.GONE
        }
    }
}