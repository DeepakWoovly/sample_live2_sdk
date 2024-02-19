package com.live2.media.utils

import android.os.CountDownTimer

/**
 * Created with ❤ by Deepak Kumawat on 11 December 2023
 */
class PauseableCountDownTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    private val finishCallback: () -> Unit
) :
    CountDownTimer(millisInFuture, countDownInterval) {

    var isTimerRunning: Boolean = false

    override fun onTick(millisUntilFinished: Long) {
    }

    override fun onFinish() {
        finishCallback()
    }

    fun startTimer() {
        isTimerRunning = true
        start()
    }

    fun pauseTimer() {
        cancel()
    }

    fun resumeTimer() {
        start()
    }

    fun destroyTimer() {
        isTimerRunning = false
        cancel()
    }
}
