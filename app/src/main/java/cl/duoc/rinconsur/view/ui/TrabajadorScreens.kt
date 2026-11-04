package cl.duoc.rinconsur.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.rinconsur.repository.models.*
import cl.duoc.rinconsur.view.utils.*
import cl.duoc.rinconsur.view.theme.*
import cl.duoc.rinconsur.viewmodels.EstadoGeneral
import cl.duoc.rinconsur.viewmodels.ProbadoresViewModel
import cl.duoc.rinconsur.viewmodels.FormulariosViewModel
import cl.duoc.rinconsur.view.utils.EstadoProbadorAnimado
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TrabajadorInicioScreen(vm: ProbadoresViewModel, estado: EstadoGeneral,
                           verAlertas: () -> Unit, verReportes: () -> Unit) {
    val cabinas = estado.cabinasEn(vm.sucursal)
    val alertas = estado.alertasEn(vm.sucursal)
    val hoy = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val atencionesHoy = estado.turnosEn(vm.sucursal).count { (it.ingreso ?: 0L) >= hoy }
    Contenido {
        Text("Inicio · Resumen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("Libres", "${cabinas.count { it.estado == "LIBRE" }}", VerdeLibre, Modifier.weight(1f))
            Contador("Ocupados", "${cabinas.count { it.estado == "OCUPADO" }}", RojoOcupado, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("Revisión", "${cabinas.count { it.estado == "REVISION" }}", NaranjaRevision, Modifier.weight(1f))
            Contador("Hoy", "$atencionesHoy", AzulRincon, Modifier.weight(1f))
        }
        Panel("Alertas que requieren atención") {
            if (alertas.isEmpty()) Etiqueta("✓ No tienes alertas pendientes.")
            alertas.take(3).forEach { alerta ->
                Text("⚠ ${alerta.texto}")
                if (alerta.cabina != null) Boton("Liberar probador ${alerta.cabina}",
                    { vm.gestionar(alerta.cabina, "LIBRE") }, secundario = true)
                HorizontalDivider()
            }
            Boton("Ver todos los avisos (${alertas.size})", verAlertas, secundario = true)
        }
        Panel("Fila virtual") {
            Text("En espera: ${estado.turnosEn(vm.sucursal).count { it.estado == "ESPERA" }}")
            Text("Admisión: ${if (estado.config(vm.sucursal).abierta) "Abierta" else "Cerrada"}")
            Boton("Consultar reportes", verReportes, secundario = true)
        }
    }
}

@Composable
fun TrabajadorProbadoresScreen(vm: ProbadoresViewModel, estado: EstadoGeneral) {
    val cabinas = estado.cabinasEn(vm.sucursal)
    var detalle by remember { mutableStateOf<Int?>(null) }
    val cabinaSeleccionada = cabinas.firstOrNull { it.numero == detalle }
    Contenido {
        if (cabinaSeleccionada == null) {
            Text("Probadores", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
            Etiqueta("Verde: libre · Rojo: ocupado · Naranja: revisión")
            cabinas.forEach { cabina ->
                Panel {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Probador ${cabina.numero.toString().padStart(2, '0')}", fontWeight = FontWeight.Bold)
                            EstadoProbadorAnimado(cabina.estado)
                            if (cabina.inicio != null) {
                                val transcurrido = (estado.ahora - cabina.inicio).coerceAtLeast(0L)
                                Etiqueta("En uso: ${transcurrido / MINUTO} min")
                                Etiqueta("Restante: ${(MAX_MINUTOS * MINUTO - transcurrido).coerceAtLeast(0L) / MINUTO} min")
                            }
                        }
                        TextButton(onClick = { detalle = cabina.numero }) { Text("Detalle") }
                    }
                }
            }
        } else {
            TextButton(onClick = { detalle = null }) { Text("← Volver a probadores") }
            Panel("Detalle · Probador ${cabinaSeleccionada.numero}") {
                EstadoProbadorAnimado(cabinaSeleccionada.estado)
                if (cabinaSeleccionada.inicio != null) {
                    val segundos = ((estado.ahora - cabinaSeleccionada.inicio) / 1000).coerceAtLeast(0L)
                    Text("Tiempo utilizado: ${segundos / 60}:${(segundos % 60).toString().padStart(2, '0')}")
                    Text("Tiempo restante: ${(MAX_MINUTOS * 60 - segundos).coerceAtLeast(0L) / 60} minutos")
                }
                if (cabinaSeleccionada.estado == "LIBRE") {
                    Boton("Marcar como ocupado", { vm.gestionar(cabinaSeleccionada.numero, "OCUPADO") })
                    Boton("Poner en revisión", { vm.gestionar(cabinaSeleccionada.numero, "REVISION") }, secundario = true)
                } else {
                    Boton("Liberar manualmente", { vm.gestionar(cabinaSeleccionada.numero, "LIBRE") }, peligro = true)
                    if (cabinaSeleccionada.estado == "OCUPADO") {
                        Boton("Simular minuto 10 para presentación", { vm.simular(cabinaSeleccionada.numero, 10) }, secundario = true)
                        Boton("Simular minuto 12 para presentación", { vm.simular(cabinaSeleccionada.numero, 12) }, secundario = true)
                    }
                }
            }
            Panel("Historial del probador") {
                val historial = estado.turnosEn(vm.sucursal).filter { it.cabina == detalle }.asReversed()
                if (historial.isEmpty()) Etiqueta("Aún sin atenciones.")
                historial.take(15).forEach { t ->
                    Text("#${t.id} · ${t.estado}")
                    Etiqueta("${t.ingreso?.let(::fechaHora) ?: "—"} — ${t.salida?.let(::fechaHora) ?: "En curso"}")
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TrabajadorAtencionesScreen(vm: ProbadoresViewModel, estado: EstadoGeneral) {
    val historial = estado.turnosEn(vm.sucursal).filter { it.ingreso != null }.asReversed()
    Contenido {
        Text("Atenciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
        Panel("Historial de ingresos y salidas") {
            if (historial.isEmpty()) Etiqueta("Aún no hay atenciones registradas.")
            historial.forEach { t ->
                Text("Atención #${t.id} · Probador ${t.cabina}", fontWeight = FontWeight.Bold)
                Etiqueta("Ingreso: ${t.ingreso?.let(::fechaHora)}")
                Etiqueta("Salida: ${t.salida?.let(::fechaHora) ?: "En curso"}")
                Etiqueta("Duración: ${if (t.salida != null && t.ingreso != null) "${(t.salida - t.ingreso) / MINUTO} minutos" else "En curso"}")
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TrabajadorReportesScreen(vm: ProbadoresViewModel, estado: EstadoGeneral) {
    val hoy = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val atenciones = estado.turnosEn(vm.sucursal).filter { (it.ingreso ?: 0L) >= hoy }
    val terminadas = atenciones.filter { it.salida != null && it.ingreso != null }
    val duracion = if (terminadas.isEmpty()) 0L else terminadas.map { (it.salida!! - it.ingreso!!) / MINUTO }.average().toLong()
    val cantidades = (9..21).associateWith { h -> atenciones.count { t ->
        t.ingreso?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).hour == h } == true
    } }
    Contenido {
        Text("Reportes del turno", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("Atenciones", "${atenciones.size}", Petroleo, Modifier.weight(1f))
            Contador("Tiempo medio", "$duracion min", AzulRincon, Modifier.weight(1f))
        }
        Panel("Horas peak · atenciones por hora") {
            Etiqueta("Atenciones registradas hoy en esta sucursal.")
            Row(Modifier.fillMaxWidth().height(190.dp).horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cantidades.forEach { (hora, total) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$total", style = MaterialTheme.typography.labelSmall)
                        Box(Modifier.width(22.dp).height((12 + total * 22).coerceAtMost(140).dp)
                            .background(Petroleo, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                        Text("${hora}h", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            val peak = cantidades.maxByOrNull { it.value }
            Etiqueta(if (peak == null || peak.value == 0) "No hay datos para calcular una hora peak." else "Hora peak: ${peak.key}:00 · ${peak.value} atenciones")
        }
        Etiqueta("El reporte resume el uso de los probadores.")
    }
}

@Composable
fun TrabajadorAlertasScreen(vm: ProbadoresViewModel, estado: EstadoGeneral) {
    val alertas = estado.alertas.filter { it.sucursal == vm.sucursal }
    Contenido {
        Text("Avisos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
        Panel("Avisos de esta sucursal") {
            if (alertas.isEmpty()) Etiqueta("No hay avisos registrados.")
            alertas.forEach { a ->
                Text("${if (a.resuelta) "✓" else "⚠"} ${a.tipo}", fontWeight = FontWeight.Bold,
                    color = if (a.resuelta) VerdeLibre else NaranjaRevision)
                Text(a.texto)
                Etiqueta("${fechaHora(a.creada)} · ${if (a.resuelta) "Atendida" else "Pendiente"}")
                if (!a.resuelta) {
                    Boton("Marcar atendida", { vm.resolver(a.id) }, secundario = true)
                    if (a.cabina != null) Boton("Liberar probador ${a.cabina}", { vm.gestionar(a.cabina, "LIBRE") }, peligro = true)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun TrabajadorPerfilScreen(vm: ProbadoresViewModel, formularios: FormulariosViewModel, estado: EstadoGeneral,
                           verReportes: () -> Unit, verHistorialQr: () -> Unit, salir: () -> Unit) {
    val formulario by formularios.uiState.collectAsState()
    val config = estado.config(vm.sucursal)
    Contenido {
        Text("Perfil y configuración", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Panel("Trabajador") {
            Text("Usuario: ${vm.trabajador}")
            SelectorSucursal(vm.sucursal, vm::cambiarSucursal)
            Boton("Ver reporte del turno", verReportes, secundario = true)
            Boton("Ver historial", verHistorialQr, secundario = true)
        }
        Panel("Control de fila virtual") {
            Text("Estado: ${if (config.abierta) "Abierta" else "Cerrada"}")
            Etiqueta("No se aceptarán clientes si su espera + 12 minutos supera el horario de cierre.")
            CampoFormulario(
                valor = formulario.minutos, alCambiar = formularios::onMinutosChange,
                etiqueta = "Minutos para el cierre", icono = Icons.Filled.Timer,
                error = formulario.minutosError, ayuda = "Entre 1 y 600 minutos",
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Boton("Programar límite de ingreso", { formularios.validarMinutos()?.let(vm::programarCierre) })
            Boton("Cerrar fila ahora", vm::cerrarFila, peligro = true)
            Boton("Reabrir fila", vm::abrirFila, secundario = true)
        }
        Panel("Colación") {
            Text(if (config.colacion) "En colación" else "En servicio")
            Etiqueta("Al ir a colación se crea un aviso local al jefe. No se envía una notificación externa.")
            Boton(if (config.colacion) "Volver de colación" else "Iniciar colación", { vm.colacion(!config.colacion) }, secundario = true)
        }
        Panel("Demostración") {
            Boton("Demo: ocupar probadores libres", vm::prepararDemoFila, secundario = true)
            Boton("Restaurar datos ficticios", vm::reiniciar, secundario = true)
            Etiqueta("Los registros se conservan en este teléfono.")
            Boton("Cerrar sesión / cambiar de rol", salir, secundario = true)
        }
    }
}
