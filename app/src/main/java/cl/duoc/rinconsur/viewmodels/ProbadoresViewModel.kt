package cl.duoc.rinconsur.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsur.repository.data.AppDatabase
import cl.duoc.rinconsur.repository.ProbadoresRepository
import cl.duoc.rinconsur.viewmodels.logic.ProcesadorQr
import cl.duoc.rinconsur.repository.models.*
import cl.duoc.rinconsur.network.RegistroApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class EstadoGeneral(
    val cabinas: List<Cabina> = emptyList(),
    val turnos: List<Turno> = emptyList(),
    val alertas: List<Alerta> = emptyList(),
    val configuraciones: List<Configuracion> = emptyList(),
    val ahora: Long = System.currentTimeMillis()
) {
    fun cabinasEn(sucursal: String) = cabinas.filter { it.sucursal == sucursal }
    fun turnosEn(sucursal: String) = turnos.filter { it.sucursal == sucursal }
    fun alertasEn(sucursal: String) = alertas.filter { it.sucursal == sucursal && !it.resuelta }
    fun config(sucursal: String) = configuraciones.firstOrNull { it.sucursal == sucursal } ?: Configuracion(sucursal)
}

class ProbadoresViewModel(application: Application) : AndroidViewModel(application) {
    private val bd = AppDatabase.get(application)
    private val repo = ProbadoresRepository(bd.dao())
    private val prefs = application.getSharedPreferences("demo_sesion", 0)
    private val reloj = MutableStateFlow(System.currentTimeMillis())
    val estado = combine(repo.cabinas, repo.turnos, repo.alertas, repo.configuraciones, reloj) {
        c, t, a, s, ahora -> EstadoGeneral(c, t, a, s, ahora)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EstadoGeneral())

    var trabajador by mutableStateOf("Trabajador")
        private set
    var sucursal by mutableStateOf(prefs.getString("sucursal", "Plaza Norte") ?: "Plaza Norte")
        private set
    var ticketId by mutableStateOf<Long?>(null)
        private set
    var clienteHash by mutableStateOf<String?>(null)
        private set
    fun limpiarHistorialCliente() { clienteHash = null }
    fun consultarHistorial(rut: String) {
        if (!Validaciones.rutDemo(rut)) return
        clienteHash = repo.hashRut(rut)
        ticketId = estado.value.turnos.firstOrNull {
            it.rutHash == clienteHash && it.estado in listOf("ACTIVO", "ESPERA")
        }?.id
    }
    var mensaje by mutableStateOf("")
        private set
    var cabinaDetalle by mutableStateOf<Int?>(null)

    init {
        viewModelScope.launch {
            repo.preparar()
            while (isActive) {
                reloj.value = System.currentTimeMillis()
                repo.revisarAlertas()
                delay(2000)
            }
        }
    }

    var qrValidado by mutableStateOf(false)
        private set
    fun cambiarSucursal(nueva: String) {
        if (nueva in SUCURSALES) {
            if (sucursal != nueva) qrValidado = false
            sucursal = nueva
            prefs.edit().putString("sucursal", nueva).apply()
        }
    }
    fun usarQr(texto: String): Boolean {
        val datos = ProcesadorQr.interpretar(texto)
        if (datos != null) cambiarSucursal(datos.sucursal)
        qrValidado = datos != null
        mensaje = if (datos != null) "QR correcto: ${datos.sucursal}. Ya puedes pedir turno."
            else "QR inválido: debe pertenecer a una sucursal de Rincón Sur."
        return datos != null
    }
    fun accesoDemoSinCamara() { qrValidado = true; mensaje = "Modo prueba activado para ${sucursal}. Ya puedes pedir turno." }
    var solicitandoTurno by mutableStateOf(false)
        private set
    var turnoSolicitado by mutableStateOf<Long?>(null)
        private set
    fun consumirTurnoSolicitado() { turnoSolicitado = null }
    fun solicitarTurno(rut: String) {
        if (solicitandoTurno) return
        solicitandoTurno = true
        viewModelScope.launch {
            try {
                if (!qrValidado) { mensaje = "Primero escanea el QR de la sucursal."; return@launch }
                val (id, respuesta) = repo.inscribir(rut, sucursal)
                if (id != null) {
                    clienteHash = repo.hashRut(rut)
                    ticketId = id
                    prefs.edit().putLong("ticket", id).apply()
                    turnoSolicitado = id
                }
                mensaje = respuesta
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mensaje = "No se pudo registrar el turno: ${e.message}"
            } finally { solicitandoTurno = false }
        }
    }
    fun terminarTurno() = ejecutar {
        val id = ticketId ?: return@ejecutar "No hay turno activo."
        val respuesta = repo.finalizarTurno(id)
        ticketId = null
        prefs.edit().remove("ticket").apply()
        respuesta
    }
    fun ayuda(tipo: String) = ejecutar {
        repo.pedirAyuda(ticketId ?: -1L, tipo)
    }
    fun gestionar(numero: Int, accion: String) = ejecutar {
        repo.gestionarCabina(sucursal, numero, accion)
    }
    fun resolver(id: Long) = ejecutar { repo.resolverAlerta(id) }
    fun simular(numero: Int, minutos: Int) = ejecutar {
        val r = repo.simularMinuto(sucursal, numero, minutos)
        repo.revisarAlertas()
        r
    }
    fun cerrarFila() = ejecutar { repo.configurarCierre(sucursal, false, null) }
    fun abrirFila() = ejecutar { repo.configurarCierre(sucursal, true, null) }
    fun programarCierre(minutos: Int) = ejecutar {
        if (minutos !in 1..600) "Ingresa un tiempo de 1 a 600 minutos."
        else repo.configurarCierre(sucursal, true, minutos)
    }
    fun colacion(activar: Boolean) = ejecutar { repo.marcarColacion(sucursal, trabajador, activar) }
    fun probarApi() = ejecutar { RegistroApi.probarConexion() }
    fun prepararDemoFila() = ejecutar { repo.prepararDemoFila(sucursal) }
    fun reiniciar() = ejecutar {
        ticketId = null
        clienteHash = null
        qrValidado = false
        prefs.edit().remove("ticket").apply()
        cabinaDetalle = null
        repo.reiniciar()
    }
    fun informar(texto: String) { mensaje = texto }
    fun borrarMensaje() { mensaje = "" }
    fun salir() {
        clienteHash = null
        ticketId = null
        prefs.edit().remove("ticket").apply()
        mensaje = ""; cabinaDetalle = null
        qrValidado = false
        turnoSolicitado = null
    }

    private fun ejecutar(accion: suspend () -> String) {
        viewModelScope.launch {
            try { mensaje = accion() } catch (ex: CancellationException) { throw ex } catch (ex: Exception) {
                mensaje = "No se pudo completar la operación: ${ex.message ?: "error interno"}"
            }
        }
    }
}
