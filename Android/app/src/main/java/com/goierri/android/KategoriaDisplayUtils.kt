package com.goierri.android

// Devuelve un nombre de categoría más legible para mostrar en los botones
fun getKategoriaDisplayName(izena: String): String {
    return when (izena) {
        "kafeak" -> "Kafeak"
        "lehen_platerak" -> "Lehen platerak"
        "bigarren_platerak" -> "Bigarren platerak"
        "pintxoak" -> "Pintxoak"
        "postreak" -> "Postreak"
        "razioak" -> "Razioak"
        "edariak_alkohol" -> "Edariak (alkohol)"
        "edariak_sinalkohol" -> "Edariak (alkohol gabe)"
        else -> {
            // Por defecto, sustituimos guiones bajos por espacios y capitalizamos la primera letra
            izena.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
