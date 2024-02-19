package com.example.videosdk.feature

import com.example.videosdk.network.ApiResult
import com.example.videosdk.network.model.PostModel

interface Repository {
    suspend fun fetchFirstSetOfVideosForCarousel(): ApiResult<PostModel.Model>
    suspend fun fetchFirstSetOfVideosForGrid(): ApiResult<PostModel.Model>
    suspend fun fetchFirstSetOfVideosForStory(): ApiResult<PostModel.Model>
    suspend fun fetchFirstSetOfVideosForStoryWindow(): ApiResult<PostModel.Model>
    suspend fun fetchFirstSetOfVideosForPiP(): ApiResult<PostModel.Model>

    suspend fun submitMCQResponse(
        publicCampaignId: String,
        optionIds: List<String>,
        optionTextList: List<String>
    ): ApiResult<Any>

    suspend fun submitPollResponse(
        publicCampaignId: String,
        optionId: String,
        optionText: String
    ): ApiResult<Any>

    suspend fun submitQnaResponse(
        publicCampaignId: String,
        answerText: String
    ): ApiResult<Any>

}