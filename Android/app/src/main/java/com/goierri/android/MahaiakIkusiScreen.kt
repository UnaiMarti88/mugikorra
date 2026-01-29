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

    fun kargatuMahaiak() {
        scope.launch {
            loading = true
            try {
                val response = ApiClient.apiService.getMahaiLibre()
                if (response.code == 200) {
                    mahaiLibreak = response.datuak ?: emptyList()
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mahaiLibreak) { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🪑", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mahaia ${m.zenbakia}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("Libre", fontSize = 12.sp, color = Color(0xFFC8E6C9))
                        }
                    }
                }
            }
        }
    }
}
