package com.goierri.android

import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EskaeraTPVScreen() {
    val scope = rememberCoroutineScope()
    var kategoriak by remember { mutableStateOf<List<Kategoria>>(emptyList()) }
    var kategoriaAktiboa by remember { mutableStateOf<Kategoria?>(null) }
    var produktuak by remember { mutableStateOf<List<Produktua>>(emptyList()) }
    var eskaera by remember { mutableStateOf<List<Produktua>>(emptyList()) }

    var seleccionado by remember { mutableStateOf<Produktua?>(null) }
    var menuAbierto by remember { mutableStateOf(false) }

    // Cargar categorías al iniciar
    LaunchedEffect(Unit) {
        try {
            kategoriak = ApiClient.apiService.getKategoriak()
        } catch (e: Exception) {
            Log.e("TPV", "Error cargando categorias", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ------------------- CONTENIDO PRINCIPAL -------------------
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderTPV(
                modifier = Modifier.height(100.dp),
                onMenuClick = { menuAbierto = !menuAbierto }
            )

            Row(modifier = Modifier.fillMaxSize()) {
                // ----------------- PANEL IZQUIERDO -----------------
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    // Lista de productos seleccionados
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        items(eskaera) { producto ->
                            val estaSeleccionado = producto == seleccionado
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (estaSeleccionado) Color(0xFFB3E5FC) else Color.Transparent)
                                    .clickable { seleccionado = producto }
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "• ${producto.izena} - ${producto.prezioa}€",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid de productos
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
                                    .clickable {
                                        eskaera = eskaera + producto
                                        seleccionado = null
                                    }
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
                                    Text("${producto.prezioa} €", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----------------- GRID DE CATEGORIAS -----------------
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 4 botones por fila
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp), // ajustado para dos filas
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
                                            produktuak =
                                                ApiClient.apiService.getProduktuakByKategoria(kategoria.id)
                                        } catch (e: Exception) {
                                            Log.e("TPV", "Error cargando productos", e)
                                        }
                                    }
                                },
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

                // ----------------- PANEL DERECHO (botones acción) -----------------
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight(0.5f) // solo hasta mitad de altura
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val botones = listOf("Diru Totala", "Ilara Ezabatu", "Mahia Definitu", "Eskaera Gorde")
                    botones.forEach { text ->
                        Button(
                            onClick = {
                                if (text == "Ilara Ezabatu") {
                                    // borrar producto seleccionado
                                    if (seleccionado != null) {
                                        eskaera = eskaera.filter { it != seleccionado }
                                        seleccionado = null
                                    } else if (eskaera.isNotEmpty()) {
                                        // borrar último producto
                                        eskaera = eskaera.dropLast(1)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(text)
                        }
                    }
                }
            }
        }

        // ------------------- MENÚ LATERAL -------------------
        Menua(
            menuAbierto = menuAbierto,
            onCloseMenu = { menuAbierto = false },
            onEskaeraEginClick = { /* TODO */ },
            onEskaeraIkusiClick = { /* TODO */ },
            onMahiakIkusiClick = { /* TODO */ }
        )
    }
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
