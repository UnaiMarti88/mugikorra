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
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.HttpException
import java.io.IOException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                var loggedIn by remember { mutableStateOf(false) }
                var usuarioNombre by remember { mutableStateOf("") }

                Surface(modifier = Modifier.fillMaxSize()) {

                    if (loggedIn) {
                        EskaeraTPVScreen()
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

                                    // ✅ Usuario correcto
                                    if (!response.ezabatua) {
                                        usuarioNombre = response.erabiltzailea
                                        loggedIn = true
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Usuario correcto",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // ❌ Usuario marcado como eliminado
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Login akatsa",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } catch (e: HttpException) {
                                    // Error HTTP (como 401 Unauthorized)
                                    Log.e("MainActivity", "Error HTTP", e)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Login akatsa",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } catch (e: IOException) {
                                    // Error de red
                                    Log.e("MainActivity", "Error de conexión", e)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Konexio errorea: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error inesperado", e)
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
