package cl.duoc.rinconsur.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FormulariosUiState(
    val rut: String = "",
    val rutError: String? = null,
    val minutos: String = "30",
    val minutosError: String? = null
)

 
class FormulariosViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _uiState = MutableStateFlow(FormulariosUiState(
        rut = savedState["rut"] ?: "", minutos = savedState["minutos"] ?: "30"))
    val uiState = _uiState.asStateFlow()

    fun onRutChange(value: String) {
        val rut = value.uppercase().filter { it.isDigit() || it in "K-." }.take(12)
        savedState["rut"] = rut
        _uiState.update { it.copy(rut = rut, rutError = null) }
    }
    fun validarRut(): String? {
        val rut = _uiState.value.rut.replace(".", "").replace(" ", "").trim()
        val error = if (Validaciones.rutDemo(rut)) null
            else "Escribe un RUT de 7 u 8 dígitos y su dígito verificador."
        _uiState.update { it.copy(rutError = error) }
        return rut.takeIf { error == null }
    }
    fun onMinutosChange(value: String) {
        val minutos = value.filter(Char::isDigit).take(3)
        savedState["minutos"] = minutos
        _uiState.update { it.copy(minutos = minutos, minutosError = null) }
    }
    fun validarMinutos(): Int? {
        val valor = _uiState.value.minutos
        val error = if (Validaciones.minutosCierreValidos(valor)) null
            else "Ingresa un tiempo de 1 a 600 minutos."
        _uiState.update { it.copy(minutosError = error) }
        return valor.toIntOrNull().takeIf { error == null }
    }
    fun limpiarRut() {
        savedState["rut"] = ""
        _uiState.update { it.copy(rut = "", rutError = null) }
    }
}
