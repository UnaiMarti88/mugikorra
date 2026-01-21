package com.goierri.android

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/Logina")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
