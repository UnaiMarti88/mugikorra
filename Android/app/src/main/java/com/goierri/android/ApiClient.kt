package com.goierri.android

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Cambia a la IP de tu PC si usas emulador
    private const val BASE_URL = "http://192.168.2.103:5093/"

    private val gson = GsonBuilder().setLenient().create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
