package cl.duoc.rinconsur.viewmodels

import org.junit.Assert.*
import org.junit.Test

class ValidacionesTest {
    @Test fun cierreRespetaLosLimitesYRechazaTexto() {
        assertTrue(Validaciones.minutosCierreValidos("1"))
        assertTrue(Validaciones.minutosCierreValidos("600"))
        listOf("", "0", "601", "-1", "abc", "99999999999999999999").forEach {
            assertFalse("No debe aceptar: $it", Validaciones.minutosCierreValidos(it))
        }
    }
    @Test fun seccionRespetaLongitudYEspacios() {
        assertTrue(Validaciones.seccionValida("ab"))
        assertTrue(Validaciones.seccionValida("a".repeat(60)))
        assertFalse(Validaciones.seccionValida("a"))
        assertFalse(Validaciones.seccionValida("a".repeat(61)))
    }
    @Test fun camposObligatoriosRechazanEspacios() {
        assertEquals("Requerido", Validaciones.campoObligatorio("  ", "Requerido"))
        assertNull(Validaciones.campoObligatorio("trabajador", "Requerido"))
    }
    @Test fun rutDemoConFormato() {
        assertTrue(Validaciones.rutDemo("12.345.678-5"))
        assertFalse(Validaciones.rutDemo("hola"))
    }
    @Test fun noAceptaSeccionesVacias() {
        assertTrue(Validaciones.seccionValida("Hombre"))
        assertFalse(Validaciones.seccionValida(" "))
    }
}
