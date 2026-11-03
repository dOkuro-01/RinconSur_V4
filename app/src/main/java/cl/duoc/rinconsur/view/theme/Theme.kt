package cl.duoc.rinconsur.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RinconColores = lightColorScheme(
    primary = Petroleo,
    onPrimary = Color.White,
    secondary = AzulRincon,
    onSecondary = Color.White,
    background = FondoRincon,
    surface = Color.White,
    onSurface = AzulRincon,
    error = RojoOcupado
)

@Composable
fun RinconSurTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RinconColores, typography = RinconTypography, content = contenido)
}
