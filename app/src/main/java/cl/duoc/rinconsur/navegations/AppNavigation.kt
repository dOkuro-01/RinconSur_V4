package cl.duoc.rinconsur.navegations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import cl.duoc.rinconsur.view.ui.*
import cl.duoc.rinconsur.view.utils.*
import cl.duoc.rinconsur.view.theme.AzulRincon
import cl.duoc.rinconsur.viewmodels.*
import kotlinx.coroutines.launch

data class ItemNavegacion(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val navCliente = listOf(
    ItemNavegacion(Rutas.CLIENTE_INICIO, "Inicio", Icons.Filled.Home),
    ItemNavegacion(Rutas.CLIENTE_TURNO, "Mi turno", Icons.Filled.Schedule),
    ItemNavegacion(Rutas.CAMARA, "Cámara", Icons.Filled.PhotoCamera),
    ItemNavegacion(Rutas.REGISTROS_QR, "Historial", Icons.Filled.History),
    ItemNavegacion(Rutas.CLIENTE_PERFIL, "Perfil", Icons.Filled.Person)
)
private val navTrabajador = listOf(
    ItemNavegacion(Rutas.TRABAJADOR_INICIO, "Inicio", Icons.Filled.Home),
    ItemNavegacion(Rutas.TRABAJADOR_PROBADORES, "Probadores", Icons.Filled.Checkroom),
    ItemNavegacion(Rutas.TRABAJADOR_ATENCIONES, "Atenciones", Icons.Filled.History),
    ItemNavegacion(Rutas.TRABAJADOR_REPORTES, "Reportes", Icons.Filled.BarChart),
    ItemNavegacion(Rutas.TRABAJADOR_ALERTAS, "Avisos", Icons.Filled.Notifications),
    ItemNavegacion(Rutas.CAMARA, "Cámara", Icons.Filled.PhotoCamera),
    ItemNavegacion(Rutas.REGISTROS_QR, "Historial", Icons.Filled.History),
    ItemNavegacion(Rutas.TRABAJADOR_PERFIL, "Perfil", Icons.Filled.Person)
)

 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(vm: ProbadoresViewModel, login: LoginViewModel,
                  formularios: FormulariosViewModel, registrosQr: RegistrosQrViewModel,
                  compacto: Boolean) {
    val nav = rememberNavController()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entrada by nav.currentBackStackEntryAsState()
    val pantalla = entrada?.destination?.route ?: Rutas.BIENVENIDA
    val estado by vm.estado.collectAsState()
    val acceso by login.uiState.collectAsState()
    SideEffect { registrosQr.permitirGestion(acceso.autenticado) }
    val esAcceso = pantalla in listOf(Rutas.BIENVENIDA, Rutas.LOGIN_TRABAJADOR)
    val items = if (esAcceso) emptyList() else if (acceso.autenticado) navTrabajador else navCliente
    val titulo = items.firstOrNull { it.ruta == pantalla }?.etiqueta ?: "Rincón Sur"

    fun cambiar(ruta: String) {
        nav.navigate(ruta) {
            popUpTo(if (acceso.autenticado) Rutas.TRABAJADOR_INICIO else Rutas.CLIENTE_INICIO) {
                saveState = false
            }
            launchSingleTop = true
        }
        scope.launch { drawer.close() }
    }
    fun regresarRoles() {
        vm.salir()
        login.logout()
        registrosQr.limpiar()
        formularios.limpiarRut()
        nav.navigate(Rutas.BIENVENIDA) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
        scope.launch { drawer.close() }
    }
    
    LaunchedEffect(pantalla, acceso.autenticado) {
        if (pantalla in navTrabajador.map { it.ruta }.filter { it !in listOf(Rutas.REGISTROS_QR, Rutas.CAMARA) } && !acceso.autenticado) {
            nav.navigate(Rutas.LOGIN_TRABAJADOR) {
                popUpTo(nav.graph.id) { inclusive = true }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawer, gesturesEnabled = !esAcceso,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("Rincón Sur", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.etiqueta) }, selected = item.ruta == pantalla,
                        icon = { Icon(item.icono, contentDescription = null) },
                        onClick = { cambiar(item.ruta) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                if (!esAcceso) NavigationDrawerItem(
                    label = { Text("Salir / cambiar de rol") }, selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    onClick = ::regresarRoles
                )
            }
        }
    ) {
        Scaffold(topBar = {
            Column {
                TopAppBar(
                    title = { Text(titulo, maxLines = if (compacto) 1 else 2) },
                    navigationIcon = {
                        if (!esAcceso) IconButton(onClick = { scope.launch { drawer.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        if (pantalla != Rutas.BIENVENIDA) TextButton(onClick = ::regresarRoles) {
                            Text("Salir", color = androidx.compose.ui.graphics.Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulRincon,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                        actionIconContentColor = androidx.compose.ui.graphics.Color.White)
                )
                if (vm.mensaje.isNotBlank()) Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Text(vm.mensaje, Modifier.weight(1f).padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = vm::borrarMensaje) { Text("Cerrar") }
                    }
                }
            }
        }) { padding ->
            NavHost(navController = nav, startDestination = Rutas.BIENVENIDA,
                modifier = Modifier.padding(padding),
                enterTransition = navEnterTransition, exitTransition = navExitTransition,
                popEnterTransition = navPopEnterTransition, popExitTransition = navPopExitTransition) {
                composable(Rutas.BIENVENIDA) {
                    BienvenidaScreen({ cambiar(Rutas.CLIENTE_INICIO) }, { cambiar(Rutas.LOGIN_TRABAJADOR) })
                }
                composable(Rutas.LOGIN_TRABAJADOR) {
                    LoginTrabajadorScreen(login) {
                        nav.navigate(Rutas.TRABAJADOR_INICIO) {
                            popUpTo(Rutas.LOGIN_TRABAJADOR) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                composable(Rutas.CLIENTE_INICIO) {
                    ClienteInicioScreen(vm, formularios, registrosQr, estado,
                        { cambiar(Rutas.CLIENTE_TURNO) }, { cambiar(Rutas.REGISTROS_QR) }, { cambiar(Rutas.CAMARA) })
                }
                composable(Rutas.CAMARA) {
                    CameraScreen(alConfirmar = { codigo ->
                        // Ambos ViewModels reciben EXACTAMENTE el texto real leído por CameraX.
                        if (registrosQr.leer(codigo) && vm.usarQr(codigo)) {
                            cambiar(if (acceso.autenticado) Rutas.TRABAJADOR_INICIO else Rutas.CLIENTE_INICIO)
                        }
                    }, volver = { nav.popBackStack() })
                }
                composable(Rutas.REGISTROS_QR) { RegistrosQrScreen(registrosQr, puedeGestionar = acceso.autenticado) {
                    cambiar(if (acceso.autenticado) Rutas.TRABAJADOR_PERFIL else Rutas.CLIENTE_INICIO)
                } }
                composable(Rutas.CLIENTE_TURNO) { ClienteTurnoScreen(vm, estado) { cambiar(Rutas.CLIENTE_INICIO) } }
                composable(Rutas.CLIENTE_PERFIL) { ClientePerfilScreen(vm, ::regresarRoles) }
                composable(Rutas.TRABAJADOR_INICIO) {
                    TrabajadorInicioScreen(vm, estado, { cambiar(Rutas.TRABAJADOR_ALERTAS) }, { cambiar(Rutas.TRABAJADOR_REPORTES) })
                }
                composable(Rutas.TRABAJADOR_PROBADORES) { TrabajadorProbadoresScreen(vm, estado) }
                composable(Rutas.TRABAJADOR_ATENCIONES) { TrabajadorAtencionesScreen(vm, estado) }
                composable(Rutas.TRABAJADOR_REPORTES) { TrabajadorReportesScreen(vm, estado) }
                composable(Rutas.TRABAJADOR_ALERTAS) { TrabajadorAlertasScreen(vm, estado) }
                composable(Rutas.TRABAJADOR_PERFIL) {
                    TrabajadorPerfilScreen(vm, formularios, estado, { cambiar(Rutas.TRABAJADOR_REPORTES) },
                        { cambiar(Rutas.REGISTROS_QR) }, ::regresarRoles)
                }
            }
        }
    }
}
