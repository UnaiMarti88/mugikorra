package com.goierri.android

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun MahaiakIkusiScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var mahaiLibreak by remember { mutableStateOf<List<MahaiaDTO>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var mahaiKapasitateak by remember { mutableStateOf<Map<Int, Int?>>(emptyMap()) }

    fun kargatuMahaiak() {
        scope.launch {
            loading = true
            try {
                val response = ApiClient.apiService.getMahaiLibre()
                if (response.code == 200) {
                    mahaiLibreak = response.datuak ?: emptyList()

                    // Kargatu mahai bakoitzaren kapazitatea API-ko funtzio espezifikotik
                    val mapa = mutableMapOf<Int, Int?>()
                    for (m in mahaiLibreak) {
                        try {
                            val respKap = ApiClient.apiService.getMahaiKapasitatea(m.id)
                            mapa[m.id] = respKap.datuak?.firstOrNull()
                        } catch (e: Exception) {
                            Log.e("MahaiakIkusi", "Mahai kapazitatea lortzean errorea", e)
                            mapa[m.id] = null
                        }
                    }
                    mahaiKapasitateak = mapa
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MahaiakIkusi", "Mahaiak kargatzean errorea", e)
                Toast.makeText(context, "Ezin dira mahaiak kargatu", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        kargatuMahaiak()
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🪑 Mahai libreak", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextButton(onClick = { kargatuMahaiak() }) {
                Text("🔄 Berritu", fontWeight = FontWeight.Bold)
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mahaiLibreak.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😴", fontSize = 80.sp)
                    Text("Ez dago mahai librerik", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
                }
            }
        } else {
            // Lista de mesas como etiquetas horizontales a lo ancho
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mahaiLibreak) { m ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪑", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Mahaia ${m.zenbakia}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1565C0)
                                    )
                                    val kap = mahaiKapasitateak[m.id]
                                    Text(
                                        text = if (kap != null) "Max kapazitatea: $kap pertsona" else "Max kapazitatea: -",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                            Text(
                                text = "Libre",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}
