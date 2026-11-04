package cl.duoc.rinconsur.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.rinconsur.view.utils.*
import cl.duoc.rinconsur.viewmodels.RegistrosQrViewModel
import java.util.Locale

 
@Composable
fun RegistrosQrScreen(vm: RegistrosQrViewModel, puedeGestionar: Boolean = false, volver: () -> Unit) {
    val registros by vm.registros.collectAsState()
    val formulario by vm.uiState.collectAsState()
    var confirmarEliminar by remember { mutableStateOf<Long?>(null) }
    Contenido {
        Text("Historial", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Boton("Volver", volver, secundario = true)
        OutlinedTextField(
            value = formulario.filtro,
            onValueChange = vm::buscar,
            label = { Text("Buscar centro, tienda o sección") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            supportingText = { Text("La búsqueda consulta SQLite mediante el DAO.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (registros.isEmpty()) Etiqueta("No hay registros coincidentes. Escanea y guarda un QR desde Inicio.")
        formulario.mensaje?.let { Text(it) }
        registros.forEach { registro ->
            key(registro.id) {
            Panel("Registro #${registro.id}") {
                Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("${registro.tienda} · ${registro.seccion}", fontWeight = FontWeight.Bold)
                    Text("Centro comercial: ${registro.centroComercial}")
                    Text("Sucursal: ${registro.sucursal}")
                    Etiqueta("Escaneado: ${fechaHora(registro.fechaEscaneo)}")
                    Text("Coordenadas: ${if (registro.latitud != null && registro.longitud != null)
                        String.format(Locale.US, "%.5f, %.5f", registro.latitud, registro.longitud)
                        else "No registradas"}")
                    if (puedeGestionar) {
                    Boton("Editar sección", {
                        vm.editar(registro.id, registro.seccion)
                    }, secundario = true)
                    AnimatedVisibility(visible = formulario.editando == registro.id) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoFormulario(
                                valor = formulario.seccion, alCambiar = vm::onSeccionChange,
                                etiqueta = "Nueva sección", icono = Icons.Filled.Edit,
                                error = formulario.seccionError, ayuda = "Entre 2 y 60 caracteres"
                            )
                            Boton("Guardar cambios", vm::guardarSeccion)
                            Boton("Cancelar edición", vm::cancelarEdicion, secundario = true)
                        }
                    }
                    if (confirmarEliminar == registro.id) {
                        Text("¿Eliminar definitivamente este registro de SQLite?")
                        Boton("Sí, eliminar registro", {
                            vm.eliminar(registro.id)
                            confirmarEliminar = null
                        }, peligro = true)
                        Boton("No, conservar", { confirmarEliminar = null }, secundario = true)
                    } else {
                        Boton("Eliminar registro", { confirmarEliminar = registro.id },
                            secundario = true)
                    }
                    }
                }
            }
            }
        }
    }
}
