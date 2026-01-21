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

