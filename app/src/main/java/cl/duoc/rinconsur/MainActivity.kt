package cl.duoc.rinconsur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.rinconsur.navegations.AppNavigation
import cl.duoc.rinconsur.view.theme.RinconSurTheme
import cl.duoc.rinconsur.viewmodels.*

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ProbadoresViewModel = viewModel()
            val login: LoginViewModel = viewModel()
            val formularios: FormulariosViewModel = viewModel()
            val registrosQr: RegistrosQrViewModel = viewModel()
            
            val ancho = calculateWindowSizeClass(this@MainActivity).widthSizeClass
            RinconSurTheme { AppNavigation(vm, login, formularios, registrosQr, compacto = ancho == WindowWidthSizeClass.Compact) }
        }
    }
}
