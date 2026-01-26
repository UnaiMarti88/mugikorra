package com.goierri.android

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mahai libreak", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = { kargatuMahaiak() }) {
                Text("Berritu")
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp)
        ) {
            if (mahaiLibreak.isEmpty()) {
                item { Text("Ez dago mahai librerik") }
            } else {
                items(mahaiLibreak) { m ->
                    Text("• Mahaia ${m.zenbakia}", modifier = Modifier.padding(6.dp))
                }
            }
        }
    }
}
