package com.goierri.android

data class LoginRequest(
    val erabiltzailea: String,
    val pasahitza: String
)

data class LoginResponse(
    val id: Int,
    val erabiltzailea: String,
    val emaila: String,
    val pasahitza: String,
    val rola: Rola,
    val ezabatua: Boolean,
    val aktibatuta: Boolean
)

data class Rola(
    val id: Int,
    val izena: String
)
// Models.kt

data class Kategoria(
    val id: Int,
    val izena: String
)

data class Produktua(
    val id: Int,
    val izena: String,
    val prezioa: Double
)

