package cl.duoc.rinconsur.viewmodels.logic

import org.junit.Assert.*
import org.junit.Test

class ProcesadorQrTest {
    @Test fun interpretaQrConCentroTiendaSeccion() {
        val dato = ProcesadorQr.interpretar("RINCONSUR|Mall Plaza Norte|Falabella|Hombre")
        assertNotNull(dato)
        assertEquals("Mall Plaza Norte", dato!!.centroComercial)
        assertEquals("Falabella", dato.tienda)
        assertEquals("Hombre", dato.seccion)
        assertEquals("Plaza Norte", dato.sucursal)
    }

    @Test fun mantieneCompatibilidadConQrOriginal() {
        val dato = ProcesadorQr.interpretar("RINCONSUR|Plaza Norte")
        assertEquals("Plaza Norte", dato?.sucursal)
        assertEquals("Probadores", dato?.seccion)
    }

    @Test fun rechazaQrIncompletosOAjenos() {
        assertNull(ProcesadorQr.interpretar("URL|Mall Plaza Norte|Falabella|Hombre"))
        assertNull(ProcesadorQr.interpretar("RINCONSUR|Mall Plaza Norte||Hombre"))
        assertNull(ProcesadorQr.interpretar("RINCONSUR|Mall No Existente|Falabella|Hombre"))
    }
}
