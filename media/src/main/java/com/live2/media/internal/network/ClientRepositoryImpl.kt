package com.live2.media.internal.network

import android.util.Log
import com.live2.media.internal.model.PostModel
import com.live2.media.utils.Utils.Companion.getJsonRequestBody
import org.json.JSONObject

class ClientRepositoryImpl(
    private val clientApiService: ClientService,
) : Repository {


    override suspend fun fetchFirstSetOfVideos(embedId: String): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideos(embedId)
        }
        return result
    }

    override suspend fun authorize(token: String): ApiResult<Any> {
        return getResult {
            clientApiService.authorize(authToken = token)
        }
    }

    override suspend fun submitMCQResponse(
        publicCampaignId: String,
        optionIds: List<String>,
        optionTextList: List<String>
    ): ApiResult<Any> {
        val json = JSONObject()
        val overlayResponse = JSONObject()
        json.put("overlayType", "MCQ")
        json.put("publicCampaignId", publicCampaignId)
        overlayResponse.put("optionIds", optionIds)
        overlayResponse.put("optionTextList", optionTextList)
        json.put("overlayResponse", overlayResponse)
        return getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }
    }

    override suspend fun submitPollResponse(
        publicCampaignId: String,
        optionId: String,
        optionText: String
    ): ApiResult<Any> {
        val json = JSONObject()
        val overlayResponse = JSONObject()
        json.put("overlayType", "POLL")
        json.put("publicCampaignId", publicCampaignId)
        overlayResponse.put("optionId", optionId)
        overlayResponse.put("optionText", optionText)
        json.put("overlayResponse", overlayResponse)
        return getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }

    }

    override suspend fun submitQnaResponse(
        publicCampaignId: String,
        answerText: String
    ): ApiResult<Any> {
        val json = JSONObject()
        val overlayResponse = JSONObject()
        json.put("overlayType", "QUESTION")
        json.put("publicCampaignId", publicCampaignId)
        json.put("overlayResponse", overlayResponse.put("answerText", answerText))
       return getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }
    }

}