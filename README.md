
# Rincón Sur V4

**Proyecto académico DSY1105 · Desarrollo de Aplicaciones Móviles**

## Descripción del proyecto

Rincón Sur es una aplicación móvil desarrollada en Android Studio que permite administrar una fila virtual y asignar probadores de manera automática.

La aplicación cuenta con dos perfiles: **Cliente** y **Trabajador**. Utiliza una base de datos local SQLite para almacenar información y permite escanear códigos QR mediante la cámara del dispositivo.

También incorpora el uso de ubicación aproximada como segundo recurso nativo.

## Integrantes

-David Fuentes
-Krishna Aranguiz

## Tecnologías utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Material Design 3
- Arquitectura MVVM
- SQLite mediante Room
- CameraX
- ZXing Core
- LocationManager
- Gradle
- Git y GitHub

## Arquitectura MVVM

El proyecto utiliza el patrón de arquitectura MVVM (Model-View-ViewModel), separando la interfaz gráfica, la lógica de presentación y el acceso a los datos.

### UI (View)

Contiene las pantallas de la aplicación desarrolladas con Jetpack Compose.

Se encarga de mostrar información al usuario, recibir acciones y comunicarse con el ViewModel.

### ViewModel

Administra los estados de las pantallas y procesa las acciones que recibe desde la UI.

Se comunica con los repositorios para realizar operaciones sobre los datos.

### Model y Repository

Representan las entidades y gestionan el acceso a la información almacenada.

El Repository comunica el ViewModel con los DAO.

### DAO

Los DAO (Data Access Object) contienen las consultas y operaciones necesarias para acceder a la base de datos.

Permiten realizar operaciones CRUD:

- Create: crear registros.
- Read: consultar registros.
- Update: actualizar registros.
- Delete: eliminar registros.

### SQLite

La base de datos almacena información local relacionada con:

- Usuarios de demostración.
- Probadores.
- Turnos.
- Alertas.
- Configuraciones.
- Historial de códigos QR.

### Comunicación entre capas

El flujo general de información es:

UI → ViewModel → Repository → DAO → SQLite

Cuando el usuario realiza una acción, la UI invoca una función del ViewModel.

El ViewModel se comunica con el Repository, que utiliza el DAO para realizar la operación en SQLite.

Los resultados regresan mediante las capas correspondientes hasta actualizar la interfaz.

## Funcionalidades del Cliente

1. Ingresar a la aplicación seleccionando el perfil Cliente.
2. Introducir un RUT ficticio de demostración.
3. Escanear un código QR utilizando CameraX.
4. Obtener la información de la sucursal y sección contenida en el QR.
5. Solicitar opcionalmente la ubicación aproximada.
6. Guardar el escaneo en SQLite.
7. Consultar el historial de códigos QR.
8. Solicitar un turno de atención.
9. Recibir la asignación automática de un probador.
10. Consultar la posición en la fila, tiempo de espera y alertas.

La asignación de probadores se realiza automáticamente, sin que el cliente seleccione manualmente uno.

El botón "Modo prueba: continuar sin QR" permite simular el acceso a la fila, pero no genera un registro de escaneo QR.

## Funcionalidades del Trabajador

El trabajador puede:

- Iniciar sesión con credenciales de demostración.
- Consultar el estado de los probadores.
- Gestionar probadores libres, ocupados y en revisión.
- Consultar turnos y atenciones.
- Revisar alertas.
- Visualizar reportes.
- Gestionar períodos de colación.
- Abrir y cerrar la fila.
- Consultar el historial de códigos QR.
- Editar y eliminar registros QR con confirmación.

### Credenciales de demostración

Usuario: trabajador

Contraseña: 1234

Estas credenciales se utilizan únicamente con fines académicos. No representan un sistema de autenticación seguro para producción.

## Recursos nativos

### 1. Cámara mediante CameraX

La aplicación utiliza CameraX para acceder a la cámara del dispositivo y escanear códigos QR desde una pantalla integrada.

ZXing Core interpreta el contenido de los códigos QR capturados.

Archivos principales:

- view/ui/CameraScreen.kt
- camera/LectorQrCameraX.kt
- camera/DecodificadorQrZxing.kt
- viewmodels/logic/ProcesadorQr.kt
- viewmodels/RegistrosQrViewModel.kt

El lector funciona localmente, sin necesidad de enviar imágenes a Internet.

### 2. Ubicación aproximada

La aplicación utiliza LocationManager y LocationManagerCompat para obtener opcionalmente la ubicación aproximada del dispositivo.

Archivo principal:

- location/UbicacionActual.kt

El permiso de ubicación se solicita cuando el usuario utiliza esta funcionalidad.

Si el permiso es rechazado o la ubicación no está disponible, el código QR puede guardarse sin coordenadas.

## Base de datos SQLite

La aplicación utiliza Room como capa de acceso a SQLite.

Los datos se almacenan localmente en el dispositivo.

