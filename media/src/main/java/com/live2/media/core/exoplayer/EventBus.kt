package com.example.videosdk.core

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private const val TAG = "EventBus"
    private val _events = MutableSharedFlow<Any>()
    val events = _events.asSharedFlow()

    suspend fun emitEvent(event: Any) {
         Log.d(TAG, "Emitting event = $event")
        _events.emit(event)
    }
}
