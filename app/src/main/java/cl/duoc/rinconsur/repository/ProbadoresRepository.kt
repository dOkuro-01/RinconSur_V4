package cl.duoc.rinconsur.repository

import cl.duoc.rinconsur.viewmodels.logic.CalculadoraFila
import cl.duoc.rinconsur.viewmodels.Validaciones
import cl.duoc.rinconsur.repository.data.ProbadoresDao
import cl.duoc.rinconsur.repository.models.*
import cl.duoc.rinconsur.network.RegistroApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest

class ProbadoresRepository(private val dao: ProbadoresDao) {
    val cabinas = dao.cabinasFlow()
    val turnos = dao.turnosFlow()
    val alertas = dao.alertasFlow()
    val configuraciones = dao.configuracionesFlow()
    private val mutex = Mutex()

    suspend fun preparar() = mutex.withLock { prepararInterno() }

    private suspend fun prepararInterno() {
        if (dao.cuentaCabinas() > 0) return
        SUCURSALES.forEach { sucursal ->
            dao.guardarConfiguracion(Configuracion(sucursal))
            (1..10).forEach { numero -> dao.guardarCabina(Cabina(sucursal, numero)) }
        }
    }

    fun hashRut(rut: String): String {
        val normalizado = rut.uppercase().replace(".", "").replace("-", "").replace(" ", "")
        return MessageDigest.getInstance("SHA-256").digest(normalizado.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    suspend fun inscribir(rut: String, sucursal: String): Pair<Long?, String> = mutex.withLock {
        prepararInterno()
        if (sucursal !in SUCURSALES) return@withLock null to "Sucursal no válida."
        if (!Validaciones.rutDemo(rut)) {
            return@withLock null to "Ingresa un RUT de demostración válido en formato 12345678-5."
        }
        val hash = hashRut(rut)
        val turnos = dao.turnos(sucursal)
        val existente = turnos.firstOrNull { it.rutHash == hash && it.estado in listOf("ACTIVO", "ESPERA") }
        if (existente != null) return@withLock existente.id to "Ya tienes un turno vigente."
        val config = dao.config(sucursal) ?: Configuracion(sucursal)
        if (!config.abierta) return@withLock null to "La fila de esta sucursal está cerrada."
        val ahora = System.currentTimeMillis()
        val cabinas = dao.cabinas(sucursal)
        val enFila = turnos.count { it.estado == "ESPERA" }
        val espera = CalculadoraFila.espera(cabinas, enFila, ahora)
            ?: return@withLock null to "No hay probadores disponibles para recibir turnos."
        if (config.cierre != null && ahora + espera + MAX_MINUTOS * MINUTO > config.cierre) {
            return@withLock null to "No quedan cupos antes del cierre programado."
        }
        val id = dao.crearTurno(Turno(rutHash = hash, sucursal = sucursal, solicita = ahora))
        asignarSiguiente(sucursal, ahora)
        RegistroApi.anotar("NUEVO_TURNO", "$sucursal · Turno $id")
        id to "Ingreso registrado en la fila virtual."
    }

    private suspend fun asignarSiguiente(sucursal: String, ahora: Long = System.currentTimeMillis()) {
        val libres = dao.cabinas(sucursal).filter { it.estado == "LIBRE" }.sortedBy { it.numero }
        val pendientes = dao.turnos(sucursal).filter { it.estado == "ESPERA" }
        libres.zip(pendientes).forEach { (cabina, turno) ->
            dao.actualizarTurno(turno.copy(estado = "ACTIVO", cabina = cabina.numero, ingreso = ahora))
            dao.guardarCabina(cabina.copy(estado = "OCUPADO", inicio = ahora, turnoId = turno.id))
        }
    }

    suspend fun finalizarTurno(id: Long): String = mutex.withLock {
        val turno = dao.turno(id) ?: return@withLock "No se encontró el turno."
        if (turno.estado == "ESPERA") {
            dao.actualizarTurno(turno.copy(estado = "CANCELADO", salida = System.currentTimeMillis()))
            return@withLock "Saliste de la fila virtual."
        }
        if (turno.estado != "ACTIVO") return@withLock "El turno ya terminó."
        liberarTurnoInterno(turno, System.currentTimeMillis())
        RegistroApi.anotar("FIN_TURNO", "Turno ${turno.id} · ${turno.sucursal}")
        "Prueba finalizada. El probador está disponible."
    }

    private suspend fun liberarTurnoInterno(turno: Turno, ahora: Long) {
        dao.actualizarTurno(turno.copy(estado = "FINALIZADO", salida = ahora))
        val cabina = dao.cabinas(turno.sucursal).firstOrNull { it.numero == turno.cabina }
        if (cabina != null) {
            dao.guardarCabina(cabina.copy(estado = "LIBRE", inicio = null, turnoId = null))
            dao.resolverAlertasCabina(turno.sucursal, cabina.numero)
        }
        asignarSiguiente(turno.sucursal, ahora)
    }

    suspend fun pedirAyuda(id: Long, tipo: String): String = mutex.withLock {
        val turno = dao.turno(id) ?: return@withLock "No se encontró el turno."
        if (turno.estado != "ACTIVO") return@withLock "Puedes pedir ayuda durante el uso de un probador."
        dao.crearAlerta(Alerta(sucursal = turno.sucursal, cabina = turno.cabina, turnoId = id,
            tipo = "AYUDA", texto = "Probador ${turno.cabina}: solicita $tipo.", creada = System.currentTimeMillis()))
        RegistroApi.anotar("AYUDA", "${turno.sucursal} · Probador ${turno.cabina} · $tipo")
        "Solicitud de ayuda registrada para el trabajador."
    }

    suspend fun gestionarCabina(sucursal: String, numero: Int, accion: String): String = mutex.withLock {
        val cabina = dao.cabinas(sucursal).firstOrNull { it.numero == numero }
            ?: return@withLock "Probador no encontrado."
        val ahora = System.currentTimeMillis()
        when (accion) {
            "LIBRE" -> {
                val t = cabina.turnoId?.let { dao.turno(it) }
                if (t?.estado == "ACTIVO") liberarTurnoInterno(t, ahora)
                else {
                    dao.guardarCabina(cabina.copy(estado = "LIBRE", inicio = null, turnoId = null))
                    dao.resolverAlertasCabina(sucursal, numero)
                    asignarSiguiente(sucursal, ahora)
                }
            }
            "REVISION" -> {
                
                if (cabina.estado != "LIBRE") return@withLock "Libera el probador antes de enviarlo a revisión."
                dao.guardarCabina(cabina.copy(estado = "REVISION"))
                dao.crearAlerta(Alerta(sucursal = sucursal, cabina = numero,
                    turnoId = cabina.turnoId, tipo = "REVISION",
                    texto = "Probador $numero requiere revisión.", creada = ahora))
            }
            "OCUPADO" -> {
                if (cabina.estado != "LIBRE") return@withLock "Primero libera el probador."
                val id = dao.crearTurno(Turno(rutHash = "MANUAL", sucursal = sucursal,
                    solicita = ahora, estado = "ACTIVO", cabina = numero, ingreso = ahora))
                dao.guardarCabina(cabina.copy(estado = "OCUPADO", inicio = ahora, turnoId = id))
            }
            else -> return@withLock "Acción inválida."
        }
        RegistroApi.anotar("PROBADOR_$accion", "$sucursal · $numero")
        "Probador $numero actualizado a $accion."
    }

    suspend fun revisarAlertas(): Unit = mutex.withLock {
        val ahora = System.currentTimeMillis()
        for (cabina in SUCURSALES.flatMap { dao.cabinas(it) }) {
            val inicio = cabina.inicio ?: continue
            val turnoId = cabina.turnoId ?: continue
            if (cabina.estado != "OCUPADO") continue
            val transcurrido = ahora - inicio
            if (transcurrido >= ALERTA_MINUTOS * MINUTO && dao.alertaPorTurno(turnoId, "AVISO_10") == null) {
                dao.crearAlerta(Alerta(sucursal = cabina.sucursal, cabina = cabina.numero,
                    turnoId = turnoId, tipo = "AVISO_10",
                    texto = "Probador ${cabina.numero}: 10 minutos de uso. Revisar pronto.", creada = ahora))
            }
            if (transcurrido >= MAX_MINUTOS * MINUTO && dao.alertaPorTurno(turnoId, "EXCESO_12") == null) {
                dao.crearAlerta(Alerta(sucursal = cabina.sucursal, cabina = cabina.numero,
                    turnoId = turnoId, tipo = "EXCESO_12",
                    texto = "Probador ${cabina.numero}: superó los 12 minutos. Requiere atención.", creada = ahora))
            }
        }
    }

    suspend fun resolverAlerta(id: Long): String = mutex.withLock {
        
        val alerta = dao.alertasNoResueltas().firstOrNull { it.id == id }
            ?: return@withLock "La alerta ya no está pendiente."
        dao.actualizarAlerta(alerta.copy(resuelta = true))
        "Alerta marcada como atendida."
    }

    suspend fun configurarCierre(sucursal: String, abierto: Boolean, minutosRestantes: Int?): String = mutex.withLock {
        val viejo = dao.config(sucursal) ?: Configuracion(sucursal)
        val cierre = if (abierto && minutosRestantes != null && minutosRestantes > 0)
            System.currentTimeMillis() + minutosRestantes * MINUTO else null
        dao.guardarConfiguracion(viejo.copy(abierta = abierto, cierre = cierre))
        if (!abierto) "Fila cerrada: no entrarán clientes nuevos. Los turnos actuales continúan."
        else if (cierre != null) "Cierre programado. Se rechazarán turnos que no alcancen antes del cierre."
        else "Fila abierta nuevamente."
    }

    suspend fun marcarColacion(sucursal: String, trabajador: String, activar: Boolean): String = mutex.withLock {
        val viejo = dao.config(sucursal) ?: Configuracion(sucursal)
        dao.guardarConfiguracion(viejo.copy(colacion = activar))
        if (activar) {
            dao.crearAlerta(Alerta(sucursal = sucursal, tipo = "COLACION",
                texto = "$trabajador inició colación. Se requiere cobertura de otro compañero (aviso simulado al jefe).",
                creada = System.currentTimeMillis()))
        }
        if (activar) "Colación registrada. El aviso aparece en notificaciones."
        else "Volviste de colación."
    }

    suspend fun simularMinuto(sucursal: String, numero: Int, minutos: Int): String = mutex.withLock {
        val cabina = dao.cabinas(sucursal).firstOrNull { it.numero == numero }
            ?: return@withLock "Probador no encontrado."
        if (cabina.estado == "LIBRE") return@withLock "Primero ocupa el probador para simular."
        val inicio = System.currentTimeMillis() - minutos * MINUTO
        dao.guardarCabina(cabina.copy(inicio = inicio))
        val turno = cabina.turnoId?.let { dao.turno(it) }
        if (turno != null) dao.actualizarTurno(turno.copy(ingreso = inicio))
        "Tiempo de uso simulado: $minutos minutos."
    }

     
    suspend fun prepararDemoFila(sucursal: String): String = mutex.withLock {
        prepararInterno()
        val ahora = System.currentTimeMillis()
        val libres = dao.cabinas(sucursal).filter { it.estado == "LIBRE" }
        libres.forEach { cabina ->
            val id = dao.crearTurno(Turno(rutHash = "MANUAL", sucursal = sucursal,
                solicita = ahora, estado = "ACTIVO", cabina = cabina.numero, ingreso = ahora))
            dao.guardarCabina(cabina.copy(estado = "OCUPADO", turnoId = id, inicio = ahora))
        }
        "Demo: ${libres.size} probadores ocupados. Ahora puedes solicitar un turno como cliente."
    }

    suspend fun reiniciar(): String = mutex.withLock {
        dao.borrarAlertas(); dao.borrarTurnos(); dao.borrarCabinas(); dao.borrarConfiguraciones()
        prepararInterno()
        "Demostración reiniciada."
    }
}
