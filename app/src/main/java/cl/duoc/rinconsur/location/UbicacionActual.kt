package cl.duoc.rinconsur.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import cl.duoc.rinconsur.repository.models.Coordenadas
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

suspend fun obtenerUbicacionActual(contexto: Context): Coordenadas {
    if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED) {
        throw IllegalStateException("Autoriza la ubicación para obtener coordenadas. Puedes continuar sin ellas.")
    }
    val servicio = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (servicio == null || !LocationManagerCompat.isLocationEnabled(servicio)) {
        throw IllegalStateException("Activa la ubicación del teléfono o continúa sin coordenadas.")
    }
    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(contexto) != ConnectionResult.SUCCESS) {
        throw IllegalStateException("El servicio de ubicación no está disponible. Puedes continuar sin coordenadas.")
    }
    val cliente = LocationServices.getFusedLocationProviderClient(contexto)
    val cancelacion = CancellationTokenSource()
    try {
        val resultado = withTimeoutOrNull(15_000L) {
            cliente.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancelacion.token).await()
        } ?: throw IllegalStateException("No pudimos obtener tu ubicación. Intenta nuevamente o continúa sin coordenadas.")
        return Coordenadas(resultado.latitude, resultado.longitude)
    } catch (e: CancellationException) {
        throw e
    } catch (e: SecurityException) {
        throw IllegalStateException("El permiso de ubicación fue retirado. Puedes continuar sin coordenadas.")
    } finally {
        cancelacion.cancel()
    }
}
