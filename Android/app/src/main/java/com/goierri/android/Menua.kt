package com.goierri.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Menua(
    menuAbierto: Boolean,
    onCloseMenu: () -> Unit,
    onEskaeraEginClick: () -> Unit,
    onEskaeraIkusiClick: () -> Unit,
    onMahiakIkusiClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    if (!menuAbierto) return

    // Capa semitransparente que detecta clics fuera del menú
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onCloseMenu() } // clic fuera cierra el menú
    ) {
        // Menú lateral
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(250.dp)
                .background(Color(0xFF1565C0))
                .padding(16.dp)
                .clickable(enabled = false) {}, // evita que clics dentro cierren el menú
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onEskaeraEginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Eskaera Egin", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEskaeraIkusiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Eskaera Ikusi", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onMahiakIkusiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Mahiak Ikusi", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Saioa itxi", fontSize = 16.sp)
            }
        }
    }
}
