package cl.duoc.rinconsur.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.rinconsur.repository.data.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val usuarioError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val autenticado: Boolean = false
)

 

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).usuarioDao()
    private var loginJob: Job? = null
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsuarioChange(value: String) = _uiState.update {
        it.copy(usuario = value, usuarioError = null, error = null)
    }
    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, error = null)
    }
    fun login() {
        val s = _uiState.value
        if (s.isLoading) return
        val usuarioError = Validaciones.campoObligatorio(s.usuario, "Ingresa el usuario")
        val passwordError = Validaciones.campoObligatorio(s.password, "Ingresa la contraseña")
        _uiState.update { it.copy(usuarioError = usuarioError, passwordError = passwordError, error = null) }
        if (usuarioError != null || passwordError != null) return
        _uiState.update { it.copy(isLoading = true) }
        loginJob = viewModelScope.launch {
            try {
                val usuario = dao.login(s.usuario.trim().lowercase(), s.password)
                _uiState.update { it.copy(autenticado = usuario != null,
                    error = if (usuario == null) "Usuario o contraseña incorrectos." else null,
                    password = if (usuario != null) "" else it.password) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo iniciar sesión. Intenta nuevamente.") }
            } finally { _uiState.update { it.copy(isLoading = false) } }
        }
    }
    fun logout() {
        loginJob?.cancel()
        _uiState.value = LoginUiState()
    }
}
