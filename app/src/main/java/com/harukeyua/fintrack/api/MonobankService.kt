/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.api

import com.harukeyua.fintrack.data.model.api.ClientInfo
import com.harukeyua.fintrack.data.model.api.StatementItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MonobankService {

    @GET("/personal/client-info")
    suspend fun getAccountInfo(@Header("X-Token") key: String): ClientInfo

    @GET("/personal/statement/{account}/{from}")
    suspend fun getStatements(
        @Header("X-Token") key: String,
        @Path("account") account: String,
        @Path("from") from: Long
    ): List<StatementItem>

    companion object {
        private const val BASE_URL = "https://api.monobank.ua"

        fun create(): MonobankService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
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