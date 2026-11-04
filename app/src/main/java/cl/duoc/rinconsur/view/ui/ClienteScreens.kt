package cl.duoc.rinconsur.view.ui

import androidx.compose.foundation.layout.*
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import cl.duoc.rinconsur.location.obtenerUbicacionActual
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.rinconsur.viewmodels.logic.CalculadoraFila
import cl.duoc.rinconsur.viewmodels.logic.HistorialCliente
import cl.duoc.rinconsur.repository.models.*
import cl.duoc.rinconsur.view.utils.*
import cl.duoc.rinconsur.view.theme.*
import cl.duoc.rinconsur.viewmodels.EstadoGeneral
import cl.duoc.rinconsur.viewmodels.ProbadoresViewModel
import cl.duoc.rinconsur.viewmodels.FormulariosViewModel
import cl.duoc.rinconsur.viewmodels.RegistrosQrViewModel
import cl.duoc.rinconsur.view.utils.AnimacionEntradaFormulario

 
@Composable
fun ClienteInicioScreen(vm: ProbadoresViewModel, formularios: FormulariosViewModel, qrVm: RegistrosQrViewModel, estado: EstadoGeneral, verMiTurno: () -> Unit, verHistorialQr: () -> Unit, abrirCamara: () -> Unit) {
    val formulario by formularios.uiState.collectAsState()
    val qrState by qrVm.uiState.collectAsState()
    val contexto = LocalContext.current
    var permisoUbicacionDenegado by rememberSaveable { mutableStateOf(false) }

    val solicitarPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { autorizado ->
        permisoUbicacionDenegado = !autorizado
        if (autorizado) qrVm.solicitarUbicacion()
        else qrVm.informar("Ubicación denegada. Puedes guardar el QR sin coordenadas.")
    }
    val cabinas = estado.cabinasEn(vm.sucursal)
    val configuracion = estado.config(vm.sucursal)
    val libres = cabinas.count { it.estado == "LIBRE" }
    val fila = estado.turnosEn(vm.sucursal).count { it.estado == "ESPERA" }
    val espera = CalculadoraFila.espera(cabinas, fila, estado.ahora)
    val ticket = estado.turnos.firstOrNull { it.id == vm.ticketId && it.estado in listOf("ESPERA", "ACTIVO") }
    LaunchedEffect(vm.turnoSolicitado) {
        if (vm.turnoSolicitado != null) {
            vm.consumirTurnoSolicitado()
            verMiTurno()
        }
    }

    Contenido {
        Text("Inicio del cliente", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Panel("1. Ingresa tu RUT") {
            Text("Escribe un RUT de demostración para pedir tu turno.")
            CampoFormulario(
                valor = formulario.rut,
                alCambiar = {
                    formularios.onRutChange(it)
                    vm.limpiarHistorialCliente()
                },
                etiqueta = "RUT del cliente", icono = Icons.Filled.Badge,
                error = formulario.rutError, ayuda = "Ejemplo: 12345678-5",
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done)
            )
            Etiqueta("Solo para pruebas: no ingreses tu RUT verdadero.")
        }
        Panel("2. Identifica la sucursal") {
            Text("Escanea el código de la tienda para identificar la sucursal.")
            Boton("Escanear código QR", abrirCamara, habilitado = !qrState.guardando)
            SelectorSucursal(vm.sucursal, {
                vm.cambiarSucursal(it)
                qrVm.limpiar()
            }, habilitado = !vm.qrValidado && !qrState.guardando)
            AnimacionEntradaFormulario(visible = qrState.actual != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val qr = qrState.actual
                    if (qr != null) {
                        HorizontalDivider()
                        Text("QR leído correctamente", fontWeight = FontWeight.Bold, color = VerdeLibre)
                        Text("Centro comercial: ${qr.centroComercial}")
                        Text("Tienda: ${qr.tienda}")
                        Text("Sección: ${qr.seccion}")
                        Text("Sucursal: ${qr.sucursal}")
                        Text("Coordenadas: ${qrState.ubicacion?.let {
                            String.format(Locale.US, "%.5f, %.5f", it.latitud, it.longitud)
                        } ?: "No registradas"}")
                        Etiqueta("La ubicación es opcional: se obtiene con permiso y se guarda solo si la autorizas.")
                        Boton(if (qrState.obteniendoUbicacion) "Obteniendo ubicación…" else "Obtener ubicación (opcional)", {
                            if (ContextCompat.checkSelfPermission(contexto,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                                qrVm.solicitarUbicacion()
                            else solicitarPermisoUbicacion.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }, secundario = true, habilitado = !qrState.guardado && !qrState.guardando && !qrState.obteniendoUbicacion)
                        if (permisoUbicacionDenegado) TextButton(onClick = {
                            contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${contexto.packageName}")))
                        }) { Text("Abrir ajustes de permisos") }
                        Boton(if (qrState.guardado) "Registro guardado" else "Guardar registro",
                            qrVm::guardar,
                            habilitado = !qrState.guardado && !qrState.guardando && !qrState.obteniendoUbicacion)
                    }
                }
            }
            qrState.mensaje?.let { Text(it) }
            Boton("Ver historial", verHistorialQr, secundario = true)
            if (vm.qrValidado) {
                Text("✓ Sucursal confirmada: ${vm.sucursal}", color = VerdeLibre)
            } else {
                Etiqueta("Para una demostración sin QR puedes activar el modo de prueba, identificado como simulación.")
                Boton("Modo prueba: continuar sin QR", vm::accesoDemoSinCamara, secundario = true)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("Disponibles", "$libres", VerdeLibre, Modifier.weight(1f))
            Contador("En fila", "$fila", AzulRincon, Modifier.weight(1f))
        }
        Panel("3. Entrar a la fila virtual") {
            if (ticket != null) {
                Text("Ya tienes un turno activo: #${ticket.id}.")
                Boton("Ver mi turno", verMiTurno)
            } else {
                Text("Espera estimada: ${espera?.let { "${(it + MINUTO - 1) / MINUTO} minutos" } ?: "Sin cupos"}")
                Etiqueta("El sistema asignará un probador libre automáticamente.")
                Boton(if (vm.solicitandoTurno) "Registrando turno…" else "Solicitar mi turno", {
                    formularios.validarRut()?.let(vm::solicitarTurno)
                }, habilitado = configuracion.abierta && espera != null && !vm.solicitandoTurno)
            }
            if (!configuracion.abierta) Text("Fila cerrada por el trabajador.", color = RojoOcupado)
            if (configuracion.cierre != null) Etiqueta("Horario límite: ${hora(configuracion.cierre)}")
            Etiqueta("Tu RUT no se guarda como texto.")
        }
    }
}

 
@Composable
fun ClienteTurnoScreen(vm: ProbadoresViewModel, estado: EstadoGeneral, volverInicio: () -> Unit) {
    val turno = estado.turnos.firstOrNull { it.id == vm.ticketId }
    Contenido {
        Text("Mi turno", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        when {
            turno == null || turno.estado in listOf("FINALIZADO", "CANCELADO") -> {
                Panel("No tienes turno activo") {
                    Text("Escanea el QR en Inicio para ingresar a la fila virtual.")
                    Boton("Volver al inicio", volverInicio)
                }
            }
            turno.estado == "ESPERA" -> {
                val esperando = estado.turnosEn(turno.sucursal).filter { it.estado == "ESPERA" }
                val posicion = esperando.indexOfFirst { it.id == turno.id } + 1
                val espera = CalculadoraFila.espera(estado.cabinasEn(turno.sucursal), posicion - 1, estado.ahora)
                Panel("Fila virtual") {
                    Text("Turno #${turno.id} · ${turno.sucursal}", color = Petroleo, fontWeight = FontWeight.Bold)
                    Text("$posicion", modifier = Modifier.fillMaxWidth(), fontSize = 70.sp,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text("Tu posición en la fila", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Text("Espera estimada: ${espera?.let { "${(it + MINUTO - 1) / MINUTO} min" } ?: "—"}")
                    Etiqueta("Cuando se libere un probador, aparecerá su número automáticamente.")
                    Boton("Cancelar mi turno", vm::terminarTurno, secundario = true)
                }
            }
            else -> {
                val transcurrido = ((estado.ahora - (turno.ingreso ?: estado.ahora)) / 1000).coerceAtLeast(0)
                val tiempoMostrado = transcurrido.coerceAtMost(MAX_MINUTOS * 60)
                val restante = (MAX_MINUTOS * 60 - transcurrido).coerceAtLeast(0)
                Panel("Tu probador está asignado") {
                    Text("PROBADOR ${turno.cabina?.toString()?.padStart(2, '0') ?: "—"}",
                        color = Petroleo, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${tiempoMostrado / 60}:${(tiempoMostrado % 60).toString().padStart(2,'0')}",
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                        fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                    Etiqueta("Tiempo utilizado (desde 00:00 hasta 12:00)")
                    LinearProgressIndicator(progress = { (transcurrido / (MAX_MINUTOS * 60f)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(12.dp))
                    Text("Quedan: ${restante / 60}:${(restante % 60).toString().padStart(2,'0')}")
                    if (transcurrido >= MAX_MINUTOS * 60) Text("Tiempo cumplido. El trabajador debe verificar el estado.", color = RojoOcupado)
                    else if (transcurrido >= 10 * 60) Text("Aviso registrado para el trabajador.", color = RojoOcupado)
                    Boton("Finalizar uso y liberar", vm::terminarTurno, peligro = true)
                }
                Panel("Solicitar ayuda") {
                    Boton("Necesito otra talla", { vm.ayuda("otra talla") }, secundario = true)
                    Boton("Necesito asistencia", { vm.ayuda("asistencia general") }, secundario = true)
                }
            }
        }
    }
}

@Composable
fun ClientePerfilScreen(vm: ProbadoresViewModel, volverRoles: () -> Unit) {
    Contenido {
        Text("Mi perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Panel("Datos de la sesión") {
            Text("Rol: Cliente")
            Text("Sucursal: ${vm.sucursal}")
            Text("Turno: ${vm.ticketId?.let { "#$it" } ?: "Sin turno"}")
            Etiqueta("Solo se muestran los datos de tu sesión.")
        }
        Panel("Acerca de la aplicación") {
            Text("Rincón Sur · MVP académico DSY1105")
            Etiqueta("Consulta tus turnos y la disponibilidad de probadores.")
            Boton("Cambiar de rol", volverRoles, secundario = true)
        }
    }
}

@Composable
fun ClienteHistorialScreen(vm: ProbadoresViewModel, formularios: FormulariosViewModel,
                           estado: EstadoGeneral, volver: () -> Unit) {
    val formulario by formularios.uiState.collectAsState()
    val historial = HistorialCliente.consultar(estado.turnos, vm.clienteHash)
    Contenido {
        Text("Historial", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Panel("Mis turnos") {
            Text("Consulta tus visitas con el RUT ficticio que usaste al pedir turno.")
            CampoFormulario(
                valor = formulario.rut,
                alCambiar = {
                    formularios.onRutChange(it)
                    vm.limpiarHistorialCliente()
                },
                etiqueta = "RUT del cliente", icono = Icons.Filled.Badge,
                error = formulario.rutError, ayuda = "Ejemplo: 12345678-5",
                teclado = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done)
            )
            Boton("Consultar mis turnos", { formularios.validarRut()?.let(vm::consultarHistorial) })
        }
        if (vm.clienteHash != null && historial.isEmpty()) Etiqueta("No hay turnos registrados para este RUT.")
        historial.forEach { turno ->
            Panel("Turno #${turno.id}") {
                Text("Sucursal: ${turno.sucursal}")
                Text("Estado: ${turno.estado}")
                Text("Solicitado: ${fechaHora(turno.solicita)}")
                turno.cabina?.let { Text("Probador: $it") }
                turno.ingreso?.let { Text("Ingreso: ${fechaHora(it)}") }
                turno.salida?.let { Text("Salida: ${fechaHora(it)}") }
            }
        }
        Boton("Volver al inicio", volver, secundario = true)
    }
}
