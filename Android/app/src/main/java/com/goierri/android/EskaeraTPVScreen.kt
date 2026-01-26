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
    MAHAIAK_IKUSI
}

@Composable
fun EskaeraTPVScreen(
    erabiltzaileId: Int,
    erabiltzaileIzena: String,
    onLogout: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    var view by remember { mutableStateOf(TPVView.ESKAERA_EGIN) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderTPV(
                modifier = Modifier.height(100.dp),
                onMenuClick = { menuAbierto = !menuAbierto }
            )

            when (view) {
                TPVView.ESKAERA_EGIN -> EskaeraEginContent(
                    erabiltzaileId = erabiltzaileId,
                    erabiltzaileIzena = erabiltzaileIzena
                )
                TPVView.ESKAERA_IKUSI -> EskaeraIkusiScreen()
                TPVView.MAHAIAK_IKUSI -> MahaiakIkusiScreen()
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
    erabiltzaileIzena: String
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var kategoriak by remember { mutableStateOf<List<Kategoria>>(emptyList()) }
    var kategoriaAktiboa by remember { mutableStateOf<Kategoria?>(null) }
    var produktuak by remember { mutableStateOf<List<Produktua>>(emptyList()) }
    var eskaera by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    var hautatutakoItem by remember { mutableStateOf<OrderItem?>(null) }
    var mahaiaHautatua by remember { mutableStateOf<MahaiaDTO?>(null) }
    var komensalak by remember { mutableStateOf<Int?>(null) }
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
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                items(eskaera) { item ->
                    val selected = item == hautatutakoItem
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) Color(0xFFB3E5FC) else Color.Transparent)
                            .then(
                                if (isOrderReady) Modifier.clickable { hautatutakoItem = item } else Modifier
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "• ${item.produktua.izena} x${item.kantitatea} - ${String.format(Locale.getDefault(), "%.2f", item.produktua.prezioa)} €",
                            fontSize = 16.sp
                        )
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
                .fillMaxHeight(0.6f)
                .padding(4.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Diru Totala")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Ilara Ezabatu")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Mahia Aukeratu")
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
                                eskaera = emptyList()
                                hautatutakoItem = null
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Eskaera Gorde")
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
                                    mahaiaHautatua = m
                                    mahaiDialog = false
                                    scope.launch {
                                        try {
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
                        komensalak = value
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
// HEADER CON HORA Y MENÚ
// ----------------------------------------------------------------
@Composable
fun HeaderTPV(modifier: Modifier = Modifier, onMenuClick: () -> Unit) {
    var horaActual by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            horaActual = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF5F9EA0))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("☰", fontSize = 32.sp, modifier = Modifier.clickable { onMenuClick() })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💬", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(horaActual, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
