package com.goierri.android

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.text.SimpleDateFormat
import java.util.*

private enum class TPVView {
    ESKAERA_EGIN,
    ESKAERA_IKUSI,
    MAHAIAK_IKUSI,
    CHAT
}

@Composable
fun EskaeraTPVScreen(
    erabiltzaileId: Int,
    erabiltzaileIzena: String,
    onLogout: () -> Unit,
    activity: android.app.Activity? = null,
    tieneChatAcceso: Boolean = false,
    chatManager: ChatManager? = null
) {
    var menuAbierto by remember { mutableStateOf(false) }
    var view by remember { mutableStateOf(TPVView.ESKAERA_EGIN) }
    var tieneNotificacionChat by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Listener para notificaciones del chat
    val onMensajeRecibidoGlobal = { _: String ->
        if (view != TPVView.CHAT) {
            tieneNotificacionChat = true
        }
    }
    
    // Subscribirse a mensajes cuando entra
    LaunchedEffect(chatManager) {
        chatManager?.subscribirseAMensajes(onMensajeRecibidoGlobal)
    }
    
    // Desuscribirse cuando sale
    DisposableEffect(chatManager) {
        onDispose {
            chatManager?.desuscribirse(onMensajeRecibidoGlobal)
        }
    }
    
    // Estado compartido para mesa y comensales (se mantiene al cambiar de vista)
    var mahaiaHautatua by remember { mutableStateOf<MahaiaDTO?>(null) }
    var komensalak by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderTPV(
                modifier = Modifier.height(100.dp),
                erabiltzaileIzena = erabiltzaileIzena,
                onMenuClick = { menuAbierto = !menuAbierto },
                onChatClick = {
                    if (tieneChatAcceso) {
                        view = TPVView.CHAT
                        tieneNotificacionChat = false
                    } else {
                        Toast.makeText(context, "Ez duzu txatara sartzeko baimenik", Toast.LENGTH_SHORT).show()
                    }
                },
                chatHabilitado = tieneChatAcceso,
                tieneNotificacion = tieneNotificacionChat
            )

            when (view) {
                TPVView.ESKAERA_EGIN -> EskaeraEginContent(
                    erabiltzaileId = erabiltzaileId,
                    erabiltzaileIzena = erabiltzaileIzena,
                    mahaiaHautatua = mahaiaHautatua,
                    onMahaiaChange = { mahaiaHautatua = it },
                    komensalak = komensalak,
                    onKomensalakChange = { komensalak = it }
                )
                TPVView.ESKAERA_IKUSI -> EskaeraIkusiScreen()
                TPVView.MAHAIAK_IKUSI -> MahaiakIkusiScreen()
                TPVView.CHAT -> {
                    if (activity != null) {
                        ChatScreen(
                            erabiltzaileId = erabiltzaileId,
                            erabiltzaileIzena = erabiltzaileIzena,
                            onBackClick = { view = TPVView.ESKAERA_EGIN },
                            activity = activity,
                            chatManager = chatManager
                        )
                        tieneNotificacionChat = false
                    }
                }
            }
        }

        Menua(
            menuAbierto = menuAbierto,
            onCloseMenu = { menuAbierto = false },
            onEskaeraEginClick = {
                view = TPVView.ESKAERA_EGIN
                menuAbierto = false
            },
            onEskaeraIkusiClick = {
                view = TPVView.ESKAERA_IKUSI
                menuAbierto = false
            },
            onMahiakIkusiClick = {
                view = TPVView.MAHAIAK_IKUSI
                menuAbierto = false
            },
            onLogoutClick = {
                menuAbierto = false
                onLogout()
            }
        )
    }
}

