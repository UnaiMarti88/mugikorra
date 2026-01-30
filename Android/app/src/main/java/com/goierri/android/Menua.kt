package com.goierri.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
    ) {
        // Panel del menú lateral (no cierra al hacer clic dentro)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(260.dp)
                .background(Color(0xFF0F4C75))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera del menú centrada
            Text(
                text = "MENUA",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0x33FFFFFF))
            Spacer(modifier = Modifier.height(16.dp))

            MenuItem(
                icon = "🧾",
                label = "Eskaera egin",
                onClick = onEskaeraEginClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuItem(
                icon = "👀",
                label = "Eskaera ikusi",
                onClick = onEskaeraIkusiClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuItem(
                icon = "🪑",
                label = "Mahiak ikusi",
                onClick = onMahiakIkusiClick
            )

            Spacer(modifier = Modifier.weight(1f))

            MenuItem(
                icon = "🚪",
                label = "Saioa itxi",
                onClick = onLogoutClick,
                highlightColor = Color(0xFFE53935)
            )
        }

        // Zona oscura a la derecha que sí cierra el menú al hacer clic
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onCloseMenu() }
        ) {}
    }
}

@Composable
private fun MenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit,
    highlightColor: Color = Color(0xFF1B6CA8)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(highlightColor.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

