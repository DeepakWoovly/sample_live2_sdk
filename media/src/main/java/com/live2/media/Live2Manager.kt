package com.live2.media

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.live2.media.client.network.ViewState
import com.live2.media.client.Live2ViewModel

internal class Live2Manager {

    private lateinit var application: Application
    private lateinit var live2ViewModel: Live2ViewModel
    private var context: LifecycleOwner? = null
    fun setup(
        application: Application,
        authToken: String,
        context: LifecycleOwner
    ) {
        this.application = application
        this.context = context
        live2ViewModel = Live2ViewModel()
        if (live2ViewModel.isInitialized()){
            //live2ViewModel.authorize(token = authToken)
        }
        observe()
    }

    private fun observe(){
        live2ViewModel.authLiveData.observe(context!!) {state ->
            when(state){
                is ViewState.Error -> {}
                ViewState.Loading -> {}
                is ViewState.Success -> {}
            }
        }
    }

    // need to remove this
    fun getLive2Videos(embedId: String){
        if (live2ViewModel.isInitialized()){
            live2ViewModel.fetchFirstSetOfData(embedId)
        }
    }


}