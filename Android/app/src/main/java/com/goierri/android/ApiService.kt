package com.goierri.android

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    // 🔐 Login
    @POST("api/Logina")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // 📦 Obtener todas las categorías
    @GET("api/Kategoria")
    suspend fun getKategoriak(): List<Kategoria>

    // 🧃 Obtener productos por categoría
    @GET("api/Produktuak/kategoria/{id}")
    suspend fun getProduktuakByKategoria(
        @Path("id") kategoriaId: Int
    ): List<Produktua>
}
