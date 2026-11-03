# RinconSur V4 · Guía de pruebas para defensa y GitHub

**Estado:** plan de pruebas manuales pendiente de ejecutar en Android Studio / Samsung S25. No registrar aprobaciones no comprobadas.

## 1) Compilación

En la carpeta raíz `RinconSur_V4` ejecutar:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Anotar resultado real: `BUILD SUCCESSFUL`, o copiar error con número de línea.

## 2) Pruebas del recurso nativo 1: CameraX

- [ ] La cámara abre dentro de la pantalla Rincón Sur y solicita permiso Android `CAMERA`.
- [ ] Al denegar permiso, la aplicación permanece abierta y permite volver a solicitarlo.
- [ ] El escáner reconoce el **QR físico** `QR_V4_PlazaNorte_Falabella_Hombre.png` sin presionar botón de fotografía.
- [ ] Se muestran *Mall Plaza Norte*, *Falabella*, *Hombre*, *Plaza Norte*.
- [ ] Con `QR_V4_LaFlorida_TiendaDemo_Mujer.png` cambian automáticamente sucursal y sección.
- [ ] Con un QR externo sin prefijo `RINCONSUR` muestra advertencia y no registra una sucursal falsa.
- [ ] La cámara deja de analizar al cambiar de pantalla; se puede volver a abrir sin error.

## 3) Pruebas del recurso nativo 2: ubicación

- [ ] Después del QR, el botón `Obtener ubicación actual (opcional)` solicita permiso aproximado.
- [ ] Si se concede y el proveedor está disponible, se muestran coordenadas.
- [ ] Si se deniega, se informa el motivo y **se puede guardar el QR** igualmente.
- [ ] El historial muestra latitud/longitud del QR guardado, o `No registradas`.

## 4) Datos, MVVM y formularios

- [ ] Guardar un QR en SQLite y confirmar que aparece en Historial.
- [ ] Buscar por tienda y sección: resultado entregado por DAO.
- [ ] Como trabajador (`trabajador` / `1234`), editar la sección y confirmar el cambio.
- [ ] Como trabajador, eliminar un registro (probar primero **cancelar** eliminación).
- [ ] Cerrar y volver a abrir la app; comprobar que registros restantes se conservan.
- [ ] Introducir RUT ficticio inválido y observar mensaje de validación.
- [ ] Introducir RUT ficticio válido, solicitar turno y comprobar asignación automática o fila virtual.
- [ ] Liberar una cabina desde Trabajador y comprobar que la fila se actualiza.
- [ ] Ver las animaciones de navegación y cambios de formulario.
- [ ] Mostrar carpetas `camera`, `location`, `view`, `viewmodels`, `repository`, `data`.

## 5) Checklist de entrega

- [ ] README con integrantes reales y número de equipo.
- [ ] Proyecto compilado sin errores y probado en teléfono real.
- [ ] Repositorio público con `.gitignore` y commits auténticos y descriptivos.
- [ ] Tablero de Trello (o equivalente) con planificación compartida.
- [ ] Evidencia de pruebas reales en la defensa (no afirmar que un QR simulado es escaneo).
- [ ] Entrega en GitHub y AVA antes del plazo.

## Posibles limitaciones conocidas

- La app conserva datos **localmente**. No hay sincronización entre dispositivos.
- La ubicación aproximada puede no estar disponible con GPS o proveedor de red desactivado.
- La app soporta sucursales reconocidas por `ProcesadorQr`; códigos de otras cadenas no ingresan al sistema.
- La cuenta de trabajador de prueba no ofrece seguridad de producción.
