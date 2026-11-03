package cl.duoc.rinconsur.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.rinconsur.view.utils.*
import cl.duoc.rinconsur.view.theme.*
import cl.duoc.rinconsur.viewmodels.LoginViewModel

@Composable
fun BienvenidaScreen(irCliente: () -> Unit, irTrabajador: () -> Unit) {
    Contenido {
        Spacer(Modifier.height(18.dp))
        Text("Bienvenido a Rincón Sur", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AzulRincon)
        Panel("Soy cliente") {
            Text("Escanea el QR del local, consulta la disponibilidad y recibe tu turno.")
            Boton("Entrar como cliente", irCliente)
        }
        Panel("Soy trabajador") {
            Text("Gestiona probadores, atiende avisos y consulta reportes.")
            Boton("Iniciar sesión de trabajador", irTrabajador)
        }
        Etiqueta("Prototipo académico: utiliza únicamente datos inventados.")
    }
}

@Composable
fun LoginTrabajadorScreen(vm: LoginViewModel, alIngresar: () -> Unit) {
    val estado by vm.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(estado.autenticado) { if (estado.autenticado) alIngresar() }
    Contenido {
        Spacer(Modifier.height(14.dp))
        Icon(Icons.Filled.Checkroom, contentDescription = "Rincón Sur",
            tint = AzulRincon, modifier = Modifier.size(64.dp).pulsoYRotacion())
        AnimacionEntradaFormulario(visible) {
            Panel("Acceso para personal de tienda") {
                OutlinedTextField(value = estado.usuario, onValueChange = vm::onUsuarioChange,
                    enabled = !estado.isLoading,
                    label = { Text("Usuario") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    isError = estado.usuarioError != null,
                    supportingText = { estado.usuarioError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = estado.password, onValueChange = vm::onPasswordChange,
                    enabled = !estado.isLoading,
                    label = { Text("Contraseña") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    isError = estado.passwordError != null,
                    supportingText = { estado.passwordError?.let { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Boton(if (estado.isLoading) "Ingresando…" else "Ingresar", vm::login,
                    habilitado = !estado.isLoading)
                Etiqueta("Cuenta de demostración: trabajador / 1234")
            }
        }
    }
}
