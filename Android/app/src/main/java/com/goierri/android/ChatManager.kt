package com.goierri.android

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatManager(
    private val host: String,
    private val puerto: Int,
    private val erabiltzaileIzena: String
) {
    private var chatClient: ChatClient? = null
    private var mensajeListeners = mutableListOf<(String) -> Unit>()
    private var konektatuta = false
    private var _mensajes = mutableListOf<String>()
    val mensajes: List<String> get() = _mensajes.toList()

    suspend fun konektatu(scope: CoroutineScope) {
        withContext(Dispatchers.IO) {
            try {
                chatClient = ChatClient(host, puerto, erabiltzaileIzena)
                val resultado = chatClient!!.konektatu(scope) { nuevoMensaje ->
                    Log.d("ChatManager", "Mensaje recibido: $nuevoMensaje")
                    _mensajes.add(nuevoMensaje)
                    // Notificar a todos los listeners
                    mensajeListeners.forEach { listener ->
                        listener(nuevoMensaje)
                    }
                }
                konektatuta = resultado
                Log.d("ChatManager", "Chat konektatuta: $konektatuta")
            } catch (e: Exception) {
                Log.e("ChatManager", "Error konektatzean", e)
                konektatuta = false
            }
        }
    }

    fun subscribirseAMensajes(listener: (String) -> Unit) {
        mensajeListeners.add(listener)
    }

    fun desuscribirse(listener: (String) -> Unit) {
        mensajeListeners.remove(listener)
    }

    suspend fun bidaliMezua(mezua: String): Boolean {
        return if (chatClient != null && konektatuta) {
            chatClient!!.bidaliMezua(mezua)
        } else {
            false
        }
    }

    fun deskonektatu() {
        chatClient?.itxi()
        konektatuta = false
        _mensajes.clear()
    }

    fun estaKonektatuta(): Boolean = konektatuta
}
