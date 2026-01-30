package com.goierri.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class MainActivity : ComponentActivity() {

    private var chatManager: ChatManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                var loggedIn by remember { mutableStateOf(false) }
                var usuario by remember { mutableStateOf<Erabiltzailea?>(null) }

                Surface(modifier = Modifier.fillMaxSize()) {

                    if (loggedIn && usuario != null) {
                        EskaeraTPVScreen(
                            erabiltzaileId = usuario!!.id,
                            erabiltzaileIzena = usuario!!.erabiltzailea,
                            onLogout = {
                                // Primero actualizar el estado de UI para volver al login
                                val manager = chatManager
                                chatManager = null
                                usuario = null
                                loggedIn = false

                                // Cerrar el chat en segundo plano para evitar bloqueos
                                if (manager != null) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        try {
                                            manager.deskonektatu()
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                            },
                            activity = this@MainActivity,
                            tieneChatAcceso = usuario!!.txat,
                            chatManager = chatManager
                        )
                    } else {
                        LoginScreen { erabiltzailea, pasahitza ->

                            lifecycleScope.launch {
                                try {
                                    Log.d("Login", "Enviando login: $erabiltzailea / $pasahitza")

                                    val response = ApiClient.apiService.login(
                                        LoginRequest(
                                            erabiltzailea = erabiltzailea,
                                            pasahitza = pasahitza
                                        )
                                    )

                                    Log.d("Login", "Respuesta API: $response")

                                    val erabiltzaileak = response.datuak
                                    if (response.code == 200 && !erabiltzaileak.isNullOrEmpty()) {
                                        val user = erabiltzaileak.first()
                                        if (!user.ezabatua) {
                                            usuario = user
                                            loggedIn = true
                                            
                                            // Log: login correcto
                                            try {
                                                lifecycleScope.launch {
                                                    ApiClient.apiService.gordeLog(
                                                        LogRequest(
                                                            erabiltzailea = user.id,
                                                            ekintza = "Login arrakastatsua (${user.erabiltzailea})"
                                                        )
                                                    )
                                                }
                                            } catch (_: Exception) {
                                                // No romper el login si el log falla
                                            }
                                            
                                            // Conectar al chat
                                            if (user.txat) {
                                                chatManager = ChatManager("192.168.2.103", 50001, user.erabiltzailea)
                                                lifecycleScope.launch {
                                                    chatManager?.konektatu(lifecycleScope)
                                                }
                                            }
                                            
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Saioa ondo hasi da",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Erabiltzailea ez dago aktibo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        // Log: login fallido
                                        try {
                                            lifecycleScope.launch {
                                                ApiClient.apiService.gordeLog(
                                                    LogRequest(
                                                        erabiltzailea = 0,
                                                        ekintza = "Login huts egin du: $erabiltzailea"
                                                    )
                                                )
                                            }
                                        } catch (_: Exception) {
                                        }
                                        Toast.makeText(
                                            this@MainActivity,
                                            response.message.ifBlank { "Login akatsa" },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } catch (e: HttpException) {
                                    Log.e("MainActivity", "Error HTTP", e)
                                    val message = extractErrorMessage(e)
                                    // Log: error HTTP en login
                                    try {
                                        lifecycleScope.launch {
                                            ApiClient.apiService.gordeLog(
                                                LogRequest(
                                                    erabiltzailea = 0,
                                                    ekintza = "Login HTTP errorea: $erabiltzailea (${e.code()})"
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } catch (e: IOException) {
                                    // Error de red
                                    Log.e("MainActivity", "Error de conexión", e)
                                    // Log: error de red en login
                                    try {
                                        lifecycleScope.launch {
                                            ApiClient.apiService.gordeLog(
                                                LogRequest(
                                                    erabiltzailea = 0,
                                                    ekintza = "Login konexio errorea: $erabiltzailea"
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Konexio errorea: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error inesperado", e)
                                    // Log: error inesperado en login
                                    try {
                                        lifecycleScope.launch {
                                            ApiClient.apiService.gordeLog(
                                                LogRequest(
                                                    erabiltzailea = 0,
                                                    ekintza = "Login errore ezezaguna: $erabiltzailea"
                                                )
                                            )
                                        }
                                    } catch (_: Exception) {
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Login akatsa",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractErrorMessage(e: HttpException): String {
    val code = e.code()
    val body = e.response()?.errorBody()?.string()

    if (!body.isNullOrBlank()) {
        try {
            val dto = Gson().fromJson(body, ErantzunaDTO::class.java)
            val msg = (dto?.message as? String)?.trim()
            if (!msg.isNullOrBlank()) {
                return msg
            }
        } catch (_: JsonSyntaxException) {
            // ignore parsing error
        }
    }

    return if (code == 401) {
        "Erabiltzaile edo pasahitz okerra"
    } else {
        "Login akatsa (HTTP $code)"
    }
}
