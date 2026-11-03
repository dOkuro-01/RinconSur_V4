package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.DatosQr

 



object ProcesadorQr {
    fun interpretar(texto: String): DatosQr? {
        if (texto.length !in 12..250 || texto.any { it == '\n' || it == '\r' || it == '\u0000' }) return null
        val partes = texto.split('|').map { it.trim() }
        if (partes.firstOrNull()?.uppercase() != "RINCONSUR") return null
        if (partes.size == 2) {
            val sucursal = when (partes[1].lowercase()) {
                "plaza norte" -> "Plaza Norte"
                "centro" -> "Centro"
                "la florida" -> "La Florida"
                else -> return null
            }
            return DatosQr(centroComercial = sucursal, tienda = "Rincón Sur",
                seccion = "Probadores", sucursal = sucursal)
        }
        if (partes.size != 4 || partes.drop(1).any { it.length !in 2..60 }) return null
        val sucursal = when (partes[1].lowercase()) {
            "mall plaza norte", "plaza norte" -> "Plaza Norte"
            "mall centro", "centro" -> "Centro"
            "mall la florida", "la florida" -> "La Florida"
            else -> return null 
        }
        return DatosQr(centroComercial = partes[1], tienda = partes[2],
            seccion = partes[3], sucursal = sucursal)
    }
}
