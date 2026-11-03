package cl.duoc.rinconsur.viewmodels.logic

import cl.duoc.rinconsur.camera.DecodificadorQrZxing
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifica las rotaciones de la capa de luminancia usadas por CameraX. */
class DecodificadorQrZxingTest {
    private val gris = byteArrayOf(0, 1, 2, 3, 4, 5) // imagen de 3 x 2

    @Test fun rotacionDeNoventaGrados() {
        val (datos, ancho, alto) = DecodificadorQrZxing.rotarGrises(gris, 3, 2, 90)
        assertEquals(2, ancho)
        assertEquals(3, alto)
        assertArrayEquals(byteArrayOf(3, 0, 4, 1, 5, 2), datos)
    }

    @Test fun rotacionDeCientoOchentaGrados() {
        val (datos, ancho, alto) = DecodificadorQrZxing.rotarGrises(gris, 3, 2, 180)
        assertEquals(3, ancho)
        assertEquals(2, alto)
        assertArrayEquals(byteArrayOf(5, 4, 3, 2, 1, 0), datos)
    }

    @Test fun rotacionDeDoscientosSetentaGrados() {
        val (datos, ancho, alto) = DecodificadorQrZxing.rotarGrises(gris, 3, 2, 270)
        assertEquals(2, ancho)
        assertEquals(3, alto)
        assertArrayEquals(byteArrayOf(2, 5, 1, 4, 0, 3), datos)
    }
}
