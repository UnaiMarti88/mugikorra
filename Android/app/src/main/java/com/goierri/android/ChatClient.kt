package com.goierri.android

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException

class ChatClient(
    private val host: String,
    private val puerto: Int,
    private val erabiltzaileIzena: String
) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var konektatuta = false
    private var onMensajeJasoDa: ((String) -> Unit)? = null

    suspend fun konektatu(scope: CoroutineScope, onMensaje: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            onMensajeJasoDa = onMensaje
            socket = Socket(host, puerto)
            reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            writer = PrintWriter(socket!!.outputStream, true)
            konektatuta = true
            Log.d("ChatClient", "Konektatuta zerbitzarira: $host:$puerto")

            // Iniciar corrutina de escucha en el scope proporcionado
            scope.launch(Dispatchers.IO) {
                entzun()
            }

            true
        } catch (e: Exception) {
            Log.e("ChatClient", "Errorea konektatzean", e)
            konektatuta = false
            false
        }
    }

    private suspend fun entzun() {
        try {
            var mezua: String? = null
            while (konektatuta && reader!!.readLine().also { mezua = it } != null) {
                Log.d("ChatClient", "Mezua jasota: $mezua")
                withContext(Dispatchers.Main) {
                    onMensajeJasoDa?.invoke(mezua!!)
                }
            }
        } catch (e: SocketException) {
            Log.d("ChatClient", "Socket itxita")
        } catch (e: Exception) {
            Log.e("ChatClient", "Errorea entzutean", e)
        } finally {
            konektatuta = false
        }
    }

    suspend fun bidaliMezua(mezua: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!konektatuta) {
                Log.e("ChatClient", "Ez dago konexiorik")
                return@withContext false
            }
            val osoa = "$erabiltzaileIzena: $mezua"
            writer!!.println(osoa)
            Log.d("ChatClient", "Mezua bidalita: $osoa")
            true
        } catch (e: Exception) {
            Log.e("ChatClient", "Errorea mezua bidaltzean", e)
            false
        }
    }

    fun itxi() {
        try {
            konektatuta = false
            reader?.close()
            writer?.close()
            socket?.close()
            Log.d("ChatClient", "Konexioa itxita")
        } catch (e: Exception) {
            Log.e("ChatClient", "Errorea konexioa itxitzean", e)
        }
    }

    fun estaDeskontektaduta(): Boolean = !konektatuta
}
