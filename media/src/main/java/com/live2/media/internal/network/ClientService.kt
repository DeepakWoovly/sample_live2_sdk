package com.live2.media.internal.network

import com.example.videosdk.network.ApiResult
import com.example.videosdk.network.model.PostModel
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface ClientService {

    @GET("api/live2-public/videos?embedId=zvk3mycfhd")
    suspend fun fetchFirstSetOfVideosForCarousel(): Response<PostModel.Model>

    @GET("api/live2-public/videos?embedId=3bfjbg3xvc")
    suspend fun fetchFirstSetOfVideosForGrid(): Response<PostModel.Model>
    @GET("api/live2-public/videos?embedId=dpiihkq7oi")
    suspend fun fetchFirstSetOfVideosForStory(): Response<PostModel.Model>
    @GET("api/live2-public/videos?embedId=3xl9ic3lvl")
    suspend fun fetchFirstSetOfVideosForStoryWindow(): Response<PostModel.Model>
    @GET("api/live2-public/videos?embedId=2lwnxb0gqg")
    suspend fun fetchFirstSetOfVideosForPiP(): Response<PostModel.Model>

    @POST("api/live2-public/campaigns/user-response")
    suspend fun submitCampaignResponse(
        @Body body: RequestBody
    ): Response<Any>
}