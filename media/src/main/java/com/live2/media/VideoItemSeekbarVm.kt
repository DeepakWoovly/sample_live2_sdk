package com.live2.media

import android.widget.SeekBar
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.live2.media.core.exoplayer.PlayerHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class VideoItemSeekbarVm(private val playerHelper: PlayerHelper) {

    val seekBarProgress = ObservableInt(0)
    val videoProgress = ObservableField("")
    val isVideoLayoutVisible = ObservableBoolean(true)
    var disposable: Job? = null

    var userSwipeProgressStart: Int = 0

    @OptIn(DelicateCoroutinesApi::class)
    fun initSeekbar(setup: () -> Unit) {
        disposable = flow {
            while (true) {
                delay(100)
                val currentPosition = withContext(Dispatchers.Main){
                    playerHelper.player?.currentPosition ?: -1
                }
                if (currentPosition != -1L) {
                    emit(currentPosition)
                }
            }
        }.distinctUntilChanged().onEach {
                withContext(Dispatchers.Main) {
                    calculateVideoProgress(it, setup)
                }
            }.launchIn(GlobalScope)
    }


    private fun calculateVideoProgress(currentMill: Long, setup: () -> Unit) {
        val player = playerHelper.player ?: return
        val totalMillis = player.duration
        if (totalMillis > 0L) {
            val remainMillis = (totalMillis - currentMill).toFloat() / 1000
            val remainMin = (remainMillis / 60).toInt()
            val remainSecs = (remainMillis % 60).toInt()
            if (remainSecs < 10) {
                videoProgress.set("$remainMin:0$remainSecs")
            } else {
                videoProgress.set("$remainMin:$remainSecs")
            }
            seekBarProgress.set((currentMill.toFloat() / totalMillis * 100).toInt())
            setup()
        }
    }

    fun stopSeekbar() {
        videoProgress.set("")
        seekBarProgress.set(0)
        disposable?.cancel()
        disposable = null
    }
}

fun PlayerHelper.attachSeekbar(
    progress: SeekBar
) {

    progress.setOnSeekBarChangeListener(object :
        SeekBar.OnSeekBarChangeListener {

        var userSwipeProgressStart: Int = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val player = player ?: return
                val seekMillis = player.duration.toFloat() * progress / 100
                player.seekTo(seekMillis.toLong())
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            userSwipeProgressStart = seekBar?.progress ?: 0
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            val player = player ?: return
            val seekMillis = player.duration.toFloat() * (seekBar?.progress ?: 0) / 100
        }
    })
}
