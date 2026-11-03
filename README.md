# Rincón Sur V4 — Probadores inteligentes

Proyecto académico **DSY1105 · Desarrollo de Aplicaciones Móviles**.

Rincón Sur administra una fila virtual y asigna probadores automáticamente. Incluye modo **Cliente** y **Trabajador**, un historial real de códigos QR y dos recursos nativos: **cámara con CameraX** y **ubicación aproximada con LocationManager**.

> **Estado de verificación:** el código fuente fue revisado estáticamente y se validaron los archivos QR de prueba. No se ha ejecutado Gradle ni instalado la APK V4 en un teléfono desde este entorno. Antes de entregar, ejecutar los comandos y el checklist incluidos en `docs/PRUEBAS_V4.md`.

## Integrantes

- **Agregar aquí nombres y apellidos de integrantes** y número de equipo antes de la entrega.

## Requisitos de desarrollo

- Android Studio, JDK 17, Android SDK 35 instalado.
- Dispositivo o emulador Android **API 26 o superior** (preferible Samsung S25 real para cámara).
- Conexión a Internet **solo para sincronizar dependencias con Gradle**; el lector QR no necesita Internet.
- El backend de ejemplo está en `backend/`, pero es **opcional** y **no sincroniza** los datos entre teléfonos.

## Abrir y ejecutar (Windows)

1. Descomprime el ZIP y abre la **carpeta `RinconSur_V4`** en Android Studio (no abras `app` sola).
2. Espera `Gradle Sync` e instala SDK 35 si Android Studio lo solicita.
3. Conecta el teléfono, activa *Depuración por USB*, autoriza la conexión y selecciónalo en Android Studio.
4. Ejecuta ▶ **Run app**. Autoriza Cámara para escanear QR y Ubicación aproximada solo si deseas guardar coordenadas.
5. Prueba los QRs en `docs/`, impresos o en otra pantalla.

Para comprobar compilación y pruebas desde la terminal de Android Studio:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Si el comando termina con **BUILD SUCCESSFUL**, la APK debug estará en `app/build/outputs/apk/debug/app-debug.apk`. Ese archivo es un resultado **por generar**, no viene incluido en este ZIP.

## Flujo principal

### Cliente

1. Selecciona **Cliente**, ingresa un RUT **ficticio** de demostración (ejemplo: `12345678-5`).
2. Pulsa **Escanear QR con CameraX**: la cámara se abre dentro de Rincón Sur.
3. Apunta al QR: la aplicación lee automáticamente `RINCONSUR|centro|tienda|sección`, sin tomar foto ni confirmar una sucursal de una lista.
4. Se muestran centro comercial, tienda, sección y sucursal reales del código.
5. Opcional: **Obtener ubicación actual**, autorizando ubicación aproximada. Si el permiso se rechaza, guarda sin coordenadas.
6. Pulsa **Guardar escaneo en SQLite**. Después consulta el historial y solicita un turno; el sistema asigna probador sin que el cliente lo elija.
7. Consulta la posición de fila, tiempo y alertas de atención.

> El botón **Modo prueba: continuar sin QR** solo simula el acceso a la fila y **no genera un registro QR**; no se debe mostrar como prueba del recurso de cámara.

### Trabajador

- Usuario de demostración: `trabajador`.
- Contraseña de demostración: `1234`.
- Gestiona probadores libres, ocupados, en revisión; ve alertas y atenciones, reportes, colación y apertura/cierre de fila.
- Puede escanear QR y editar/eliminar registros del historial, con confirmación.

**Aviso de seguridad:** la contraseña anterior se inserta en SQLite como dato ficticio en texto plano para la asignatura; **no utilizar esta autenticación de demostración en producción**.

## Cámara nativa de CameraX, no Google Code Scanner

| Capa | Archivo | Responsabilidad |
| --- | --- | --- |
| Vista | `view/ui/CameraScreen.kt` | Solicita permiso y presenta cámara + marco de lectura |
| Cámara | `camera/LectorQrCameraX.kt` | AndroidX CameraX `Preview`, `ImageAnalysis` y ciclo de vida |
| Decodificador | `camera/DecodificadorQrZxing.kt` | Procesa fotogramas de la cámara con **ZXing Core**, local y sin servicios de Google |
| Validación | `viewmodels/logic/ProcesadorQr.kt` | Valida prefijo, sucursal, centro, tienda y sección |
| Estado | `viewmodels/RegistrosQrViewModel.kt` | Entrega resultado a UI, fecha y ubicación |
| Datos | `repository/RegistrosQrRepository.kt` → DAO | Guarda, busca, edita y elimina en Room/SQLite |

**CameraX captura la imagen de la cámara del dispositivo dentro de la app.** ZXing solamente interpreta el código QR de esos fotogramas: no abre Google Code Scanner, no muestra interfaz externa y no requiere Google Play Services ni enviar imágenes a Internet. Se usa una librería de lectura de códigos porque CameraX **por sí sola no interpreta el contenido de los QR**.

## Segundo recurso nativo: ubicación

`location/UbicacionActual.kt` utiliza `LocationManager` y `LocationManagerCompat` de AndroidX con el proveedor de red del dispositivo. Solicita `ACCESS_COARSE_LOCATION` solo cuando el cliente lo pide. Puede devolver coordenadas aproximadas o informar que no están disponibles, sin impedir guardar el QR.

## Persistencia y arquitectura

- **Kotlin + Jetpack Compose + Material 3**.
- Patrón **MVVM**: `view/ui/` → `viewmodels/` → `repository/` → `repository/data/` (DAO Room) → SQLite.
- SQLite contiene cabinas, turnos, alertas, configuraciones, usuarios de demostración y registros QR.
- Migraciones **1 → 2 → 3** para mantener compatibilidad con versiones antiguas de la BD del proyecto.
- Las coordenadas se guardan solo si fueron obtenidas por permiso explícito.
- Los datos se almacenan **localmente**, no se comparten automáticamente entre teléfonos.

## Formato QR aceptado

Para sucursales del MVP se aceptan estas familias de centros comerciales: **Mall Plaza Norte / Plaza Norte, Mall Centro / Centro, Mall La Florida / La Florida**. Ejemplos:

```text
RINCONSUR|Mall Plaza Norte|Falabella|Hombre
RINCONSUR|Mall La Florida|Tienda Demo|Mujer
RINCONSUR|Mall Centro|Tienda Demo|Infantil
```

También se aceptan QR antiguos como `RINCONSUR|Plaza Norte`. Otros centros comerciales no configurados se rechazan. Los QRs en `docs/` se verificaron con otro lector QR para confirmar el texto que contienen.

## Preparación para GitHub y evaluación

- Subir esta carpeta **sin** `.gradle/`, `.idea/`, `app/build/`, `local.properties` ni APK generadas (ya se excluyen mediante `.gitignore`).
- Completar integrantes antes de publicar.
- Mantener commits claros y reales. No inventar commits de fechas anteriores.
- Adjuntar evidencia de la planificación Trello o equivalente según la rúbrica.
- Entregar además el proyecto en AVA, según corresponda.
- **No editar el repositorio después del plazo de entrega y antes de la evaluación**, ya que la pauta advierte una sanción por cambios tardíos.

Revisar `docs/PRUEBAS_V4.md` para el checklist funcional.
