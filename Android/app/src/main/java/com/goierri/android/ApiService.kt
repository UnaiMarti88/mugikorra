package com.goierri.android

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface ApiService {

    // 🔐 Login
    @POST("api/Logina")
    suspend fun login(
        @Body request: LoginRequest
    ): ErantzunaDTO<Erabiltzailea>

    // 📦 Obtener todas las categorías
    @GET("api/Kategoria")
    suspend fun getKategoriak(): List<Kategoria>

    // 🧃 Obtener productos por categoría
    @GET("api/Produktuak/kategoria/{id}")
    suspend fun getProduktuakByKategoria(
        @Path("id") kategoriaId: Int
    ): List<Produktua>

    // 🪑 Mahai libreak
    @GET("api/Mahaiak/libre")
    suspend fun getMahaiLibre(): ErantzunaDTO<MahaiaDTO>

    // 🧾 Eskaerak
    @POST("api/eskaerak")
    suspend fun sortuEskaera(
        @Body request: EskaeraSortuRequest
    ): ErantzunaDTO<String>

    @GET("api/eskaerak")
    suspend fun getEskaerak(): ErantzunaDTO<EskaeraDTO>

    @GET("api/eskaerak/{id}/produktuak")
    suspend fun getEskaeraProduktuak(
        @Path("id") eskaeraId: Int
    ): ErantzunaDTO<EskaeraLortuDTO>

    @GET("api/eskaerak/mahaiak/{id}/kapazitatea")
    suspend fun getMahaiKapasitatea(
        @Path("id") mahaiaId: Int
    ): ErantzunaDTO<Int>

    @PUT("api/eskaerak/{id}")
    suspend fun eguneratuEskaera(
        @Path("id") eskaeraId: Int,
        @Body request: EskaeraEguneratuRequest
    ): ErantzunaDTO<String>

    @DELETE("api/eskaerak/{id}")
    suspend fun ezabatuEskaera(
        @Path("id") eskaeraId: Int
    ): ErantzunaDTO<String>
}
