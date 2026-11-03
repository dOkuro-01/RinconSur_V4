package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Cabina
import cl.duoc.rinconsur.repository.models.MINUTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadoraFilaTest {
    @Test fun probadorLibreAtiendeSinEspera() {
        assertEquals(0L, CalculadoraFila.espera(listOf(Cabina("Plaza Norte", 1)), 0, 100_000L))
    }
    @Test fun calculaColaDeDoceMinutos() {
        assertEquals(12L * MINUTO, CalculadoraFila.espera(listOf(Cabina("Plaza Norte", 1)), 1, 100_000L))
    }
    @Test fun probadoresEnRevisionNoCuentan() {
        assertNull(CalculadoraFila.espera(listOf(Cabina("Plaza Norte", 1, "REVISION")), 0, 100_000L))
    }
    @Test fun restaTiempoYaOcupado() {
        val ahora = 800_000L
        assertEquals(2L * MINUTO, CalculadoraFila.espera(
            listOf(Cabina("Plaza Norte", 1, "OCUPADO", ahora - 10 * MINUTO)), 0, ahora))
    }
}
