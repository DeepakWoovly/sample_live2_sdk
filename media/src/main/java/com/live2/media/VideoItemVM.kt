package com.live2.media

import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import com.live2.media.ui.campaigns.MCQDialog
import com.live2.media.ui.campaigns.PollsDialog
import com.live2.media.ui.campaigns.QnADialog
import com.live2.media.core.exoplayer.PlayerHelper
import com.live2.media.databinding.ItemVideoBinding
import com.live2.media.client.model.PostModel
import com.live2.media.ui.campaigns.CampaignsClickCallback
import com.live2.media.utils.Utils.Companion.gone
import com.live2.media.utils.Utils.Companion.show
import kotlin.time.Duration.Companion.seconds

class VideoItemVM(
    item: PostModel.Video,
    position: Int,
    playerHelper: PlayerHelper,
    private val campaignsClickCallback: CampaignsClickCallback
): VideoCommonItemVm(item, playerHelper) {
    var videoPosition = position
    lateinit var binding: ItemVideoBinding
    var startTime: Long = 0
    private var timer: CountDownTimer? = null
    private var timerLimit = 5.seconds
    private val timerInterval = 1.seconds
    val isCampaignCTAEnabled = ObservableBoolean(false)
    val isTvCampaignsVisible = ObservableBoolean(false)
    val isSeekBarEnabled = ObservableBoolean(true)
    var isPaused = ObservableBoolean(false)
    var rightClickCount = 0
    var leftClickCount = 0
    var campaignType = PostModel.Campaigns.NO_CAMPAIGN
    val tenSecHandler = Handler(Looper.getMainLooper())




     private fun showView(view: View) {
        view.visibility = View.VISIBLE
        val slideUpAnimation = AnimationUtils.loadAnimation(binding.root.context, R.anim.slide_up)
        view.startAnimation(slideUpAnimation)
    }

     private fun hideView(view: View) {
        view.visibility = View.GONE
        val slideDownAnimation = AnimationUtils.loadAnimation(binding.root.context, R.anim.slide_down)
        view.startAnimation(slideDownAnimation)
    }

    private fun performDialogOpenAnimations(){
        binding.videoLayout.tvCampaigns.gone()
        if (item.products.isNotEmpty()){
            hideView(binding.videoLayout.productLayout.root)
            showView(binding.videoLayout.productLayout1)
        }
    }

    private fun performDialogCloseAnimations(){
        binding.videoLayout.tvCampaigns.show()
        if (item.products.isNotEmpty()){
            hideView(binding.videoLayout.productLayout1)
            showView(binding.videoLayout.productLayout.root)
        }
    }

    fun openMCQDialog(){
        val customDialog = MCQDialog(binding.root.context, item, campaignsClickCallback)
        performDialogOpenAnimations()
        customDialog.show()
        customDialog.setOnDismissListener {
            performDialogCloseAnimations()
        }
    }

    fun openQnaDialog(){
        val customDialog = QnADialog(binding.root.context, item, campaignsClickCallback)
        performDialogOpenAnimations()
        customDialog.show()
        customDialog.setOnDismissListener {
            performDialogCloseAnimations()
        }
    }

    fun openPollsDialog(){
        val customDialog = PollsDialog(binding.root.context, item, campaignsClickCallback)
        performDialogOpenAnimations()
        customDialog.show()
        customDialog.setOnDismissListener {
            performDialogCloseAnimations()
        }
    }


    companion object{
        @JvmStatic
        @BindingAdapter(
            value = ["android:expandableText", "android:isTextExpanded"],
            requireAll = false
        )
        fun setExpandableText(
            tv: TextView,
            title: String?,
            isExpanded: Boolean
        ) {
            try {
                tv.visibility = View.INVISIBLE
                tv.text = title
                tv.viewTreeObserver.addOnPreDrawListener(object :
                    ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        tv.viewTreeObserver.removeOnPreDrawListener(this)
                        val appendedStringBuilder = SpannableStringBuilder()
                        val stringBuilder = SpannableStringBuilder()
                        val layout = tv.layout
                        var newTitle =
                            try {
                                val lineCount = layout?.lineCount ?: 0
                                if (lineCount < 2) title
                                else {
                                    if (isExpanded) {
                                        appendedStringBuilder.append("...less")
                                        val doubleLine = title?.substring(
                                            layout.getLineStart(0), layout.getLineEnd(1)
                                        )
                                        doubleLine
                                    } else {
                                        appendedStringBuilder.append("...more")
                                        val firstLine = title?.substring(
                                            layout.getLineStart(0), layout.getLineEnd(0)
                                        )
                                        firstLine
                                    }
                                }
                            } catch (e: StringIndexOutOfBoundsException) {
                                title
                            } ?: ""

                        val textSize = 14
                        // For "...more" and "...less" 14 sp text size is required
                        appendedStringBuilder.setSpan(
                            AbsoluteSizeSpan(textSize), 0, appendedStringBuilder.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val textColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //getResources().getColor(R.color.discover_separator_gray_color, null)
                        } else {
                            //getResources().getColor(R.color.discover_separator_gray_color)
                        }
                        appendedStringBuilder.setSpan(
                            AbsoluteSizeSpan(textSize),
                            0, appendedStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        appendedStringBuilder.setSpan(
                            ForegroundColorSpan(textSize),
                            0, appendedStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        var textNotAdjusted = true
                        while (textNotAdjusted) {
                            stringBuilder.append(appendedStringBuilder)
                            stringBuilder.insert(0, newTitle ?: "")
                            tv.text = stringBuilder
                            val lineCount = tv.layout?.lineCount ?: 0
                            val checkWithExpanded = isExpanded && lineCount > 2
                            val checkWithoutExpanded = !isExpanded && lineCount > 1
                            if (checkWithExpanded || checkWithoutExpanded) {
                                newTitle =
                                    newTitle.dropLast(1) // Keep dropping char from last till condition is met.
                                stringBuilder.clear()
                                stringBuilder.clearSpans()
                            } else {
                                tv.visibility = View.VISIBLE
                                textNotAdjusted = false
                            }
                        }
                        return true
                    }
                })
            } catch (ignore: Exception) {

            }
        }
    }

}