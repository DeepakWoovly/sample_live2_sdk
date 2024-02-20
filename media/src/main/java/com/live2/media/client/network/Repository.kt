package com.live2.media.client.network

import com.live2.media.client.model.PostModel

interface Repository {
    suspend fun fetchFirstSetOfVideos(embedId: String): ApiResult<PostModel.Model>

    suspend fun authorize(
        token: String
    ): ApiResult<Any>

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