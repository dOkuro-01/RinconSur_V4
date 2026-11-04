package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Turno

object HistorialCliente {
    fun consultar(turnos: List<Turno>, rutHash: String?): List<Turno> {
        if (rutHash.isNullOrBlank() || rutHash == "MANUAL") return emptyList()
        return turnos.filter { it.rutHash == rutHash }.sortedByDescending { it.solicita }
    }
}
