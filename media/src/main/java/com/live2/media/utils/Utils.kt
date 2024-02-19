package com.example.videosdk.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat
import com.example.videosdk.R
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class Utils {
    companion object{
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun dpToPx(dp: Float): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        val screenWidthHeightRatio: Float by lazy {
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            screenWidth.toFloat() / screenHeight.toFloat()
        }

        fun shareVideo(
            context: Context,
            videoTitle: String,
            appUrl: String,
            webUrl: String,
            userId: String,
            isVideo: Boolean,
            downloadedFilePath: String = ""
        ) {
            var defaultShareMessage = context.getString(R.string.default_share_string)

            defaultShareMessage = replaceShareString(
                defaultShareMessage,
                videoTitle,
                webUrl,
                appUrl
            )

            var whatsAppShareText: String? = null
            var instagramShareText: String? = null
            var defaultShareText: String? = null

            whatsAppShareText = defaultShareMessage
            instagramShareText = defaultShareMessage
            defaultShareText = defaultShareMessage

            val emailIntent = Intent()
            emailIntent.action = Intent.ACTION_SEND
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, videoTitle)
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(defaultShareText).toString())
            if (isVideo) emailIntent.type = "video/*" else emailIntent.type = "text/plain"

            val pm = context.packageManager
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = if (isVideo) "video/*" else "text/plain"

            val openInChooser = Intent.createChooser(emailIntent, "Share Post")
            val resInfo = pm.queryIntentActivities(sendIntent, 0)
            val customList: MutableList<LabeledIntent?> = ArrayList()
            val priorityMap = HashMap<String, LabeledIntent?>()
            for (i in resInfo.indices) {
                val ri = resInfo[i]
                val packageName = ri.activityInfo.packageName

                if (packageName.contains("android.email")) {
                    emailIntent.setPackage(packageName)
                } else {
                    val intent = Intent()
                    intent.component = ComponentName(packageName, ri.activityInfo.name)
                    intent.action = Intent.ACTION_SEND
                    if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                    intent.type = if (isVideo) "video/*" else "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, videoTitle)

                    if (packageName.contains("whatsapp")) {
                        intent.putExtra(Intent.EXTRA_TEXT, whatsAppShareText)

                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap["whatsapp"] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    } else if (packageName.contains("instagram")) {
                        intent.putExtra(Intent.EXTRA_TEXT, instagramShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap["instagram"] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    } else if (packageName.contains("messenger")) {
                        intent.putExtra(Intent.EXTRA_TEXT, defaultShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap["messenger"] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    } else if (packageName.contains("twitter")) {
                        intent.putExtra(Intent.EXTRA_TEXT, defaultShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap["twitter"] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    } else if (packageName.contains("facebook")) {
                        copyTextToClipboard(context, defaultShareText, "Share Post")

                        // Warning: Facebook IGNORES our text. They say "These fields are intended for users
                        // to express themselves. Pre-filling these fields erodes the authenticity of the user voice.
                        // One workaround is to use the Facebook SDK to post, but that doesn't allow
                        // the user to choose how they want to share. We can also make a custom landing page, and the link
                        // will show the <meta content ="..."> text from that page with our link in Facebook.

                        intent.putExtra(Intent.EXTRA_TEXT, appUrl)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        customList.add(LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon))
                    } else if (packageName.contains("mms")) {
                        intent.putExtra(Intent.EXTRA_TEXT, defaultShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap["mms"] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    } else if (packageName.contains("com.google.android.apps.docs")) {
                        intent.putExtra(Intent.EXTRA_TEXT, defaultShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        customList.add(LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon))
                    } else {
                        intent.putExtra(Intent.EXTRA_TEXT, defaultShareText)
                        if (isVideo) intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(downloadedFilePath)))
                        intent.type = if (isVideo) "video/*" else "text/plain"
                        priorityMap[packageName] = LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon)
                    }
                }
            }
                if (priorityMap.containsKey("twitter")) {
                    customList.add(0, priorityMap["twitter"])
                    priorityMap.remove("twitter")
                }
                if (priorityMap.containsKey("facebook")) {
                    customList.add(0, priorityMap["facebook"])
                    priorityMap.remove("facebook")
                }
                if (priorityMap.containsKey("instagram")) {
                    customList.add(0, priorityMap["instagram"])
                    priorityMap.remove("instagram")
                }
            customList.addAll(priorityMap.values)
            // convert intentList to array
            val extraIntents = customList.toTypedArray()
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
            openInChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(openInChooser)
        }

        private fun replaceShareString(
            stringToReplace: String,
            title: String,
            webUrl: String,
            appUrl: String
        ): String {
            return stringToReplace.replace("{{title}}", title)
                .replace("{{webURL}}", webUrl)
                .replace("{{appURL}}", appUrl)
        }

        private fun copyTextToClipboard(context: Context, messageToShare: String?, title: String?) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(title, messageToShare)
            clipboard.setPrimaryClip(clip)
        }

        val View.keyboardIsVisible: Boolean
            @RequiresApi(Build.VERSION_CODES.M)
            get() = WindowInsetsCompat
                .toWindowInsetsCompat(rootWindowInsets)
                .isVisible(WindowInsetsCompat.Type.ime())

        fun View.hide() {
            this.visibility = View.INVISIBLE
        }

        fun View.show() {
            this.visibility = View.VISIBLE
        }

        fun View.gone() {
            this.visibility = View.GONE
        }

        fun View.invisible() {
            this.visibility = View.INVISIBLE
        }

        fun View.isVisible(): Boolean {
            return this.visibility == View.VISIBLE
        }

        fun View.disable() {
            isEnabled = false
        }

        fun View.enable() {
            isEnabled = true
        }

        fun CheckBox.check() {
            isChecked = true
        }

        fun CheckBox.uncheck() {
            isChecked = false
        }

        fun getTextFromHTML(text: String): Spanned? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(text)
            }
        }
        fun JSONObject.getJsonRequestBody(): RequestBody {
            return this.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        }

        fun getScreenHeight():Int{
            return Resources.getSystem().displayMetrics.heightPixels
        }
        fun getScreenWidth():Int{
            return Resources.getSystem().displayMetrics.widthPixels
        }

        fun View.setHeightAndWidth(width:Double?=null,height:Double?=null){
            val layoutParams = this.layoutParams
            layoutParams.width = width?.toInt()?:layoutParams.width
            layoutParams.height = height?.toInt()?:layoutParams.height
            this.layoutParams = layoutParams
        }
        fun View.setHeightAndWidth(width:Int?=null,height:Int?=null){
            val layoutParams = this.layoutParams
            layoutParams.width = width?.let { dpToPx(width) }?:layoutParams.width
            layoutParams.height = height?.let { dpToPx(height) }?:layoutParams.height
            this.layoutParams = layoutParams
        }
    }
}