@Composable
private fun EskaeraEginContent(
    erabiltzaileId: Int,
    erabiltzaileIzena: String,
    mahaiaHautatua: MahaiaDTO?,
    onMahaiaChange: (MahaiaDTO?) -> Unit,
    komensalak: Int?,
    onKomensalakChange: (Int?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var kategoriak by remember { mutableStateOf<List<Kategoria>>(emptyList()) }
    var kategoriaAktiboa by remember { mutableStateOf<Kategoria?>(null) }
    var produktuak by remember { mutableStateOf<List<Produktua>>(emptyList()) }
    var eskaera by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    var hautatutakoItem by remember { mutableStateOf<OrderItem?>(null) }
    var mahaiLibreak by remember { mutableStateOf<List<MahaiaDTO>>(emptyList()) }
    var mahaiKapasitatea by remember { mutableStateOf<Int?>(null) }

    var mahaiDialog by remember { mutableStateOf(false) }
    var komensalDialog by remember { mutableStateOf(false) }

    val isMahaiSelected = mahaiaHautatua != null
    val isKomensalakSelected = (komensalak != null && komensalak!! > 0)
    val isOrderReady = isMahaiSelected && isKomensalakSelected
    val contentAlpha = if (isOrderReady) 1f else 0.4f

    LaunchedEffect(Unit) {
        try {
            kategoriak = ApiClient.apiService.getKategoriak()
        } catch (e: Exception) {
            Log.e("TPV", "Kategoriak kargatzean errorea", e)
            Toast.makeText(context, "Ezin dira kategoriak kargatu", Toast.LENGTH_SHORT).show()
        }
    }

    val guztira = remember(eskaera) {
        eskaera.sumOf { it.produktua.prezioa * it.kantitatea }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .padding(8.dp)
            .alpha(contentAlpha)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Zerbitzaria: $erabiltzaileIzena", fontWeight = FontWeight.Bold)
                    val mahaiaText = mahaiaHautatua?.zenbakia?.toString() ?: "-"
                    val komensalText = komensalak?.toString() ?: "-"
                    Text("Mahaia: $mahaiaText · Komensalak: $komensalText")
                    Text("Guztira: ${String.format(Locale.getDefault(), "%.2f", guztira)} €")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                if (eskaera.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ez dago produkturik", color = Color.Gray)
                        }
                    }
                } else {
                    items(eskaera) { item ->
                        val selected = item == hautatutakoItem
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .then(
                                    if (isOrderReady) Modifier.clickable { hautatutakoItem = item } else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Color(0xFF1976D2) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (selected) 6.dp else 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🍽️ ${item.produktua.izena}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selected) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "x${item.kantitatea}",
                                        fontSize = 12.sp,
                                        color = if (selected) Color(0xFFBBDEFB) else Color.Gray
                                    )
                                }
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.2f", item.produktua.prezioa)} €",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selected) Color(0xFFC8E6C9) else Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                gridItems(produktuak) { producto ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .then(
                                if (isOrderReady) Modifier.clickable {
                                    val existing = eskaera.firstOrNull { it.produktua.id == producto.id }
                                    eskaera = if (existing == null) {
                                        eskaera + OrderItem(producto, 1)
                                    } else {
                                        eskaera.map {
                                            if (it.produktua.id == producto.id) it.copy(kantitatea = it.kantitatea + 1) else it
                                        }
                                    }
                                    hautatutakoItem = null
                                } else Modifier
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = producto.izena,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(producto.izena, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "Stock: ${producto.stockAktuala ?: "-"}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text("${String.format(Locale.getDefault(), "%.2f", producto.prezioa)} €", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gridItems(kategoriak) { kategoria ->
                    val seleccionada = kategoriaAktiboa?.id == kategoria.id
                    Button(
                        onClick = {
                            kategoriaAktiboa = kategoria
                            scope.launch {
                                try {
                                    produktuak = ApiClient.apiService.getProduktuakByKategoria(kategoria.id)
                                } catch (e: Exception) {
                                    Log.e("TPV", "Produktuak kargatzean errorea", e)
                                    Toast.makeText(context, "Ezin dira produktuak kargatu", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = isOrderReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seleccionada) Color(0xFF1565C0) else Color(0xFF90CAF9)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(kategoria.izena, fontSize = 16.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bloque de botones (arriba)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Guztira: ${String.format(Locale.getDefault(), "%.2f", guztira)} €",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                enabled = isOrderReady,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("💰 Diru Totala", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    val item = hautatutakoItem
                    if (item != null) {
                        if (item.kantitatea > 1) {
                            eskaera = eskaera.map {
                                if (it.produktua.id == item.produktua.id) it.copy(kantitatea = it.kantitatea - 1) else it
                            }
                        } else {
                            eskaera = eskaera.filter { it.produktua.id != item.produktua.id }
                        }
                        hautatutakoItem = null
                    } else if (eskaera.isNotEmpty()) {
                        val last = eskaera.last()
                        eskaera = if (last.kantitatea > 1) {
                            eskaera.dropLast(1) + last.copy(kantitatea = last.kantitatea - 1)
                        } else {
                            eskaera.dropLast(1)
                        }
                    }
                },
                enabled = isOrderReady,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("🗑️ Ilara Ezabatu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = ApiClient.apiService.getMahaiLibre()
                            if (response.code == 200 && !response.datuak.isNullOrEmpty()) {
                                mahaiLibreak = response.datuak
                                mahaiDialog = true
                            } else {
                                Toast.makeText(
                                    context,
                                    response.message.ifBlank { "Ez dago mahai librerik" },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e("TPV", "Mahai libreak kargatzean errorea", e)
                            Toast.makeText(context, "Ezin dira mahaiak kargatu", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("🪑 Mahia Aukeratu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    if (eskaera.isEmpty()) {
                        Toast.makeText(context, "Ez dago produkturik", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (mahaiaHautatua == null) {
                        Toast.makeText(context, "Mahairik ez duzu aukeratu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (komensalak == null || komensalak!! <= 0) {
                        Toast.makeText(context, "Komensalak zehaztu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        try {
                            val request = EskaeraSortuRequest(
                                erabiltzaileId = erabiltzaileId,
                                mahaiaId = mahaiaHautatua!!.id,
                                komensalak = komensalak!!,
                                produktuak = eskaera.map {
                                    EskaeraProduktuaSortuRequest(
                                        produktuaId = it.produktua.id,
                                        kantitatea = it.kantitatea,
                                        prezioUnitarioa = it.produktua.prezioa
                                    )
                                }
                            )

                            val response = ApiClient.apiService.sortuEskaera(request)
                            if (response.code == 200) {
                                Toast.makeText(context, "Eskaera ongi gorde da", Toast.LENGTH_SHORT).show()
                                // Log: eskaera berria sortu da
                                try {
                                    ApiClient.apiService.gordeLog(
                                        LogRequest(
                                            erabiltzailea = erabiltzaileId,
                                            ekintza = "Eskaera berria sortu da. Mahaia=${mahaiaHautatua!!.zenbakia}, Guztira=${String.format(Locale.getDefault(), "%.2f", guztira)}€"
                                        )
                                    )
                                } catch (_: Exception) {
                                }
                                // Garbitu eskaera eta mahaia / komensalak, mahaia libre uzteko hurrengo eskaerarako
                                eskaera = emptyList()
                                hautatutakoItem = null
                                onMahaiaChange(null)
                                onKomensalakChange(null)
                            } else {
                                val extra = if (!response.datuak.isNullOrEmpty()) {
                                    " (${response.datuak.joinToString(", ")})"
                                } else ""
                                Toast.makeText(
                                    context,
                                    response.message.ifBlank { "Eskaera ezin izan da gorde" } + extra,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: HttpException) {
                            val msg = extractServerMessage(e, "Eskaera ezin izan da gorde")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("TPV", "Eskaera gordetzean errorea", e)
                            Toast.makeText(context, "Eskaera ezin izan da gorde", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = isOrderReady,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("✅ Eskaera Gorde", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            }

            // Usuario y Hora abajo del todo a la derecha
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.usuario),
                        contentDescription = "Erabiltzaile ikonoa",
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        erabiltzaileIzena,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                var horaActual by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    while (true) {
                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        horaActual = sdf.format(Date())
                        kotlinx.coroutines.delay(1000)
                    }
                }

                Text(horaActual, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
    }

    if (mahaiDialog) {
        AlertDialog(
            onDismissRequest = { mahaiDialog = false },
            title = { Text("Mahai libreak") },
            text = {
                Column {
                    mahaiLibreak.forEach { m ->
                        Text(
                            text = "Mahaia ${m.zenbakia}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMahaiaChange(m)
                                    mahaiDialog = false
                                    scope.launch {
                                        try {
                                            // Log: mahaia hautatu da (hemen bai, coroutine barruan)
                                            try {
                                                ApiClient.apiService.gordeLog(
                                                    LogRequest(
                                                        erabiltzailea = erabiltzaileId,
                                                        ekintza = "Mahaia hautatu da: ${m.zenbakia}"
                                                    )
                                                )
                                            } catch (_: Exception) {
                                            }
                                            val resp = ApiClient.apiService.getMahaiKapasitatea(m.id)
                                            mahaiKapasitatea = resp.datuak?.firstOrNull()
                                        } catch (e: Exception) {
                                            Log.e("TPV", "Kapasitatea lortzean errorea", e)
                                            mahaiKapasitatea = null
                                        }
                                        komensalDialog = true
                                    }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mahaiDialog = false }) {
                    Text("Itxi")
                }
            }
        )
    }

    if (komensalDialog) {
        var komensalText by remember { mutableStateOf(komensalak?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { komensalDialog = false },
            title = { Text("Komensalak") },
            text = {
                val maxText = mahaiKapasitatea?.toString() ?: "-"
                OutlinedTextField(
                    value = komensalText,
                    onValueChange = { komensalText = it.filter { ch -> ch.isDigit() } },
                    singleLine = true,
                    label = { Text("Pertsona kopurua (max $maxText)") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = komensalText.toIntOrNull()
                        val max = mahaiKapasitatea
                        if (value == null || value <= 0) {
                            Toast.makeText(context, "Komensalak zehaztu", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (max != null && value > max) {
                            Toast.makeText(context, "Gehienez $max pertsona", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        onKomensalakChange(value)
                        komensalDialog = false
                    }
                ) {
                    Text("Gorde")
                }
            },
            dismissButton = {
                TextButton(onClick = { komensalDialog = false }) {
                    Text("Utzi")
                }
            }
        )
    }
}

private fun extractServerMessage(e: HttpException, fallback: String): String {
    val body = e.response()?.errorBody()?.string()
    if (!body.isNullOrBlank()) {
        try {
            val dto = Gson().fromJson(body, ErantzunaDTO::class.java)
            val msg = (dto?.message as? String)?.trim()
            if (!msg.isNullOrBlank()) return msg
        } catch (_: JsonSyntaxException) {
        }
    }
    return "$fallback (HTTP ${e.code()})"
}

// ----------------------------------------------------------------
// HEADER CON MENÚ Y CHAT
// ----------------------------------------------------------------
@Composable
fun HeaderTPV(
    modifier: Modifier = Modifier, 
    erabiltzaileIzena: String = "",
    onMenuClick: () -> Unit, 
    onChatClick: () -> Unit, 
    chatHabilitado: Boolean = true, 
    tieneNotificacion: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF5F9EA0))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Izquierda: Menú
        Text("☰", fontSize = 32.sp, modifier = Modifier.clickable { onMenuClick() }, color = Color.White)
        
        // Derecha: Chat
        Box(contentAlignment = Alignment.TopEnd) {
            Text(
                "💬",
                fontSize = 28.sp,
                modifier = Modifier
                    .clickable(enabled = chatHabilitado) { onChatClick() }
                    .alpha(if (chatHabilitado) 1f else 0.5f)
            )
            if (tieneNotificacion && chatHabilitado) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, shape = CircleShape)
                        .offset((-4).dp, 2.dp)
                )
            }
        }
    }
}
