package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Cabina
import cl.duoc.rinconsur.repository.models.MINUTO
import cl.duoc.rinconsur.repository.models.MAX_MINUTOS
import java.util.PriorityQueue

 


object CalculadoraFila {
    fun espera(cabinas: List<Cabina>, delante: Int, ahora: Long): Long? {
        val agenda = PriorityQueue<Long>()
        cabinas.filter { it.estado != "REVISION" }.forEach { c ->
            agenda.add(if (c.estado == "LIBRE") 0L else {
                (MAX_MINUTOS * MINUTO - (ahora - (c.inicio ?: ahora))).coerceAtLeast(0)
            })
        }
        if (agenda.isEmpty()) return null
        repeat(delante.coerceAtLeast(0)) {
            
            agenda.add(agenda.remove() + MAX_MINUTOS * MINUTO)
        }
        return agenda.peek()
    }
}
