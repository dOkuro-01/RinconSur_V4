package cl.duoc.rinconsur.view.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duoc.rinconsur.camera.LectorQrCameraX
import cl.duoc.rinconsur.view.utils.Boton

/** Pantalla CameraX: analiza los fotogramas en vivo; NO toma fotos ni simula el resultado. */
@Composable
fun CameraScreen(alConfirmar: (String) -> Unit, volver: () -> Unit) {
    val contexto = LocalContext.current
    var permisoConcedido by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val solicitarPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permisoConcedido = it
        if (!it) mensaje = "La cámara necesita tu permiso para leer códigos QR."
    }
    LaunchedEffect(Unit) {
        if (!permisoConcedido) solicitarPermiso.launch(Manifest.permission.CAMERA)
    }

    if (!permisoConcedido) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Permiso de cámara", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(mensaje ?: "Autoriza la cámara para escanear el código QR de Rincón Sur.", textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Boton("Autorizar cámara", { solicitarPermiso.launch(Manifest.permission.CAMERA) })
            Boton("Volver", volver, secundario = true)
        }
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        LectorQrCameraX(
            onQrValido = alConfirmar,
            onMensaje = { mensaje = it },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Escanear código QR", color = Color.White,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Apunta la cámara al QR de la tienda. Se leerá automáticamente, sin tomar una foto.",
                color = Color.White, textAlign = TextAlign.Center)
            TextButton(onClick = volver) { Text("Cancelar escaneo", color = Color.White) }
        }
        Box(
            Modifier.align(Alignment.Center).size(250.dp)
                .border(3.dp, Color.White, RoundedCornerShape(20.dp))
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f)).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(mensaje ?: "Buscando QR válido de Rincón Sur…", color = Color.White,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("CameraX + lector QR local (ZXing). Sin escáner de Google.",
                color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}
