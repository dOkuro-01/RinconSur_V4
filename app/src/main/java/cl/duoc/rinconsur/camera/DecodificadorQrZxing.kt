package cl.duoc.rinconsur.camera

import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer

/**
 * Decodifica el código QR de los fotogramas YUV que entrega CameraX.
 * ZXing Core funciona localmente: no abre escáneres externos, no usa Play Services
 * y no envía los fotogramas a servidores.
 */
object DecodificadorQrZxing {
    private val pistas = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    fun leer(image: ImageProxy): String? {
        if (image.planes.isEmpty()) return null
        val planoY = image.planes[0]
        val buffer = planoY.buffer.duplicate()
        val ancho = image.width
        val alto = image.height
        if (ancho <= 0 || alto <= 0) return null

        // Copia solo la luminancia Y respetando rowStride y pixelStride (no siempre es ancho).
        val gris = ByteArray(ancho * alto)
        val pasoFila = planoY.rowStride
        val pasoPixel = planoY.pixelStride
        for (fila in 0 until alto) {
            val inicio = fila * pasoFila
            for (columna in 0 until ancho) {
                gris[fila * ancho + columna] = buffer.get(inicio + columna * pasoPixel)
            }
        }

        val (rotado, w, h) = rotarGrises(gris, ancho, alto, image.imageInfo.rotationDegrees)
        val luminancia = PlanarYUVLuminanceSource(rotado, w, h, 0, 0, w, h, false)
        val lector = MultiFormatReader().apply { setHints(pistas) }
        return try {
            try {
                lector.decodeWithState(BinaryBitmap(HybridBinarizer(luminancia))).text
            } catch (_: ReaderException) {
                lector.reset()
                lector.decodeWithState(BinaryBitmap(HybridBinarizer(luminancia.invert()))).text
            }
        } catch (_: ReaderException) {
            null
        } finally {
            lector.reset()
        }
    }

    /** Rota píxeles Y para que ZXing lea correctamente el QR en cualquier orientación. */
    internal fun rotarGrises(datos: ByteArray, ancho: Int, alto: Int, grados: Int): Triple<ByteArray, Int, Int> {
        if (grados == 0) return Triple(datos, ancho, alto)
        val salida = ByteArray(datos.size)
        return when (grados) {
            90 -> {
                for (y in 0 until alto) for (x in 0 until ancho) {
                    salida[x * alto + (alto - 1 - y)] = datos[y * ancho + x]
                }
                Triple(salida, alto, ancho)
            }
            180 -> {
                for (y in 0 until alto) for (x in 0 until ancho) {
                    salida[(alto - 1 - y) * ancho + (ancho - 1 - x)] = datos[y * ancho + x]
                }
                Triple(salida, ancho, alto)
            }
            270 -> {
                for (y in 0 until alto) for (x in 0 until ancho) {
                    salida[(ancho - 1 - x) * alto + y] = datos[y * ancho + x]
                }
                Triple(salida, alto, ancho)
            }
            else -> Triple(datos, ancho, alto)
        }
    }
}
