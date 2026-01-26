package com.goierri.android

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val erabiltzailea: String,
    val pasahitza: String
)

data class ErantzunaDTO<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("datuak") val datuak: List<T>?
)

data class Erabiltzailea(
    val id: Int,
    val erabiltzailea: String,
    val emaila: String,
    val ezabatua: Boolean,
    val txat: Boolean,
    val rola: Rola
)

data class Rola(
    val id: Int,
    val izena: String? = null
)

data class Kategoria(
    val id: Int,
    val izena: String
)

data class Produktua(
    val id: Int,
    val izena: String,
    val prezioa: Double,
    @SerializedName("kategoria_id") val kategoriaId: Int? = null,
    @SerializedName("stock_aktuala") val stockAktuala: Int? = null
)

data class EskaeraSortuRequest(
    @SerializedName("erabiltzaileId") val erabiltzaileId: Int,
    @SerializedName("mahaiaId") val mahaiaId: Int,
    @SerializedName("komensalak") val komensalak: Int,
    @SerializedName("produktuak") val produktuak: List<EskaeraProduktuaSortuRequest>
)

data class EskaeraProduktuaSortuRequest(
    @SerializedName("produktuaId") val produktuaId: Int,
    @SerializedName("kantitatea") val kantitatea: Int,
    @SerializedName("prezioUnitarioa") val prezioUnitarioa: Double
)

data class EskaeraProduktuaEditatuRequest(
    @SerializedName("produktuaId") val produktuaId: Int,
    @SerializedName("kantitatea") val kantitatea: Int
)

data class EskaeraEguneratuRequest(
    @SerializedName("komensalak") val komensalak: Int,
    @SerializedName("produktuak") val produktuak: List<EskaeraProduktuaEditatuRequest>
)

data class EskaeraDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("izena") val izena: String,
    @SerializedName("mahaiaId") val mahaiaId: Int,
    @SerializedName("komensalak") val komensalak: Int,
    @SerializedName("data") val data: String
)

data class EskaeraLortuDTO(
    @SerializedName("produktuaId") val produktuaId: Int,
    @SerializedName("produktuaIzena") val produktuaIzena: String,
    @SerializedName("prezioUnitarioa") val prezioUnitarioa: Double,
    @SerializedName("kantitatea") val kantitatea: Int
)

data class MahaiaDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("zenbakia") val zenbakia: Int,
    @SerializedName("kapazitatea") val kapazitatea: Int? = null
)

data class OrderItem(
    val produktua: Produktua,
    val kantitatea: Int
)

