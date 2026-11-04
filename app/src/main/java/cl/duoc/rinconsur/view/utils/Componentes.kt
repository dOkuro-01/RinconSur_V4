package cl.duoc.rinconsur.view.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.rinconsur.repository.models.SUCURSALES
import cl.duoc.rinconsur.view.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

 
@Composable
fun Contenido(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(FondoRincon).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun Panel(titulo: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (titulo != null) Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun Etiqueta(texto: String) { Text(texto, fontSize = 13.sp, color = GrisSecundario) }

@Composable
fun CampoFormulario(valor: String, alCambiar: (String) -> Unit, etiqueta: String,
                    icono: ImageVector, error: String? = null, ayuda: String = "",
                    habilitado: Boolean = true,
                    teclado: KeyboardOptions = KeyboardOptions.Default,
                    transformacion: VisualTransformation = VisualTransformation.None) {
    OutlinedTextField(
        value = valor, onValueChange = alCambiar, label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) Icon(Icons.Filled.Error, contentDescription = "Campo con error")
        },
        isError = error != null, supportingText = { Text(error ?: ayuda) },
        enabled = habilitado, singleLine = true, keyboardOptions = teclado,
        visualTransformation = transformacion, modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Boton(texto: String, accion: () -> Unit, secundario: Boolean = false, peligro: Boolean = false, habilitado: Boolean = true) {
    if (secundario) {
        OutlinedButton(onClick = accion, enabled = habilitado, modifier = Modifier.fillMaxWidth()) { Text(texto) }
    } else {
        Button(onClick = accion, enabled = habilitado, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (peligro) RojoOcupado else Petroleo)) { Text(texto) }
    }
}

@Composable
fun Contador(nombre: String, valor: String, color: Color = AzulRincon, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Etiqueta(nombre)
            Text(valor, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun SelectorSucursal(actual: String, cambiar: (String) -> Unit, habilitado: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Etiqueta("Sucursal")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SUCURSALES.forEach { sucursal ->
                FilterChip(selected = actual == sucursal, onClick = { cambiar(sucursal) },
                    enabled = habilitado, label = { Text(sucursal) })
            }
        }
    }
}

fun colorEstado(estado: String): Color = when (estado) {
    "LIBRE" -> VerdeLibre
    "REVISION" -> NaranjaRevision
    else -> RojoOcupado
}
fun hora(valor: Long): String = Instant.ofEpochMilli(valor).atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("HH:mm"))
fun fechaHora(valor: Long): String = Instant.ofEpochMilli(valor).atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
