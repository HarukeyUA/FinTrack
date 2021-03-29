package com.harukeyua.fintrack.api

import com.harukeyua.fintrack.data.model.api.ClientInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface MonobankService {

    @GET("/personal/client-info")
    suspend fun getAccountInfo(@Header("X-Token") key: String): ClientInfo

    companion object {
        private const val BASE_URL = "https://api.monobank.ua"

        fun create(): MonobankService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(MonobankService::class.java)
        }
    }
}