package com.live2.media

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner

class Live2SDK  private constructor() {

    private lateinit var live2Manager: Live2Manager

    companion object {
        @JvmStatic
        fun installer(application: Application, context: LifecycleOwner): Builder {
            return Builder(application, context)
        }
    }

    class Builder(private val application: Application, private val context: LifecycleOwner) {

        private var lock = Any()
        private lateinit var live2Instance: Live2SDK
        private lateinit var authToken: String
        fun authToken(token: String): Builder{
            this.authToken = token
            return this
        }

        fun install(): Live2SDK {
            synchronized(lock) {
                if (!::live2Instance.isInitialized) {
                    live2Instance = Live2SDK().also {
                        it.live2Manager = Live2Manager().also { live2Manager ->
                            live2Manager.setup(application, authToken, context )
                        }
                    }
                } else {
                    Log.e(
                        Live2Manager::class.simpleName,
                        "Live2SDK is already installed. No need to setup Live2SDK again."
                    )
                }
            }
            return live2Instance
        }
    }
}
