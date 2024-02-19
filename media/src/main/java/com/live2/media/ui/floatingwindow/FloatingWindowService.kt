package com.live2.media.ui.floatingwindow

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.live2.media.databinding.LayoutPipItemBinding
import com.live2.media.internal.model.PostModel

class FloatingWindowService: Service() {
    private var windowManager: WindowManager? = null
    private var videoList: List<PostModel.Video>? = null
//    private lateinit var playerHelper: L1PlayerHelper
    private lateinit var binding: LayoutPipItemBinding

    companion object {
        fun getIntent(context: Context, videoList: List<PostModel.Video>): Intent {
            val intent = Intent(context, FloatingWindowService::class.java)
            val arrayList = arrayListOf<PostModel.Video>()
            videoList.let { arrayList.addAll(it) }
            val extras = Bundle().apply {
                putParcelableArrayList("list", arrayList)
            }
            intent.putExtras(extras)
            return intent
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.extras
        videoList = bundle?.getParcelableArrayList<PostModel.Video>("List")?.toList()?: listOf()

        val inflater = LayoutInflater.from(this)
        binding = LayoutPipItemBinding.inflate(inflater, null, false)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.END
        windowManager?.addView(binding.root, params)
        Log.d("DEREPAK", "STARTED")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(binding.root)
    }
}