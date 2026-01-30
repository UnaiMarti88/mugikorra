package com.goierri.android

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    erabiltzaileId: Int,
    erabiltzaileIzena: String,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit,
    activity: android.app.Activity,
    chatManager: ChatManager? = null,
    onMensajeRecibido: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mensajes by remember { mutableStateOf<List<String>>(chatManager?.mensajes ?: emptyList()) }
    var mensajeActual by remember { mutableStateOf("") }
    var konektatuta by remember { mutableStateOf(chatManager?.estaKonektatuta() ?: false) }
    var konektatzean by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Listener para recibir mensajes del ChatManager
    val onMensajeListener: (String) -> Unit = { nuevoMensaje ->
        mensajes = chatManager?.mensajes ?: emptyList()
        Log.d("ChatScreen", "Mensaje recibido: $nuevoMensaje, Total: ${mensajes.size}")
    }

    // Subscribirse a mensajes cuando entra a la pantalla de chat
    LaunchedEffect(chatManager) {
        if (chatManager != null) {
            // Obtener mensajes existentes
            mensajes = chatManager.mensajes
            konektatuta = true
            chatManager.subscribirseAMensajes(onMensajeListener)
        } else {
            Toast.makeText(context, "Chat no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    // Mantener la posición elegida por el usuario; no auto-scroll al final

    // Desuscribirse al cerrar la pantalla
    DisposableEffect(Unit) {
        onDispose {
            chatManager?.desuscribirse(onMensajeListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // Header reutilizando el mismo componente que en EskaeraTPVScreen
        HeaderTPV(
            modifier = Modifier.height(100.dp),
            erabiltzaileIzena = erabiltzaileIzena,
            onMenuClick = onMenuClick,
            onChatClick = {},
            chatHabilitado = false,
            tieneNotificacion = false,
            centerTitle = "TXATA"
        )

        // Estado de conexión
        if (konektatzean) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Konektatzean...")
            }
        } else if (!konektatuta) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFCDD2))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Zerbitzariarekiko konexiorik ez", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        // Mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mensajes) { mensaje ->
                MensajeItem(mensaje, erabiltzaileIzena)
            }
        }

        // Input de mensaje (diseño limpio, sin cajas marcadas)
        if (konektatuta) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = mensajeActual,
                    onValueChange = { mensajeActual = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                    placeholder = { Text("Idatzi mezua...") },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFB0BEC5),
                        cursorColor = Color.Black
                    )
                )

                IconButton(
                    onClick = {
                        if (mensajeActual.isNotBlank() && chatManager != null) {
                            scope.launch {
                                val testua = mensajeActual
                                val resultado = chatManager.bidaliMezua(testua)
                                if (!resultado) {
                                    Toast.makeText(
                                        context,
                                        "Ezin izan da mezua bidali",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Log: mezua bidalia
                                    try {
                                        ApiClient.apiService.gordeLog(
                                            LogRequest(
                                                erabiltzailea = erabiltzaileId,
                                                ekintza = "Txat mezua bidalia: ${testua.take(100)}"
                                            )
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                                mensajeActual = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF1976D2), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Bidali",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MensajeItem(mensaje: String, usuarioActual: String) {
    val parts = mensaje.split(":" , limit = 2)
    val usuario = if (parts.size > 1) parts[0].trim() else "Deskonozitu"
    val contenido = if (parts.size > 1) parts[1].trim() else mensaje
    val esMio = usuario == usuarioActual
    val textoMostrar = if (parts.size > 1) "$usuario: $contenido" else mensaje

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(min = 40.dp, max = 260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (esMio) Color(0xFF1976D2) else Color(0xFFE0E0E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(
                    text = textoMostrar,
                    color = if (esMio) Color.White else Color.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}
