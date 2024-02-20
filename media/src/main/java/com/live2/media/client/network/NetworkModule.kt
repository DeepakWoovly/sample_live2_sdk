package com.live2.media.client.network

import com.live2.media.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://beta.live2.ai/"
    private fun okHttpClient(): OkHttpClient {
        val timeOutInSeconds = 120
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeOutInSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeOutInSeconds.toLong(), TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }
        return builder.build()
    }

    private fun retrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideClientApiService(): ClientService = retrofit().create(ClientService::class.java)
}