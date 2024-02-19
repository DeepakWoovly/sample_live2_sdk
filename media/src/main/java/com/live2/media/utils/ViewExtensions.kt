package com.example.videosdk.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.io.File

private const val TAG = "ViewExtensions"
fun View.getDrawableCompat(@DrawableRes resId: Int): Drawable? {
    return try {
        AppCompatResources.getDrawable(context, resId)
    } catch (exception1: Resources.NotFoundException) {
        exception1.message?.let { Log.d(TAG, it) }
        try {
            ContextCompat.getDrawable(context, resId)
        } catch (exception2: Resources.NotFoundException) {
            exception2.message?.let { Log.d(TAG, it) }
            VectorDrawableCompat.create(context.resources, resId, context.theme)
        }
    }
}

fun View.setBackgroundCompat(@DrawableRes resId: Int) {
    background = getDrawableCompat(resId)
}

fun View.setForegroundCompat(@DrawableRes resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        foreground = getDrawableCompat(resId)
    }
}

fun FrameLayout.setForegroundCompat(@DrawableRes resId: Int) {
    foreground = getDrawableCompat(resId)
}

fun TextView.drawableStart(@DrawableRes drawableResId: Int) {
    this.setCompoundDrawablesWithIntrinsicBounds(getDrawableCompat(drawableResId), null, null, null)
}

fun ImageView.setImageFromFilePath(filePath: String) {
    val imageFile = File(filePath)
    if (imageFile.exists()) {
        this.setImageBitmap(BitmapFactory.decodeFile(imageFile.absolutePath))
    }
}

fun ViewPager2.reduceViewpagerDragSensitivity(sensitivityFactor: Int) {
    try {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * sensitivityFactor)
    } catch (e: java.lang.Exception) {
        e.message?.let { Log.d(TAG, it) }
    }
}

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

inline fun <reified T : ViewDataBinding> ViewStubProxy.doActionOrSetupInflationListener(crossinline actionToBeDone: T.() -> Unit) {
    if (this.isInflated) {
        this.binding.performIfInstanceOf<T> {
            this.actionToBeDone()
        }
    } else {
        this.setOnInflateListener { stub, inflated ->
            val inflatedBinding = DataBindingUtil.getBinding<T>(inflated)
            inflatedBinding?.actionToBeDone()
        }
    }
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.forcePortraitOrientation() {
    if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

fun Activity.forceLandscapeOrientation() {
    if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

inline fun <reified T : Activity> Fragment.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), T::class.java).apply(block))
}

inline fun <reified T> Any?.performIfInstanceOf(performAction: T.() -> Unit) {
    if (this is T) {
        this.performAction()
    }
}


fun View.setPaddingHorizontal(padding: Int) {
    setPadding(padding, paddingTop, padding, paddingBottom)
}

fun View.setPaddingVertical(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}