package com.smartcart.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ✅ ВСТАВЬ тот адрес, который ОТКРЫЛСЯ в браузере эмулятора:
    // Если работает 10.0.2.2:
    // private const val BASE_URL = "http://10.0.2.2:8001/"
    //
    // Если работает твой IP:
    const val BASE_URL = "http://10.0.2.2:8000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
