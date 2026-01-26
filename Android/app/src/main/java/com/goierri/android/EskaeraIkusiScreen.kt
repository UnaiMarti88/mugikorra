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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun EskaeraIkusiScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var eskaerak by remember { mutableStateOf<List<EskaeraDTO>>(emptyList()) }
    var hautatutakoEskaera by remember { mutableStateOf<EskaeraDTO?>(null) }
    var produktuak by remember { mutableStateOf<List<EskaeraLortuDTO>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }

    fun kargatuEskaerak() {
        scope.launch {
            loading = true
            try {
                val response = ApiClient.apiService.getEskaerak()
                if (response.code == 200) {
                    eskaerak = response.datuak ?: emptyList()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EskaeraIkusi", "Eskaerak kargatzean errorea", e)
                Toast.makeText(context, "Ezin dira eskaerak kargatu", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    fun kargatuProduktuak(eskaeraId: Int) {
        scope.launch {
            try {
                val response = ApiClient.apiService.getEskaeraProduktuak(eskaeraId)
                if (response.code == 200) {
                    produktuak = response.datuak ?: emptyList()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EskaeraIkusi", "Produktuak kargatzean errorea", e)
                Toast.makeText(context, "Ezin dira produktuak kargatu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        kargatuEskaerak()
    }

    if (editMode && hautatutakoEskaera != null) {
        EskaeraEditatuScreen(
            eskaera = hautatutakoEskaera!!,
            produktuak = produktuak,
            onBack = { editMode = false },
            onSaved = {
                editMode = false
                kargatuEskaerak()
                kargatuProduktuak(hautatutakoEskaera!!.id)
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Eskaerak", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = { kargatuEskaerak() }) {
                Text("Berritu")
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                if (eskaerak.isEmpty()) {
                    item {
                        Text("Ez dago eskaerarik")
                    }
                } else {
                    items(eskaerak) { eskaera ->
                        val selected = hautatutakoEskaera?.id == eskaera.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    hautatutakoEskaera = eskaera
                                    kargatuProduktuak(eskaera.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Color(0xFFB3E5FC) else Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Eskaera #${eskaera.id}", fontWeight = FontWeight.Bold)
                                Text("Mahaia: ${eskaera.mahaiaId} · Komensalak: ${eskaera.komensalak}")
                                Text("Sukaldea: ${eskaera.sukaldeaEgoera}")
                                Text("Data: ${eskaera.data}")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Text("Produktuak", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val selectedEskaera = hautatutakoEskaera
                if (selectedEskaera == null) {
                    Text("Aukeratu eskaera bat")
                } else {
                    Text("Sukaldea: ${selectedEskaera.sukaldeaEgoera}")
                    Spacer(modifier = Modifier.height(6.dp))

                    if (produktuak.isEmpty()) {
                        Text("Eskaeran ez dago produkturik")
                    } else {
                    val guztira = produktuak.sumOf { it.prezioUnitarioa * it.kantitatea }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(produktuak) { p ->
                            Text("• ${p.produktuaIzena} - ${String.format(Locale.getDefault(), "%.2f", p.prezioUnitarioa)} €")
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Guztira: ${String.format(Locale.getDefault(), "%.2f", guztira)} €",
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { editMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editatu eskaera")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { deleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Eskaera ezabatu")
                    }
                }
            }
        }
    }

    if (deleteConfirm && hautatutakoEskaera != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            title = { Text("Eskaera ezabatu") },
            text = { Text("Ziur zaude eskaera osoa ezabatu nahi duzula?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val eskaeraId = hautatutakoEskaera!!.id
                        deleteConfirm = false
                        scope.launch {
                            try {
                                val response = ApiClient.apiService.ezabatuEskaera(eskaeraId)
                                if (response.code == 200) {
                                    Toast.makeText(context, "Eskaera ezabatu da", Toast.LENGTH_SHORT).show()
                                    hautatutakoEskaera = null
                                    produktuak = emptyList()
                                    kargatuEskaerak()
                                } else {
                                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("EskaeraIkusi", "Ezabatze errorea", e)
                                Toast.makeText(context, "Ezin da ezabatu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Bai") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = false }) { Text("Ez") }
            }
        )
    }
}

@Composable
private fun EskaeraEditatuScreen(
    eskaera: EskaeraDTO,
    produktuak: List<EskaeraLortuDTO>,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var kategoriak by remember { mutableStateOf<List<Kategoria>>(emptyList()) }
    var kategoriaAktiboa by remember { mutableStateOf<Kategoria?>(null) }
    var katalogoProduktuak by remember { mutableStateOf<List<Produktua>>(emptyList()) }

    var editatutako by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var hautatutakoItem by remember { mutableStateOf<OrderItem?>(null) }
    var komensalak by remember { mutableStateOf(eskaera.komensalak) }

    LaunchedEffect(Unit) {
        val grouped = produktuak.groupBy { it.produktuaId }.map { (id, items) ->
            val first = items.first()
            val total = items.sumOf { it.kantitatea }
            OrderItem(
                produktua = Produktua(
                    id = id,
                    izena = first.produktuaIzena,
                    prezioa = first.prezioUnitarioa
                ),
                kantitatea = total
            )
        }
        editatutako = grouped

        try {
            kategoriak = ApiClient.apiService.getKategoriak()
        } catch (e: Exception) {
            Log.e("EskaeraEditatu", "Kategoriak kargatzean errorea", e)
            Toast.makeText(context, "Ezin dira kategoriak kargatu", Toast.LENGTH_SHORT).show()
        }
    }

    val guztira = remember(editatutako) {
        editatutako.sumOf { it.produktua.prezioa * it.kantitatea }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Eskaera #${eskaera.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = onBack) { Text("Itzuli") }
        }
        Text("Mahaia: ${eskaera.mahaiaId} · Data: ${eskaera.data}")
        Text("Sukaldea: ${eskaera.sukaldeaEgoera}")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = komensalak.toString(),
            onValueChange = { value ->
                komensalak = value.filter { it.isDigit() }.toIntOrNull() ?: 0
            },
            label = { Text("Komensalak") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    if (editatutako.isEmpty()) {
                        item { Text("Ez dago produkturik") }
                    } else {
                        items(editatutako) { item ->
                            val selected = hautatutakoItem?.produktua?.id == item.produktua.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (selected) Color(0xFFB3E5FC) else Color.Transparent)
                                    .clickable { hautatutakoItem = item }
                                    .padding(4.dp)
                            ) {
                                Text("• ${item.produktua.izena} x${item.kantitatea}")
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
                    gridItems(katalogoProduktuak) { producto ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .clickable {
                                    val existing = editatutako.firstOrNull { it.produktua.id == producto.id }
                                    editatutako = if (existing == null) {
                                        editatutako + OrderItem(producto, 1)
                                    } else {
                                        editatutako.map {
                                            if (it.produktua.id == producto.id) it.copy(kantitatea = it.kantitatea + 1) else it
                                        }
                                    }
                                    hautatutakoItem = null
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
                                        katalogoProduktuak = ApiClient.apiService.getProduktuakByKategoria(kategoria.id)
                                    } catch (e: Exception) {
                                        Log.e("EskaeraEditatu", "Produktuak kargatzean errorea", e)
                                        Toast.makeText(context, "Ezin dira produktuak kargatu", Toast.LENGTH_SHORT).show()
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

            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(0.6f)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Guztira: ${String.format(Locale.getDefault(), "%.2f", guztira)} €", fontWeight = FontWeight.Bold)

                Button(
                    onClick = {
                        val item = hautatutakoItem
                        if (item != null) {
                            if (item.kantitatea > 1) {
                                editatutako = editatutako.map {
                                    if (it.produktua.id == item.produktua.id) it.copy(kantitatea = it.kantitatea - 1) else it
                                }
                            } else {
                                editatutako = editatutako.filter { it.produktua.id != item.produktua.id }
                            }
                            hautatutakoItem = null
                        } else if (editatutako.isNotEmpty()) {
                            val last = editatutako.last()
                            editatutako = if (last.kantitatea > 1) {
                                editatutako.dropLast(1) + last.copy(kantitatea = last.kantitatea - 1)
                            } else {
                                editatutako.dropLast(1)
                            }
                        }
                    },
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
                                val payload = EskaeraEguneratuRequest(
                                    komensalak = komensalak,
                                    produktuak = editatutako.map {
                                        EskaeraProduktuaEditatuRequest(
                                            produktuaId = it.produktua.id,
                                            kantitatea = it.kantitatea
                                        )
                                    }
                                )
                                val response = ApiClient.apiService.eguneratuEskaera(eskaera.id, payload)
                                if (response.code == 200) {
                                    Toast.makeText(context, "Eskaera eguneratu da", Toast.LENGTH_SHORT).show()
                                    onSaved()
                                } else {
                                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("EskaeraEditatu", "Eguneratze errorea", e)
                                Toast.makeText(context, "Ezin da eguneratu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Gorde aldaketak")
                }
            }
        }
    }
}
