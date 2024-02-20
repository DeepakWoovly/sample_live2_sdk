package com.live2.media.client.network

import com.live2.media.client.model.PostModel
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ClientService {

    @POST("api/live2-public")
    suspend fun authorize(
        @Query("authToken") authToken: String
    ): Response<Any>


    @GET("api/live2-public/videos")
    suspend fun fetchFirstSetOfVideos(
       @Query("embedId") embedId: String
    ): Response<PostModel.Model>

    @POST("api/live2-public/campaigns/user-response")
    suspend fun submitCampaignResponse(
        @Body body: RequestBody
    ): Response<Any>
}