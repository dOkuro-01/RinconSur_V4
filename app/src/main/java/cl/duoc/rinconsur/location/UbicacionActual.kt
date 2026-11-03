package cl.duoc.rinconsur.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import cl.duoc.rinconsur.repository.models.Coordenadas

/**
 * Recurso nativo 2: LocationManager del propio Android (sin Google Play Services).
 * Hace una sola solicitud a NETWORK_PROVIDER con permiso aproximado.
 * El QR se puede guardar aunque no haya ubicación o permiso.
 */
fun obtenerUbicacionActual(
    contexto: Context,
    alObtener: (Coordenadas) -> Unit,
    alFallar: (String) -> Unit
) {
    if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED) {
        alFallar("No hay permiso de ubicación aproximada. Puedes guardar sin coordenadas.")
        return
    }
    val servicio = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (servicio == null) {
        alFallar("Servicio de ubicación no disponible. Puedes guardar sin coordenadas.")
        return
    }
    try {
        if (!servicio.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            alFallar("Activa Ubicación en Android para obtener coordenadas aproximadas, o guarda sin ellas.")
            return
        }
        // Se usa android.os.CancellationSignal de forma explícita.
        // AndroidX también ofrece una sobrecarga con androidx.core.os.CancellationSignal:
        // al pasar null sin tipo, Kotlin no puede elegir entre ambas.
        val cancelacion = android.os.CancellationSignal()
        @Suppress("MissingPermission")
        LocationManagerCompat.getCurrentLocation(
            servicio,
            LocationManager.NETWORK_PROVIDER,
            cancelacion,
            ContextCompat.getMainExecutor(contexto)
        ) { ubicacion: android.location.Location? ->
            if (ubicacion != null) alObtener(Coordenadas(ubicacion.latitude, ubicacion.longitude))
            else alFallar("No se pudo obtener ubicación. Puedes guardar sin coordenadas.")
        }
    } catch (_: SecurityException) {
        alFallar("Permiso de ubicación denegado. Puedes guardar sin coordenadas.")
    } catch (_: Exception) {
        alFallar("No se pudo consultar la ubicación. Puedes guardar sin coordenadas.")
    }
}
