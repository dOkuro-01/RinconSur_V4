package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.repository.models.Cabina
import cl.duoc.rinconsur.repository.models.MINUTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculadoraFilaTest {
    @Test fun distribuyeLaFilaEnRondasConCabinasMixtas() {
        val ahora = 1_000_000L
        val cabinas = listOf(
            Cabina("Centro", 1),
            Cabina("Centro", 2, "OCUPADO", ahora - 10 * MINUTO),
            Cabina("Centro", 3, "REVISION")
        )
        assertEquals(0L, CalculadoraFila.espera(cabinas, 0, ahora))
        assertEquals(2 * MINUTO, CalculadoraFila.espera(cabinas, 1, ahora))
        assertEquals(12 * MINUTO, CalculadoraFila.espera(cabinas, 2, ahora))
        assertEquals(14 * MINUTO, CalculadoraFila.espera(cabinas, 3, ahora))
    }
    @Test fun sinCabinasNoHayCupos() {
        assertNull(CalculadoraFila.espera(emptyList(), 0, 100_000L))
    }
    @Test fun unaCabinaConTiempoCumplidoNoDaEsperaNegativa() {
        val ahora = 2_000_000L
        assertEquals(0L, CalculadoraFila.espera(
            listOf(Cabina("Centro", 1, "OCUPADO", ahora - 20 * MINUTO)), 0, ahora))
    }
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
