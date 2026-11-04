package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Turno
import org.junit.Assert.*
import org.junit.Test

class HistorialClienteTest {
    private val turnos = listOf(
        Turno(id = 1, rutHash = "cliente-a", sucursal = "Centro", solicita = 100),
        Turno(id = 2, rutHash = "cliente-b", sucursal = "Centro", solicita = 200),
        Turno(id = 3, rutHash = "cliente-a", sucursal = "Plaza Norte", solicita = 300),
        Turno(id = 4, rutHash = "MANUAL", sucursal = "Centro", solicita = 400)
    )
    @Test fun muestraSoloLosTurnosDelClienteMasRecientesPrimero() {
        assertEquals(listOf(3L, 1L), HistorialCliente.consultar(turnos, "cliente-a").map { it.id })
    }
    @Test fun noMuestraDatosSinIdentificarAlCliente() {
        assertTrue(HistorialCliente.consultar(turnos, null).isEmpty())
        assertTrue(HistorialCliente.consultar(turnos, "").isEmpty())
        assertTrue(HistorialCliente.consultar(turnos, "MANUAL").isEmpty())
    }
    @Test fun clienteSinTurnosNoVeElHistorialDeOtros() {
        assertTrue(HistorialCliente.consultar(turnos, "cliente-c").isEmpty())
    }
}
