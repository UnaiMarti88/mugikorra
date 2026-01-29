package com.goierri.android

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    // Auto-scroll cuando hay nuevos mensajes
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    // Desuscribirse al cerrar la pantalla
    DisposableEffect(Unit) {
        onDispose {
            chatManager?.desuscribirse(onMensajeListener)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "TXATA",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Itzuli")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF5F9EA0)
            )
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

        // Input de mensaje
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
                        .heightIn(min = 40.dp),
                    placeholder = { Text("Idatzi mezua...") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = false
                )

                Button(
                    onClick = {
                        if (mensajeActual.isNotBlank() && chatManager != null) {
                            scope.launch {
                                val resultado = chatManager.bidaliMezua(mensajeActual)
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
                                                ekintza = "Txat mezua bidalia: ${mensajeActual.take(100)}"
                                            )
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                                mensajeActual = ""
                            }
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("→", fontSize = 20.sp)
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
                .fillMaxWidth(0.8f)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = if (esMio) Color(0xFF90CAF9) else Color(0xFFE3F2FD)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
