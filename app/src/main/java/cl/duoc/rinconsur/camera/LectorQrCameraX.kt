package cl.duoc.rinconsur.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.duoc.rinconsur.viewmodels.logic.ProcesadorQr
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Vista previa y análisis QR nativos DENTRO de la app mediante AndroidX CameraX.
 * No se lanza Google Code Scanner ni otra aplicación de cámara.
 */
@Composable
fun LectorQrCameraX(
    onQrValido: (String) -> Unit,
    onMensaje: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val callbackQr by rememberUpdatedState(onQrValido)
    val callbackMensaje by rememberUpdatedState(onMensaje)
    val vista = remember(contexto) {
        PreviewView(contexto).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, vista) {
        val ejecutorPrincipal = ContextCompat.getMainExecutor(contexto)
        val ejecutorAnalisis = Executors.newSingleThreadExecutor()
        val cerrado = AtomicBoolean(false)
        val entregado = AtomicBoolean(false)
        val ultimoAviso = AtomicLong(0L)
        val proveedorFuturo = ProcessCameraProvider.getInstance(contexto)
        var proveedor: ProcessCameraProvider? = null
        var analizador: ImageAnalysis? = null

        proveedorFuturo.addListener({
            if (!cerrado.get()) {
                try {
                    val camara = proveedorFuturo.get()
                    proveedor = camara
                    val previa = Preview.Builder().build().also {
                        it.setSurfaceProvider(vista.surfaceProvider)
                    }
                    val lectura = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analizador = lectura
                    lectura.setAnalyzer(ejecutorAnalisis) { imagen ->
                        try {
                            if (!cerrado.get() && !entregado.get()) {
                                val texto = DecodificadorQrZxing.leer(imagen)
                                if (texto != null) {
                                    if (ProcesadorQr.interpretar(texto) != null) {
                                        if (entregado.compareAndSet(false, true)) {
                                            ejecutorPrincipal.execute {
                                                if (!cerrado.get()) callbackQr(texto)
                                            }
                                        }
                                    } else {
                                        val ahora = System.currentTimeMillis()
                                        val anterior = ultimoAviso.get()
                                        if (ahora - anterior > 2_000L && ultimoAviso.compareAndSet(anterior, ahora)) {
                                            ejecutorPrincipal.execute {
                                                if (!cerrado.get()) callbackMensaje("QR detectado, pero no pertenece a Rincón Sur o su sucursal no está registrada.")
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            // Un fotograma defectuoso no debe cerrar la cámara ni la sesión.
                        } finally {
                            imagen.close() // OBLIGATORIO: libera el fotograma para el siguiente análisis.
                        }
                    }
                    camara.unbindAll()
                    camara.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, previa, lectura)
                } catch (ex: Exception) {
                    if (!cerrado.get()) callbackMensaje("No se pudo iniciar CameraX: ${ex.localizedMessage ?: "cámara no disponible"}")
                }
            }
        }, ejecutorPrincipal)

        onDispose {
            cerrado.set(true)
            analizador?.clearAnalyzer()
            proveedor?.unbindAll()
            ejecutorAnalisis.shutdown()
        }
    }

    AndroidView(factory = { vista }, modifier = modifier)
}
