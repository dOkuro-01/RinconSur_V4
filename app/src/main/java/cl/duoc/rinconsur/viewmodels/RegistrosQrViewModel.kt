package cl.duoc.rinconsur.viewmodels

import android.app.Application
import cl.duoc.rinconsur.location.obtenerUbicacionActual
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsur.repository.RegistrosQrRepository
import cl.duoc.rinconsur.repository.data.AppDatabase
import cl.duoc.rinconsur.repository.models.DatosQr
import cl.duoc.rinconsur.repository.models.Coordenadas
import cl.duoc.rinconsur.viewmodels.logic.ProcesadorQr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RegistrosQrUiState(
    val actual: DatosQr? = null,
    val fecha: Long? = null,
    val ubicacion: Coordenadas? = null,
    val guardado: Boolean = false,
    val guardando: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val filtro: String = "",
    val editando: Long? = null,
    val seccion: String = "",
    val seccionError: String? = null,
    val mensaje: String? = null
)

 
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrosQrViewModel(application: Application) : AndroidViewModel(application) {
    private var puedeGestionar = false
    fun permitirGestion(permitir: Boolean) {
        puedeGestionar = permitir
        if (!permitir) cancelarEdicion()
    }
    private val repo = RegistrosQrRepository(AppDatabase.get(application).registroQrDao())
    private val _uiState = MutableStateFlow(RegistrosQrUiState())
    val uiState = _uiState.asStateFlow()
    private val filtro = MutableStateFlow("")
    val registros = filtro.flatMapLatest {
        if (it.isBlank()) repo.todos else repo.buscar(it.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun leer(texto: String): Boolean {
        if (_uiState.value.guardando) return false
        val datos = ProcesadorQr.interpretar(texto)
        _uiState.update { it.copy(actual = datos, fecha = datos?.let { System.currentTimeMillis() },
            ubicacion = null, guardado = false,
            mensaje = if (datos == null) "QR no válido." else "QR real leído desde CameraX. Puedes añadir ubicación antes de guardar.") }
        return datos != null
    }
    /** Evita asociar coordenadas a otro escaneo si el usuario vuelve a abrir la cámara. */
    fun registrarUbicacion(punto: Coordenadas, fechaDelQr: Long?) {
        _uiState.update { estado ->
            if (estado.actual != null && estado.fecha == fechaDelQr && !estado.guardado && !estado.guardando)
                estado.copy(ubicacion = punto, mensaje = "Ubicación aproximada obtenida correctamente.")
            else estado
        }
    }
    fun informar(mensaje: String) = _uiState.update { it.copy(mensaje = mensaje) }

    fun solicitarUbicacion() {
        val s = _uiState.value
        if (s.actual == null || s.guardado || s.guardando || s.obteniendoUbicacion) return
        _uiState.update { it.copy(obteniendoUbicacion = true, mensaje = "Obteniendo ubicación…") }
        viewModelScope.launch {
            try {
                val punto = obtenerUbicacionActual(getApplication())
                registrarUbicacion(punto, s.fecha)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (_uiState.value.fecha == s.fecha)
                    informar(e.message ?: "No pudimos obtener tu ubicación. Puedes continuar sin coordenadas.")
            } finally { _uiState.update { it.copy(obteniendoUbicacion = false) } }
        }
    }

    fun guardar() {
        val s = _uiState.value
        val datos = s.actual ?: return
        if (s.guardando || s.guardado) return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                val id = repo.crear(datos, s.fecha ?: System.currentTimeMillis(), s.ubicacion)
                _uiState.update { actual ->
                    
                    if (actual.actual == datos && actual.fecha == s.fecha)
                        actual.copy(guardado = true, mensaje = "Escaneo #$id guardado en SQLite.")
                    else actual
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "No se pudo guardar: ${e.message}") }
            } finally { _uiState.update { it.copy(guardando = false) } }
        }
    }
    fun buscar(texto: String) {
        filtro.value = texto
        _uiState.update { it.copy(filtro = texto) }
    }
    fun editar(id: Long, seccion: String) {
        if (!puedeGestionar) return
        _uiState.update {
        it.copy(editando = id, seccion = seccion, seccionError = null)
        }
    }
    fun onSeccionChange(valor: String) = _uiState.update {
        it.copy(seccion = valor.take(65), seccionError = null)
    }
    fun cancelarEdicion() = _uiState.update { it.copy(editando = null, seccionError = null) }
    fun guardarSeccion() {
        if (!puedeGestionar) return
        val s = _uiState.value
        val id = s.editando ?: return
        if (!Validaciones.seccionValida(s.seccion)) {
            _uiState.update { it.copy(seccionError = "La sección debe tener entre 2 y 60 caracteres.") }
            return
        }
        operar {
            if (repo.actualizarSeccion(id, s.seccion)) {
                cancelarEdicion()
                "Sección actualizada correctamente."
            } else "No se encontró el registro QR."
        }
    }
    fun eliminar(id: Long) = operar {
        if (!puedeGestionar) return@operar "No tienes permiso para eliminar registros."
        if (repo.eliminar(id)) {
            if (_uiState.value.editando == id) cancelarEdicion()
            "Registro QR eliminado."
        } else "No se encontró el registro QR."
    }
    fun limpiar() {
        _uiState.value = RegistrosQrUiState(guardando = _uiState.value.guardando)
        filtro.value = ""
    }
    private fun operar(accion: suspend () -> String) {
        viewModelScope.launch {
            try {
                val mensaje = accion()
                _uiState.update { it.copy(mensaje = mensaje) }
            }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(mensaje = "No se pudo completar: ${e.message}") } }
        }
    }
}
