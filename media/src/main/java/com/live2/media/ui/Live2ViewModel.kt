package com.live2.media.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.live2.media.internal.model.PostModel
import com.live2.media.internal.network.ApiResult
import com.live2.media.internal.network.ClientRepositoryImpl
import com.live2.media.internal.network.NetworkModule
import com.live2.media.internal.network.Repository
import com.live2.media.internal.network.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class Live2ViewModel : ViewModel() {

    private var isInitialized = false

    init {
        isInitialized = true
    }

    private var repository: Repository = ClientRepositoryImpl(NetworkModule.provideClientApiService())

    private val _siteSectionsLiveData = MutableLiveData<ViewState<PostModel.Data>>()
    private val _videoInteractionsLiveData = MutableLiveData<ViewState<Boolean>>()
    private val _authLiveData = MutableLiveData<ViewState<Any>>()
    val siteSectionsLiveData: LiveData<ViewState<PostModel.Data>> get() = _siteSectionsLiveData
    val videoInteractionsLiveData: LiveData<ViewState<Boolean>> get() = _videoInteractionsLiveData
    val authLiveData: LiveData<ViewState<Any>> get() = _authLiveData


    fun authorize(token: String){
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = repository.authorize(token)){
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main){
                        _authLiveData.value = ViewState.Error(result.message)
                    }
                }
                is ApiResult.Success -> {
                    withContext(Dispatchers.Main){
                        _authLiveData.value = ViewState.Success(result.data)
                    }
                }
            }
        }

    }

    fun fetchFirstSetOfVideos(embedId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.fetchFirstSetOfVideos(embedId = embedId)) {
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main) {
                        _siteSectionsLiveData.value = ViewState.Error(result.message)
                    }
                }

                is ApiResult.Success -> {
                    withContext(Dispatchers.Main) {
                        val videoData = result.data
                        _siteSectionsLiveData.value = ViewState.Success(videoData.data)
                    }
                }
            }
        }
    }

    fun submitMcqResponse(
        publicCampaignId: String,
        optionIds: List<String>,
        optionTextList: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.submitMCQResponse(
                publicCampaignId = publicCampaignId,
                optionIds = optionIds,
                optionTextList = optionTextList
            )){
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Error(result.message)
                    }
                }
                is ApiResult.Success -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Success(true)
                    }
                }
            }
        }
    }

    fun submitPollResponse(
        publicCampaignId: String,
        optionId: String,
        optionText: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.submitPollResponse(
                publicCampaignId = publicCampaignId,
                optionId = optionId,
                optionText = optionText
            )){
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Error(result.message)
                    }
                }
                is ApiResult.Success -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Success(true)
                    }
                }
            }
        }
    }

    fun submitQuestionResponse(
        publicCampaignId: String,
        answerText: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.submitQnaResponse(
                publicCampaignId = publicCampaignId,
                answerText = answerText
            )){
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Error(result.message)
                    }
                }
                is ApiResult.Success -> {
                    withContext(Dispatchers.Main){
                        _videoInteractionsLiveData.value = ViewState.Success(true)
                    }
                }
            }
        }
    }

    fun isInitialized():Boolean = isInitialized

}