El historial de códigos QR permite realizar operaciones CRUD:

- Registrar un código QR escaneado.
- Consultar registros almacenados.
- Modificar registros existentes.
- Eliminar registros mediante confirmación.

El proyecto contempla migraciones de base de datos de las versiones 1 a 2 y de 2 a 3.

Los datos no se sincronizan automáticamente entre diferentes teléfonos.

## Formato de códigos QR

Los códigos QR de prueba utilizan el siguiente formato:

RINCONSUR|centro_comercial|tienda|seccion

Ejemplos:

    RINCONSUR|Mall Plaza Norte|Falabella|Hombre

    RINCONSUR|Mall La Florida|Tienda Demo|Mujer

    RINCONSUR|Mall Centro|Tienda Demo|Infantil

También se contempla compatibilidad con códigos antiguos, como:

    RINCONSUR|Plaza Norte

Los códigos QR de prueba se encuentran en la carpeta `docs/`.

La aplicación valida el formato y los centros comerciales admitidos antes de procesar la información.

## Requisitos para ejecutar

- Android Studio.
- JDK 17.
- Android SDK 35.
- Dispositivo Android con API 26 o superior.
- Cable USB para ejecutar en un teléfono físico, si corresponde.
- Conexión a Internet para descargar y sincronizar dependencias.

## Instalación y ejecución

1. Ingresar al repositorio:

   https://github.com/dOkuro-01/RinconSur_V4

2. Seleccionar **Code → Download ZIP**.

3. Descomprimir el archivo descargado.

4. Abrir Android Studio.

5. Seleccionar **Open**.

6. Abrir la carpeta raíz del proyecto `RinconSur_V4`.

7. Esperar a que termine la sincronización de Gradle.

8. Conectar un dispositivo Android o iniciar un emulador.

9. Activar la depuración USB si se utiliza un teléfono físico.

10. Presionar **Run app** para ejecutar la aplicación.

11. Autorizar los permisos necesarios para probar las funciones nativas.

## Compilación y pruebas

Desde la terminal de Android Studio, ubicada en la carpeta raíz del proyecto, ejecutar:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Si el proceso termina correctamente, mostrará:

    BUILD SUCCESSFUL

La APK de depuración se genera en:

    app/build/outputs/apk/debug/app-debug.apk

Este archivo debe generarse mediante la compilación del proyecto.

## Pruebas de códigos QR

Para comprobar la funcionalidad:

1. Iniciar la aplicación.
2. Seleccionar el perfil Cliente.
3. Ingresar un RUT ficticio válido.
4. Abrir el lector QR con CameraX.
5. Escanear uno de los códigos QR de prueba.
6. Comprobar que aparezca la información contenida en el código.
7. Guardar el registro en SQLite.
8. Consultar el historial de escaneos.
9. Cerrar y volver a abrir la aplicación.
10. Comprobar que el registro continúa almacenado.
11. Probar las operaciones de modificación y eliminación.

La guía de pruebas funcionales se encuentra en:

    docs/PRUEBAS_V4.md

## Estado de verificación

El código fuente del proyecto V4 fue revisado estáticamente y se validaron los archivos QR de prueba.

La ejecución de Gradle, las pruebas automatizadas y la instalación de la APK V4 en un dispositivo físico deben comprobarse antes de la entrega final.

No se declara una compilación o prueba funcional exitosa sin haberla ejecutado.

## Backend opcional

El repositorio incluye una carpeta `backend/` como componente de ejemplo.

Su uso es opcional y no sincroniza automáticamente la información entre diferentes dispositivos.

La aplicación utiliza almacenamiento local SQLite.

## Control de versiones

El proyecto se encuentra alojado en GitHub:

https://github.com/dOkuro-01/RinconSur_V4

Se utiliza Git para mantener un registro de los cambios realizados durante el desarrollo.

Los archivos temporales de compilación y configuración local se excluyen mediante `.gitignore`.

## Organización y planificación

La planificación del proyecto se realiza mediante Trello, utilizando tareas para organizar el desarrollo, las pruebas y las funcionalidades de la aplicación.

El tablero permite visualizar las tareas pendientes, en proceso y completadas.

La evidencia de planificación se presenta según los requisitos de la asignatura.

## Consideraciones finales

Rincón Sur V4 busca facilitar la administración de probadores mediante una fila virtual, asignación automática y registro local de información.

El proyecto aplica los contenidos de Desarrollo de Aplicaciones Móviles, incluyendo:

- Desarrollo con Kotlin.
- Interfaces con Jetpack Compose.
- Arquitectura MVVM.
- Persistencia local SQLite.
- Operaciones CRUD mediante DAO.
- Navegación entre pantallas.
- Uso de recursos nativos del dispositivo.
- Control de versiones con Git y GitHub.

**Proyecto desarrollado con fines académicos para la asignatura DSY1105 - Desarrollo de Aplicaciones Móviles.**
