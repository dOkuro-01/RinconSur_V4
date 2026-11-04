package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Cabina
import cl.duoc.rinconsur.repository.models.MINUTO
import cl.duoc.rinconsur.repository.models.MAX_MINUTOS

object CalculadoraFila {
    fun espera(cabinas: List<Cabina>, delante: Int, ahora: Long): Long? {
        val duracion = MAX_MINUTOS * MINUTO
        val disponibles = cabinas.filter { it.estado != "REVISION" }.map { cabina ->
            if (cabina.estado == "LIBRE") 0L
            else (duracion - (ahora - (cabina.inicio ?: ahora))).coerceIn(0L, duracion)
        }.sorted()
        if (disponibles.isEmpty()) return null
        val posicion = delante.coerceAtLeast(0)
        val rondas = posicion / disponibles.size
        val siguiente = posicion % disponibles.size
        return rondas * duracion + disponibles[siguiente]
    }
}
