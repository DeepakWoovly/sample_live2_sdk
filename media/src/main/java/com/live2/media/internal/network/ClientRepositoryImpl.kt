package com.example.videosdk.feature

import android.health.connect.datatypes.BloodPressureRecord.BodyPosition
import com.example.videosdk.network.ApiResult
import com.example.videosdk.network.getResult
import com.example.videosdk.network.model.PostModel
import com.example.videosdk.network.service.ClientService
import com.example.videosdk.util.Utils.Companion.getJsonRequestBody
import org.json.JSONObject

class ClientRepositoryImpl(
    private val clientApiService: ClientService,
): Repository {

    override suspend fun fetchFirstSetOfVideosForCarousel(): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideosForCarousel()
        }
        return result
    }

    override suspend fun fetchFirstSetOfVideosForGrid(): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideosForGrid()
        }
        return result
    }

    override suspend fun fetchFirstSetOfVideosForStory(): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideosForStory()
        }
        return result
    }

    override suspend fun fetchFirstSetOfVideosForStoryWindow(): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideosForStoryWindow()
        }
        return result
    }

    override suspend fun fetchFirstSetOfVideosForPiP(): ApiResult<PostModel.Model> {
        val result = getResult {
            clientApiService.fetchFirstSetOfVideosForPiP()
        }
        return result
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
        val result = getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }
        return result
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
        val result = getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }
        return result
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
        val result = getResult {
            clientApiService.submitCampaignResponse(body = json.getJsonRequestBody())
        }
        return result
    }

}